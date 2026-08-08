pluginManagement {
  // Resolve the Flutter SDK path from the machine-specific local.properties.
  // This is how the Flutter Gradle plugin locates the SDK for the flutter_test_host module.
  val flutterSdkPath = run {
    val properties = java.util.Properties()
    file("local.properties").inputStream().use { properties.load(it) }
    val flutterSdkPath = properties.getProperty("flutter.sdk")
    requireNotNull(flutterSdkPath) { "flutter.sdk not set in local.properties" }
    flutterSdkPath
  }
  includeBuild("$flutterSdkPath/packages/flutter_tools/gradle")

  repositories {
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
  id("dev.flutter.flutter-plugin-loader") version "1.0.0"
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "ScreenPilot"

include(":app")
// Flutter embedding project for the flutter_test_host module (Phase 3.1).
include(":flutter")
project(":flutter").projectDir = file("flutter_test_host/.android/Flutter")
