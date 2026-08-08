pluginManagement {
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

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
    // Local Maven repo produced by `flutter build aar` for the flutter_test_host
    // module (Phase 3.1). The host consumes the Flutter embedding as a prebuilt AAR,
    // so no Flutter Gradle plugin is required in this build.
    maven { url = uri("flutter_test_host/build/host/outputs/repo") }
    // Flutter engine/embedding artifacts referenced transitively by the generated AAR POM.
    maven { url = uri("https://storage.googleapis.com/download.flutter.io") }
  }
}

rootProject.name = "ScreenPilot"

include(":app")
