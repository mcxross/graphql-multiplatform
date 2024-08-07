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

package xyz.mcxross.graphql.client.types

import kotlinx.serialization.Serializable
import xyz.mcxross.graphql.client.exception.GraphQLClientError

/**
 * GraphQL response that is spec complaint with serialization and deserialization.
 *
 * @see [GraphQL Specification](http://spec.graphql.org/June2018/#sec-Data) for additional details
 */
@Serializable
abstract class GraphQLClientResponse<T> {
  abstract val data: T?
  abstract val errors: List<GraphQLClientError>?
  abstract val extensions: Map<String, Any?>?
}
