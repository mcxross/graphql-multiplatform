import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.dokka)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.vanniktech.maven.publish)
}

group = "xyz.mcxross.graphql.client"

version = "0.1.0-beta06"

extra["isReleaseVersion"] = !version.toString().endsWith("-SNAPSHOT")

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
  jvm { testRuns["test"].executionTask.configure { useJUnitPlatform() } }

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

    androidMain.dependencies { api(libs.ktor.client.okhttp) }

    jsMain.dependencies { api(libs.ktor.client.js) }

    jvmMain.dependencies { api(libs.ktor.client.cio) }

    appleMain.dependencies { api(libs.ktor.client.darwin) }

    linuxMain.dependencies { api(libs.ktor.client.curl) }

    mingwMain.dependencies { api(libs.ktor.client.curl) }
  }
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

android {
  namespace = "xyz.mcxross.graphql.client"
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

tasks.getByName<DokkaTask>("dokkaHtml") {
  moduleName.set("GraphQL Multiplatform Client")
  outputDirectory.set(file(buildDir.resolve("dokka")))
}

tasks.withType<DokkaTask>().configureEach {
  notCompatibleWithConfigurationCache("https://github.com/Kotlin/dokka/issues/2231")
}

mavenPublishing {
  coordinates("xyz.mcxross.graphql.client", "graphql-multiplatform-client", version.toString())

  configure(
    KotlinMultiplatform(
      javadocJar = JavadocJar.Dokka("dokkaHtml"),
      sourcesJar = true,
      androidVariantsToPublish = listOf("debug", "release"),
    )
  )

  pom {
    name.set("GraphQL Multiplatform Client")
    description.set(
      "A multiplatform GraphQL client for Kotlin, supporting Android, iOS, macOS, tvOS, watchOS, Linux, Windows, and the web."
    )
    inceptionYear.set("2023")
    url.set("https://github.com/mcxross")
    licenses {
      license {
        name.set("The Apache License, Version 2.0")
        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
        distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
      }
    }
    developers {
      developer {
        id.set("mcxross")
        name.set("Mcxross")
        email.set("oss@mcxross.xyz")
        url.set("https://mcxross.xyz/")
      }
    }
    scm {
      url.set("https://github.com/mcxross/graphql-multiplatform")
      connection.set("scm:git:ssh://github.com/mcxross/graphql-multiplatform.git")
      developerConnection.set("scm:git:ssh://github.com/mcxross/graphql-multiplatform.git")
    }
  }

  publishToMavenCentral(SonatypeHost.S01, automaticRelease = true)

  signAllPublications()
}
