extra["isReleaseVersion"] = !version.toString().endsWith("SNAPSHOT")

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.application) apply false
}

group = "xyz.mcxross.graphql"
version = "0.1.0-beta06"

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
        google()
    }
}
