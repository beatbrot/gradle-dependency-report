@file:Suppress("UnstableApiUsage")

rootProject.name = "gradle-dependency-report"

pluginManagement {
    includeBuild("./build-logic")
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id("com.gradle.enterprise") version ("3.12.2")
     id("org.gradle.toolchains.foojay-resolver-convention") version ("0.4.0")
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

includeBuild("example")
