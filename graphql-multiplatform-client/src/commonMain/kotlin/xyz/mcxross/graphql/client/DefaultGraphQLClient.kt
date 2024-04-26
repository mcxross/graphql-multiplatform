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
 * - To be Kotlin pure, we've replaced the serializer interface with a concrete implementation.
 */

package xyz.mcxross.graphql.client

import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import xyz.mcxross.graphql.client.types.AutomaticPersistedQueriesSettings
import xyz.mcxross.graphql.client.types.GraphQLClient
import xyz.mcxross.graphql.client.types.GraphQLClientRequest
import xyz.mcxross.graphql.client.types.GraphQLClientResponse
import xyz.mcxross.graphql.client.types.KotlinxGraphQLResponse

/** A lightweight typesafe GraphQL HTTP client using Ktor HTTP client engine. */
open class DefaultGraphQLClient(
  val url: String,
  override val automaticPersistedQueriesSettings: AutomaticPersistedQueriesSettings =
    AutomaticPersistedQueriesSettings(),
) : GraphQLClient<HttpRequestBuilder>, Closeable {

  suspend inline fun <reified T : Any> execute(
    request: GraphQLClientRequest<T>
  ): KotlinxGraphQLResponse<T> {
    val rawResult =
      client.post(url) {
        contentType(ContentType.Application.Json)
        expectSuccess = true
        setBody(request)
      }

    return rawResult.body()
  }

  override suspend fun <T : Any> execute(
    request: GraphQLClientRequest<T>,
    requestCustomizer: HttpRequestBuilder.() -> Unit,
  ): GraphQLClientResponse<T> {
    /*
     return if (automaticPersistedQueriesSettings.enabled) {
       val queryId = request.getQueryId()
       val automaticPersistedQueriesExtension =
         AutomaticPersistedQueriesExtension(
           version = AutomaticPersistedQueriesSettings.VERSION,
           sha256Hash = queryId,
         )
       val extensions =
         request.extensions?.let {
           automaticPersistedQueriesExtension.toExtensionsBodyMap().plus(it)
         } ?: automaticPersistedQueriesExtension.toExtensionsBodyMap()

       val apqRawResultWithoutQuery: String =
         when (automaticPersistedQueriesSettings.httpMethod) {
           is AutomaticPersistedQueriesSettings.HttpMethod.GET -> {
             httpClient
               .get(url) {
                 expectSuccess = true
                 header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded)
                 accept(ContentType.Application.Json)
                 url {
                   parameters.append(
                     "extension",
                     automaticPersistedQueriesExtension.toQueryParamString(),
                   )
                 }
               }
               .body()
           }
           is AutomaticPersistedQueriesSettings.HttpMethod.POST -> {
             val requestWithoutQuery =
               object : GraphQLClientRequest<T> by request {
                 override val query = null
                 override val extensions = extensions
               }
             httpClient
               .post(url) {
                 expectSuccess = true
                 apply(requestCustomizer)
                 accept(ContentType.Application.Json)
                 setBody(
                   TextContent(
                     serializer.serialize(requestWithoutQuery),
                     ContentType.Application.Json,
                   )
                 )
               }
               .body()
           }
         }

       serializer.deserialize(apqRawResultWithoutQuery, request.responseType()).let {
         if (it.errors.isNullOrEmpty() && it.data != null) return it
       }

       val apqRawResultWithQuery: String =
         when (automaticPersistedQueriesSettings.httpMethod) {
           is AutomaticPersistedQueriesSettings.HttpMethod.GET -> {
             httpClient
               .get(url) {
                 expectSuccess = true
                 header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded)
                 accept(ContentType.Application.Json)
                 url {
                   parameters.append("query", serializer.serialize(request))
                   parameters.append(
                     "extension",
                     automaticPersistedQueriesExtension.toQueryParamString(),
                   )
                 }
               }
               .body()
           }
           is AutomaticPersistedQueriesSettings.HttpMethod.POST -> {
             val requestWithQuery =
               object : GraphQLClientRequest<T> by request {
                 override val extensions = extensions
               }
             httpClient
               .post(url) {
                 expectSuccess = true
                 apply(requestCustomizer)
                 accept(ContentType.Application.Json)
                 setBody(
                   TextContent(serializer.serialize(requestWithQuery), ContentType.Application.Json)
                 )
               }
               .body()
           }
         }

       serializer.deserialize(apqRawResultWithQuery, request.responseType())
     } else {

       val rawResult: String = client
         .post(url) {
           expectSuccess = true
           apply(requestCustomizer)
           setBody(serializer.serialize(request))
         }
         .body()
       serializer.deserialize(rawResult, request.responseType())
    }*/
    return client
      .post(url) {
        expectSuccess = true
        apply(requestCustomizer)
        setBody("")
      }
      .body()
  }

  override suspend fun execute(
    requests: List<GraphQLClientRequest<*>>,
    requestCustomizer: HttpRequestBuilder.() -> Unit,
  ): List<GraphQLClientResponse<*>> {
    return client
      .post(url) {
        expectSuccess = true
        apply(requestCustomizer)
        setBody(requests)
      }
      .body()
    // return serializer.deserialize(rawResult, requests.map { it.responseType() })
  }

  override fun close() {
    client.close()
  }
}
