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
}

gradleEnterprise {
    buildScan {
        capture {
            isTaskInputFiles = true
        }
    }
}
