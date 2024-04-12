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

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.*
import io.ktor.http.content.TextContent
import io.ktor.utils.io.core.*
import xyz.mcxross.graphql.client.extensions.getQueryId
import xyz.mcxross.graphql.client.extensions.toExtensionsBodyMap
import xyz.mcxross.graphql.client.extensions.toQueryParamString
import xyz.mcxross.graphql.client.serializer.GraphQLClientKotlinxSerializer
import xyz.mcxross.graphql.client.serializer.GraphQLClientSerializer
import xyz.mcxross.graphql.client.types.*

/** A lightweight typesafe GraphQL HTTP client using Ktor HTTP client engine. */
open class GraphQLKtorClient(
  private val url: String,
  private val httpClient: HttpClient = HttpClient(defaultEngine),
  private val serializer: GraphQLClientSerializer = GraphQLClientKotlinxSerializer(),
  override val automaticPersistedQueriesSettings: AutomaticPersistedQueriesSettings =
    AutomaticPersistedQueriesSettings(),
) : GraphQLClient<HttpRequestBuilder>, Closeable {

  override suspend fun <T : Any> execute(
    request: GraphQLClientRequest<T>,
    requestCustomizer: HttpRequestBuilder.() -> Unit,
  ): GraphQLClientResponse<T> {
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
      val rawResult: String =
        httpClient
          .post(url) {
            expectSuccess = true
            apply(requestCustomizer)
            setBody(TextContent(serializer.serialize(request), ContentType.Application.Json))
          }
          .body()
      serializer.deserialize(rawResult, request.responseType())
    }
  }

  override suspend fun execute(
    requests: List<GraphQLClientRequest<*>>,
    requestCustomizer: HttpRequestBuilder.() -> Unit,
  ): List<GraphQLClientResponse<*>> {
    val rawResult: String =
      httpClient
        .post(url) {
          expectSuccess = true
          apply(requestCustomizer)
          setBody(TextContent(serializer.serialize(requests), ContentType.Application.Json))
        }
        .body()
    return serializer.deserialize(rawResult, requests.map { it.responseType() })
  }

  override fun close() {
    httpClient.close()
  }
}
