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

package xyz.mcxross.graphql.client.serializer

import kotlin.reflect.KClass
import xyz.mcxross.graphql.client.types.GraphQLClientRequest
import xyz.mcxross.graphql.client.types.GraphQLClientResponse

/** GraphQL request/response serializer. */
interface GraphQLClientSerializer {

  /** Serialize GraphQLClientRequest (or batch request) to a raw String representation. */
  fun serialize(request: GraphQLClientRequest<*>): String

  /** Serialize GraphQLClientRequest (or batch request) to a raw String representation. */
  fun serialize(requests: List<GraphQLClientRequest<*>>): String

  /** Deserialize raw response String to a target GraphQLClientResponse. */
  fun <T : Any> deserialize(rawResponse: String, responseType: KClass<T>): GraphQLClientResponse<T>

  /** Deserialize raw response String to a list of GraphQLClientResponses. */
  fun deserialize(
    rawResponses: String,
    responseTypes: List<KClass<*>>,
  ): List<GraphQLClientResponse<*>>
}
