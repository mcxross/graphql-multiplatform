/*
 * Copyright 2022 Expedia, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.mcxross.graphql.plugin.gradle.client.generator.types

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import graphql.language.InputObjectTypeDefinition
import graphql.language.NamedNode
import graphql.language.Type
import kotlinx.serialization.Serializable
import xyz.mcxross.graphql.client.Generated
import xyz.mcxross.graphql.plugin.gradle.client.generator.GraphQLClientGeneratorContext
import xyz.mcxross.graphql.plugin.gradle.client.generator.ScalarConverterInfo

/**
 * Generate [TypeSpec] data class from the specified input object definition where are fields are
 * mapped to corresponding Kotlin property.
 */
internal fun generateGraphQLInputObjectTypeSpec(
  context: GraphQLClientGeneratorContext,
  inputObjectDefinition: InputObjectTypeDefinition,
): TypeSpec {
  val inputTypeName = inputObjectDefinition.name
  val inputObjectTypeSpecBuilder =
    TypeSpec.classBuilder(inputTypeName)
      .addModifiers(KModifier.DATA)
      .addAnnotation(Generated::class)
  inputObjectDefinition.description?.content?.let { kdoc ->
    inputObjectTypeSpecBuilder.addKdoc("%L", kdoc)
  }

  inputObjectTypeSpecBuilder.addAnnotation(Serializable::class)

  val constructorBuilder = FunSpec.constructorBuilder()
  inputObjectDefinition.inputValueDefinitions.forEach { fieldDefinition ->
    val (inputPropertySpec, defaultValue) =
      createInputPropertySpec(
        context,
        fieldDefinition.name,
        fieldDefinition.type,
        fieldDefinition.description?.content,
      )
    inputObjectTypeSpecBuilder.addProperty(inputPropertySpec)

    constructorBuilder.addParameter(
      ParameterSpec.builder(inputPropertySpec.name, inputPropertySpec.type)
        .also { builder ->
          if (defaultValue != null) {
            builder.defaultValue(defaultValue)
          }
        }
        .build()
    )
  }
  inputObjectTypeSpecBuilder.primaryConstructor(constructorBuilder.build())

  return inputObjectTypeSpecBuilder.build()
}

internal fun shouldWrapInOptional(type: TypeName, context: GraphQLClientGeneratorContext) =
  type.isNullable && context.useOptionalInputWrapper

internal fun TypeName.wrapOptionalInputType(context: GraphQLClientGeneratorContext): TypeName =
  ClassName("xyz.mcxross.graphql.client.serialization.types", "OptionalInput")
    .parameterizedBy(this.copy(nullable = false))

internal fun createInputPropertySpec(
  context: GraphQLClientGeneratorContext,
  graphqlFieldName: String,
  graphqlFieldType: Type<*>,
  graphqlFieldDescription: String? = null,
): Pair<PropertySpec, CodeBlock?> {
  val kotlinFieldTypeName = generateTypeName(context, graphqlFieldType)

  val (rawType, isList) = unwrapRawType(kotlinFieldTypeName)
  val isScalar =
    rawType in setOf(BOOLEAN, DOUBLE, INT, STRING) ||
      (graphqlFieldType is NamedNode<*> && context.isTypeAlias(graphqlFieldType.name))
  val isCustomScalar = context.isCustomScalar(rawType)
  val shouldWrapInOptional = shouldWrapInOptional(kotlinFieldTypeName, context)

  val scalarAnnotations =
    if (isCustomScalar) {
      generateCustomScalarPropertyAnnotations(context, rawType, isList, shouldWrapInOptional)
    } else {
      emptyList()
    }

  val inputFieldType =
    if (shouldWrapInOptional) {
      kotlinFieldTypeName.copy(annotations = scalarAnnotations).wrapOptionalInputType(context)
    } else {
      kotlinFieldTypeName
    }

  val inputProperty =
    PropertySpec.builder(graphqlFieldName, inputFieldType)
      .initializer(graphqlFieldName)
      .also { builder ->
        if (graphqlFieldDescription != null) {
          builder.addKdoc("%L", graphqlFieldDescription)
        }

        if (shouldWrapInOptional) {
          context.requireOptionalSerializer = context.requireOptionalSerializer || isCustomScalar

          if (!isCustomScalar) {
            builder.addAnnotations(scalarAnnotations)
          }

          val customSerializerInfo =
            context.scalarClassToConverterTypeSpecs[rawType]
              as? ScalarConverterInfo.KotlinxSerializerInfo
          val optionalSerializerClassName =
            if (isList && isScalar) {
              ClassName(
                "xyz.mcxross.graphql.client.serialization.serializers",
                "OptionalScalarListSerializer",
              )
            } else if (isScalar) {
              ClassName(
                "xyz.mcxross.graphql.client.serialization.serializers",
                "OptionalScalarSerializer",
              )
            } else {
              val elementName = (rawType as ClassName).simpleName
              val className =
                if (isList) {
                  ClassName(
                    "${context.packageName}.scalars",
                    "Optional${elementName}ListSerializer",
                  )
                } else {
                  ClassName("${context.packageName}.scalars", "Optional${elementName}Serializer")
                }
              context.optionalSerializers.computeIfAbsent(className) {
                generateKotlinxOptionalInputSerializer(
                  rawType,
                  className.simpleName,
                  customSerializerInfo?.serializerClassName,
                  isList,
                )
              }
              className
            }
          builder.addAnnotation(
            AnnotationSpec.builder(Serializable::class)
              .addMember("with = %T::class", optionalSerializerClassName)
              .build()
          )
        } else {
          builder.addAnnotations(scalarAnnotations)
        }
      }
      .build()
  val defaultValue =
    if (kotlinFieldTypeName.isNullable) {
      nullableDefaultValueCodeBlock(context)
    } else {
      null
    }
  return inputProperty to defaultValue
}

internal fun nullableDefaultValueCodeBlock(context: GraphQLClientGeneratorContext): CodeBlock =
  if (context.useOptionalInputWrapper) {
    CodeBlock.of(
      "%M",
      MemberName("xyz.mcxross.graphql.client.serialization.types", "OptionalInput.Undefined"),
    )
  } else {
    CodeBlock.of("null")
  }
