import java.util.*

plugins {
  id("com.android.library")
  kotlin("multiplatform")
  alias(libs.plugins.kotlin.serialization)
  id("maven-publish")
  id("signing")
}

group = "xyz.mcxross.graphql.client"

version = "0.1.0-beta02"

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

ext["sonatypeUser"] = null

ext["sonatypePass"] = null

ext["signing.secretKeyRingFile"] = null

ext["signing.password"] = null

ext["signing.keyId"] = null

ext["keyId"] = null

ext["keyPassword"] = null

ext["signingKey"] = null

val secretPropsFile = project.rootProject.file("local.properties")

fun getProperty(name: String): String? {
  return System.getenv(name) ?: project.findProperty(name) as String?
}

fun extraProperty(name: String): String {
  return extra[name] as String
}

fun isFilePathProperty(name: String): Boolean {
  return getProperty(name)?.let { File(it).exists() } ?: false
}

fun loadFileContents(name: String): String {
  return getProperty(name)?.let { File(it).readText() } ?: ""
}

if (secretPropsFile.exists()) {
  secretPropsFile
    .reader()
    .use { Properties().apply { load(it) } }
    .onEach { (name, value) -> ext[name.toString()] = value }
} else {
  ext["sonatypeUser"] = getProperty("OSSRH_USERNAME")
  ext["sonatypePass"] = getProperty("OSSRH_PASSWORD")
  ext["keyId"] = getProperty("GPG_KEY_ID")
  ext["keyPassword"] = getProperty("GPG_KEY_PASSWORD")
  ext["signingKey"] = getProperty("SIGNING_KEY")
}

publishing {
  if (hasProperty("sonatypeUser") && hasProperty("sonatypePass")) {
    repositories {
      maven {
        name = "sonatype"
        val isSnapshot = version.toString().endsWith("-SNAPSHOT")
        setUrl(
          if (isSnapshot) {
            "https://s01.oss.sonatype.org/content/repositories/snapshots/"
          } else {
            "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
          }
        )
        credentials {
          username = extraProperty("sonatypeUser")
          password = extraProperty("sonatypePass")
        }
      }
    }
  }

  publications.withType<MavenPublication> {
    pom {
      name.set("GraphQL Multiplatform Client")
      description.set(
        "A multiplatform GraphQL client for Kotlin, supporting Android, iOS, macOS, tvOS, watchOS, Linux, Windows, and the web."
      )
      url.set("https://github.com/mcxross")

      licenses {
        license {
          name.set("Apache License, Version 2.0")
          url.set("https://opensource.org/licenses/APACHE-2.0")
        }
      }
      developers {
        developer {
          id.set("mcxross")
          name.set("Mcxross")
          email.set("oss@mcxross.xyz")
        }
      }
      scm { url.set("https://github.com/mcxross/graphql-multiplatform") }
    }
  }
}

tasks.withType<Sign>().configureEach {
  onlyIf("isReleaseVersion is set") { project.extra["isReleaseVersion"] as Boolean }
}

signing {
  val sonatypeGpgKey = getProperty("signingKey")
  val sonatypeGpgKeyPassword = getProperty("keyPassword")
  useGpgCmd()

  configurations.forEach {
    sign(it)
  }
}
