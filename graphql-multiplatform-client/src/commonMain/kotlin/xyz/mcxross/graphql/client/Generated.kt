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

package xyz.mcxross.graphql.client

/**
 * Annotation to automatically exclude auto-generated client code from JaCoCo reports.
 *
 * Starting with JaCoCo 8.3, classes and methods annotated with `@Generated` annotation that has
 * RUNTIME retention will be excluded from code coverage report. We are using custom annotation
 * instead of `javax.annotation.Generated` or `javax.annotation.processing.Generated` as their
 * retention policy is just SOURCE.
 */
@Target(allowedTargets = [AnnotationTarget.CLASS])
@Retention(value = AnnotationRetention.RUNTIME)
annotation class Generated
