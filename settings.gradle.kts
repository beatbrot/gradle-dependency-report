@file:Suppress("UnstableApiUsage")

rootProject.name = "gradle-dependency-report"

pluginManagement {
    includeBuild("./build-logic")
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id("com.gradle.develocity") version ("4.3.1")
    id("com.gradle.common-custom-user-data-gradle-plugin") version ("2.4.0")
    id("org.gradle.toolchains.foojay-resolver-convention") version ("1.0.0")
}

develocity {
    buildScan {
        capture.fileFingerprints = true
    }
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
