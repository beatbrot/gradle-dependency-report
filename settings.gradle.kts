@file:Suppress("UnstableApiUsage")

rootProject.name = "gradle-dependency-report"

pluginManagement {
    includeBuild("./build-logic")
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id("com.gradle.enterprise") version ("3.14.1")
    id("com.gradle.common-custom-user-data-gradle-plugin") version ("1.11.2")
    id("org.gradle.toolchains.foojay-resolver-convention") version ("0.7.0")
}

gradleEnterprise {
    buildScan {
        capture {
            isTaskInputFiles = true
        }
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = System.getenv("GRADLE_TOS_AGREE")
    }
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
