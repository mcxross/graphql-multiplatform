# GraphQL Kotlin Multiplatform

GraphQL Kotlin Multiplatform is a GraphQL client for Kotlin Multiplatform. It is a multiplatform library that allows you
to interact with a GraphQL server from Kotlin code.
It is built on top of [graphql-kotlin](https://github.com/ExpediaGroup/graphql-kotlin).

Significant changes have been made to the original library to make it work on Kotlin Multiplatform. This included
providing Ktor client support for all platforms and utilization of only multiplatform libraries.

[![Kotlin Version](https://img.shields.io/badge/Kotlin-1.9.23-B125EA?logo=kotlin)](https://kotlinlang.org)

[![Kotlin Version](https://img.shields.io/badge/Kotlin-v1.9.23-B125EA?logo=kotlin)](https://kotlinlang.org)
[![Maven Central](https://img.shields.io/maven-central/v/xyz.mcxross.graphql.client/graphql-multiplatform-client)](https://central.sonatype.com/artifact/xyz.mcxross.graphql.client/graphql-multiplatform-client)
![badge-android](http://img.shields.io/badge/Platform-Android-brightgreen.svg?logo=android)
![badge-ios](http://img.shields.io/badge/Platform-iOS-orange.svg?logo=apple)
![badge-js](http://img.shields.io/badge/Platform-NodeJS-yellow.svg?logo=javascript)
![badge-jvm](http://img.shields.io/badge/Platform-JVM-red.svg?logo=openjdk)
![badge-linux](http://img.shields.io/badge/Platform-Linux-lightgrey.svg?logo=linux)
![badge-macos](http://img.shields.io/badge/Platform-macOS-orange.svg?logo=apple)
![badge-windows](http://img.shields.io/badge/Platform-Windows-blue.svg?logo=windows)

# Table of contents

- [Features](#features)
- [Quick start](#quick-start)
- [What's included](#whats-included)
- [Contribution](#contribution)
- [License](#license)

## Features

- Multiplatform
- Uses Ktor as the underlying HTTP client
- Type-safe
- Client Configurable

## Quick Start

### Installation

GraphQL Multiplatform is available
on [Maven Central](https://search.maven.org/artifact/com.apurebase/graphql-kotlin-multiplatform)
as a Kotlin Multiplatform library. You can either add it to your multiplatform project as shown below:

```kotlin
commonMain.dependencies {
    implementation("xyz.mcxross.graphql:graphql-multiplatform.client")
}
```

Then apply the plugin to your project:

```kotlin
plugins {
    id("xyz.mcxross.graphql-multiplatform")
}
```

> :warning: **Note:** Currently, only Kotlin Multiplatform projects are supported.

### Usage

Applying the plugin to your project will generate a `graphql` block in your `build.gradle.kts` file. You can configure
the client by setting the `endpoint` and `packageName` properties.

```kotlin
graphql {
    client {
        endpoint = "https://api.example.com/graphql"
        packageName = "com.example.graphql"
    }
}
```

## What's included

- `Client` - A multiplatform GraphQL client that allows you to interact with a GraphQL server
- `Plugin` - A Gradle plugin that generates Kotlin code from your GraphQL schema

## Contribution

We welcome contributions to GraphQL Kotlin Multiplatform. Please refer to the [contribution guide](CONTRIBUTING.md) for
more information.

## License

    Copyright 2024 McXross

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.