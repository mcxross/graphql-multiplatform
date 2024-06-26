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

package xyz.mcxross.graphql.plugin.gradle.config

import java.io.Serializable

/** Timeout configuration for executing introspection query and downloading schema SDL. */
data class TimeoutConfiguration(
  /** Timeout in milliseconds to establish new connection. */
  var connect: Long = 5_000,
  /** Read timeout in milliseconds */
  var read: Long = 15_000,
) : Serializable
