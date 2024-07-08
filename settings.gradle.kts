pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "graphql-multiplatform"

include(":graphql-multiplatform-client")
include(":graphql-multiplatform-plugin")
