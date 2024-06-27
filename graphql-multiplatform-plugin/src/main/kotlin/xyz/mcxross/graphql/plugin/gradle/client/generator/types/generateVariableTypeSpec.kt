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

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec
import graphql.language.VariableDefinition
import kotlinx.serialization.Serializable
import xyz.mcxross.graphql.client.Generated
import xyz.mcxross.graphql.plugin.gradle.client.generator.GraphQLClientGeneratorContext

/** Generate [TypeSpec] data class wrapper for variables used within the target query. */
internal fun generateVariableTypeSpec(
  context: GraphQLClientGeneratorContext,
  variableDefinitions: List<VariableDefinition>,
): TypeSpec? {
  val variablesTypeName = "Variables"
  val variableTypeSpec =
    TypeSpec.classBuilder(variablesTypeName)
      .addModifiers(KModifier.DATA)
      .addAnnotation(Generated::class)

  variableTypeSpec.addAnnotation(Serializable::class)

  val constructorSpec = FunSpec.constructorBuilder()
  variableDefinitions.forEach { variableDef ->
    val (variable, defaultValue) =
      createInputPropertySpec(context, variableDef.name, variableDef.type)
    variableTypeSpec.addProperty(variable)

    constructorSpec.addParameter(
      ParameterSpec.builder(variable.name, variable.type)
        .also { builder ->
          if (defaultValue != null) {
            builder.defaultValue(defaultValue)
          }
        }
        .build()
    )
  }

  variableTypeSpec.primaryConstructor(constructorSpec.build())
  return if (variableTypeSpec.propertySpecs.isEmpty()) {
    null
  } else {
    variableTypeSpec.build()
  }
}
