/*
 * Copyright 2021 Expedia, Inc
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
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import graphql.Directives.DeprecatedDirective
import graphql.Directives.IncludeDirective
import graphql.Directives.SkipDirective
import graphql.language.Field
import graphql.language.FieldDefinition
import graphql.language.NonNullType
import graphql.language.SelectionSet
import graphql.language.StringValue
import kotlinx.serialization.Serializable
import xyz.mcxross.graphql.plugin.gradle.client.generator.GraphQLClientGeneratorContext
import xyz.mcxross.graphql.plugin.gradle.client.generator.ScalarConverterInfo
import xyz.mcxross.graphql.plugin.gradle.client.generator.exceptions.DeprecatedFieldsSelectedException
import xyz.mcxross.graphql.plugin.gradle.client.generator.exceptions.InvalidSelectionSetException
import xyz.mcxross.graphql.plugin.gradle.client.generator.exceptions.MissingArgumentException

/** Generate [PropertySpec]s from the field definitions and selection set. */
internal fun generatePropertySpecs(
  context: GraphQLClientGeneratorContext,
  objectName: String,
  selectionSet: SelectionSet,
  fieldDefinitions: List<FieldDefinition>,
  abstract: Boolean = false,
): List<PropertySpec> =
  selectionSet
    .getSelectionsOfType(Field::class.java)
    .filterNot { it.name == "__typename" }
    .map { selectedField ->
      val fieldDefinition =
        fieldDefinitions.find { it.name == selectedField.name }
          ?: throw InvalidSelectionSetException(
            context.operationName,
            selectedField.name,
            objectName,
          )

      val missingRequiredArguments =
        fieldDefinition.inputValueDefinitions
          .filter { it.type is NonNullType }
          .map { it.name }
          .minus(selectedField.arguments.map { it.name })
      if (missingRequiredArguments.isNotEmpty()) {
        throw MissingArgumentException(
          context.operationName,
          objectName,
          selectedField.name,
          missingRequiredArguments,
        )
      }

      val optional =
        selectedField.directives.any {
          it.name == SkipDirective.name || it.name == IncludeDirective.name
        }
      val kotlinFieldType =
        generateTypeName(
          context,
          fieldDefinition.type,
          selectedField.selectionSet,
          optional = optional,
        )
      val fieldName = selectedField.alias ?: fieldDefinition.name

      val propertySpecBuilder = PropertySpec.builder(fieldName, kotlinFieldType)

      if (!abstract) {
        propertySpecBuilder.initializer(fieldName)
        val (rawType, isList) = unwrapRawType(kotlinFieldType)
        if (context.isCustomScalar(rawType)) {
          generateCustomScalarPropertyAnnotations(context, rawType, isList).forEach {
            scalarAnnotation ->
            propertySpecBuilder.addAnnotation(scalarAnnotation)
          }
        }
      } else {
        propertySpecBuilder.addModifiers(KModifier.ABSTRACT)
      }
      val deprecatedDirective =
        fieldDefinition.getDirectives(DeprecatedDirective.name).firstOrNull()
      if (deprecatedDirective != null) {
        if (!context.allowDeprecated) {
          throw DeprecatedFieldsSelectedException(
            context.operationName,
            selectedField.name,
            objectName,
          )
        } else {
          val deprecatedReason = deprecatedDirective.getArgument("reason")?.value as? StringValue
          val reason = deprecatedReason?.value ?: "no longer supported"
          propertySpecBuilder.addAnnotation(
            AnnotationSpec.builder(Deprecated::class).addMember("message = %S", reason).build()
          )
        }
      }
      fieldDefinition.description?.content?.let { kdoc -> propertySpecBuilder.addKdoc("%L", kdoc) }
      propertySpecBuilder.build()
    }

internal fun unwrapRawType(type: TypeName): Pair<TypeName, Boolean> {
  val rawType = type.unwrapNullableType()
  return if (rawType is ParameterizedTypeName) {
    rawType.typeArguments.first().copy(annotations = emptyList()) to true
  } else {
    rawType.copy(annotations = emptyList()) to false
  }
}

private fun TypeName.unwrapNullableType(): TypeName =
  if (this.isNullable) {
    this.copy(nullable = false)
  } else {
    this
  }

internal fun generateCustomScalarPropertyAnnotations(
  context: GraphQLClientGeneratorContext,
  rawType: TypeName,
  isList: Boolean = false,
  shouldWrapInOptional: Boolean = false,
): List<AnnotationSpec> {
  val result = mutableListOf<AnnotationSpec>()
  val converterInfo =
    context.scalarClassToConverterTypeSpecs[rawType] as ScalarConverterInfo.KotlinxSerializerInfo

  if (!isList) {
    result.add(
      AnnotationSpec.builder(Serializable::class)
        .addMember("with = %T::class", converterInfo.serializerClassName)
        .build()
    )
  }

  return result
}
