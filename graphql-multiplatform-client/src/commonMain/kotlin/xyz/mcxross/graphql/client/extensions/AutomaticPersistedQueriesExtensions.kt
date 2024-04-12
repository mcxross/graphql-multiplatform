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

/*
 * Modifications made:
 * - Kotlin Multiplatform does not support Java crypto packages, so the `MessageDigest` class is not available.
 * We have replaced the `MessageDigest` class with a simple string hashing function.
 */

package xyz.mcxross.graphql.client.extensions

import xyz.mcxross.graphql.client.types.AutomaticPersistedQueriesExtension
import xyz.mcxross.graphql.client.types.GraphQLClientRequest

fun GraphQLClientRequest<*>.getQueryId(): String {

  return "String.format(BigInteger(1, messageDigest.digest(this.query?.toByteArray(StandardCharsets.UTF_8)))"
}

fun AutomaticPersistedQueriesExtension.toQueryParamString(): String =
  """{"persistedQuery":{"version":$version,"sha256Hash":"$sha256Hash"}}"""

fun AutomaticPersistedQueriesExtension.toExtensionsBodyMap(): Map<String, Map<String, Any>> =
  mapOf("persistedQuery" to mapOf("version" to version, "sha256Hash" to sha256Hash))
