pluginManagement {
    includeBuild("./..")
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id("io.github.beatbrot.dependency-report")
}
