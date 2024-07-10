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
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.errors.*
import xyz.mcxross.graphql.client.core.client
import xyz.mcxross.graphql.client.types.GraphQLClient
import xyz.mcxross.graphql.client.types.GraphQLClientRequest
import xyz.mcxross.graphql.client.types.KotlinxGraphQLResponse

/** A lightweight typesafe GraphQL HTTP client using Ktor HTTP client engine. */
open class DefaultGraphQLClient(val url: String) : GraphQLClient<HttpRequestBuilder>, Closeable {

  suspend inline fun <reified T : Any> execute(
    request: GraphQLClientRequest<T>
  ): KotlinxGraphQLResponse<T> {
    return try {
      val rawResult: HttpResponse =
        client.post(url) {
          contentType(ContentType.Application.Json)
          expectSuccess = true
          setBody(request)
        }

      if (rawResult.status.isSuccess()) {
        rawResult.body()
      } else {
        throw Exception("HTTP error: ${rawResult.status}")
      }
    } catch (e: ClientRequestException) {
      throw e
    } catch (e: ServerResponseException) {
      throw e
    } catch (e: IOException) {
      throw e
    } catch (e: Exception) {
      throw e
    }
  }

  override fun close() {
    client.close()
  }
}
