@file:Suppress("UnstableApiUsage")

rootProject.name = "gradle-dependency-report"

pluginManagement {
    includeBuild("./build-logic")
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id("com.gradle.develocity") version ("3.17.2")
    id("com.gradle.common-custom-user-data-gradle-plugin") version ("2.0.1")
    id("org.gradle.toolchains.foojay-resolver-convention") version ("0.8.0")
}

develocity {
    buildScan {
        capture.fileFingerprints = true
        termsOfUseUrl = "https://gradle.com/terms-of-service"
        termsOfUseAgree = System.getenv("GRADLE_TOS_AGREE")
    }
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
