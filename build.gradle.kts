extra["isReleaseVersion"] = !version.toString().endsWith("SNAPSHOT")

plugins {
    kotlin("multiplatform") apply false
    kotlin("jvm") apply false
    kotlin("plugin.serialization") apply false
    kotlin("android") apply false
    id("com.android.application") apply false
    id("com.android.library") apply false
}

group = "xyz.mcxross.graphql"
version = "1.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
        google()
    }
}
