description =
    "Gradle Kotlin Gradle Plugin that can generate type-safe GraphQL Kotlin client and GraphQL schema in SDL format using reflections"

plugins {
  kotlin("jvm")
  `java-library`
  `java-gradle-plugin`
  alias(libs.plugins.plugin.publish)
}

dependencies {
  api(project(":graphql-multiplatform-client"))
  implementation(libs.kotlin.gradle.api)
  implementation(libs.ktor.client.apache)
  implementation(libs.ktor.client.content)
  implementation(libs.jackson)

  implementation(libs.ktor.serialization.jackson) {
    exclude(group = "com.fasterxml.jackson.core", module = "jackson-databind")
    exclude(group = "com.fasterxml.jackson.module", module = "jackson-module-kotlin")
  }

  api(libs.graphql.java) { exclude(group = "com.graphql-java", module = "java-dataloader") }
  api(libs.poet)
  api(libs.kotlinx.serialization.json)

  compileOnly(libs.android.plugin)
  compileOnly(libs.graalvm.plugin)
}

java {
  withSourcesJar()
  if (rootProject.extra["isReleaseVersion"] as Boolean) {
    withJavadocJar()
  }
}

gradlePlugin {
  website.set("https://opensource.expediagroup.com/graphql-kotlin/docs/")
  vcsUrl.set("https://github.com/mcxross/graphql-multiplatform")
  plugins {
    register("graphQLPlugin") {
      id = "xyz.mcxross.graphql"
      displayName = "GraphQL Kotlin Gradle Plugin"
      description =
          "Gradle Plugin that can generate type-safe GraphQL Kotlin client and GraphQL schema in SDL format using reflections"
      implementationClass = "xyz.mcxross.graphql.plugin.gradle.GraphQLGradlePlugin"
      tags.set(listOf("graphql", "kotlin", "graphql-client", "schema-generator", "sdl"))
    }
  }
}

val generateDefaultVersion by
    tasks.registering {
      val fileName = "PluginVersion.kt"
      val defaultVersionFile =
          File("$buildDir/generated/src/xyz/mcxross/graphql/plugin/gradle", fileName)

      inputs.property(fileName, project.version)
      outputs.dir(defaultVersionFile.parent)

      doFirst {
        defaultVersionFile.parentFile.mkdirs()
        defaultVersionFile.writeText(
            """
                package xyz.mcxross.graphql.plugin.gradle
                internal const val DEFAULT_PLUGIN_VERSION = "${project.version}"

            """
                .trimIndent())
      }
    }

sourceSets { main { java { srcDir(generateDefaultVersion) } } }

publishing {
  repositories {
    maven {
      name = "localPluginRepository"
      url = uri("../../repo")
    }
  }
}
