plugins {
    id("com.android.library")
    kotlin("multiplatform")
    alias(libs.plugins.kotlin.serialization)
    id("maven-publish")
}

group = "xyz.mcxross.graphql.client"
version = "1.0-SNAPSHOT"

kotlin {
    androidTarget { publishLibraryVariants("release", "debug") }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    js {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                    useSafari()
                }
            }
        }
        nodejs()
        compilations.configureEach {
            kotlinOptions.sourceMap = true
            kotlinOptions.moduleKind = "umd"
        }
    }
    jvm {
        testRuns["test"].executionTask.configure { useJUnitPlatform() }
    }

    linuxX64()
    macosArm64()
    macosX64()
    tvosX64()
    tvosArm64()
    watchosX64()
    watchosArm32()
    watchosArm64()
    mingwX64()

    applyDefaultHierarchyTemplate()

    sourceSets {

        commonMain.dependencies {
            api(libs.ktor.client.core)
            implementation(libs.uri.kmp)
            api(libs.ktor.client.serialization)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlin.reflect)
            implementation(libs.stately.concurrent.collections)
        }

        androidMain.dependencies {
            api(libs.ktor.client.okhttp)
        }

        jsMain.dependencies {
            api(libs.ktor.client.js)
        }

        jvmMain.dependencies {
            api(libs.ktor.client.cio)
        }

        appleMain.dependencies {
            api(libs.ktor.client.darwin)
        }

        linuxMain.dependencies {
            api(libs.ktor.client.curl)
        }

        mingwMain.dependencies {
            api(libs.ktor.client.curl)
        }
    }
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))


android {
    namespace = "mcxross.graphql"
    defaultConfig {
        minSdk = 24
        compileSdk = 33
    }

    sourceSets {
        named("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            res.srcDirs("src/androidMain/res", "src/commonMain/resources")
        }
    }

}
