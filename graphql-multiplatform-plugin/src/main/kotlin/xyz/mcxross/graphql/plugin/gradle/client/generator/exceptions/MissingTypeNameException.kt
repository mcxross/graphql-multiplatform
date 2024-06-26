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

package xyz.mcxross.graphql.plugin.gradle.client.generator.exceptions

/** Exception thrown when polymorphic query does not specify __typename information. */
internal class MissingTypeNameException(
  operationName: String,
  interfaceName: String,
  implementationName: String,
) :
  RuntimeException(
    "Operation $operationName specifies invalid polymorphic selection set - $implementationName implementation of $interfaceName is missing __typename field in its selection set"
  )
