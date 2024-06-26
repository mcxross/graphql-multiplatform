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

package xyz.mcxross.graphql.client.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import xyz.mcxross.graphql.client.types.OptionalInput

/** KSerializer that can serialize/deserialize optional scalar values. */
object OptionalScalarSerializer : KSerializer<OptionalInput<Any>> {

  override val descriptor: SerialDescriptor = buildClassSerialDescriptor("OptionalScalar")

  override fun serialize(encoder: Encoder, value: OptionalInput<Any>) {
    when (value) {
      is OptionalInput.Undefined -> {
        return
      }
      is OptionalInput.Defined<*> -> {
        AnyKSerializer.serialize(encoder, value.value)
      }
    }
  }

  // undefined is only supported during client serialization
  override fun deserialize(decoder: Decoder): OptionalInput<Any> =
    OptionalInput.Defined(AnyKSerializer.deserialize(decoder))
}
