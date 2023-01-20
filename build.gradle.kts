plugins {
    `java-gradle-plugin`
    id("java-convention")
    id("gradle-blackbox-test")
    alias(libs.plugins.gradlePublish)
    signing
}

group = "io.github.beatbrot"
version = "1.0-SNAPSHOT"

@Suppress("UnstableApiUsage")
gradlePlugin {
    plugins {
        register("dependencyUpdates") {
            id = "io.github.beatbrot.dependency-report"
            implementationClass = "io.github.beatbrot.dependencyreport.DependencyUpdatesPlugin"
            displayName = "Dependency Updates"
            description = "Show available dependency updates"
            tags.set(listOf("dependencies", "versions", "updates"))
        }
    }
    website.set("https://github.com/beatbrot/gradle-dependency-report")
    vcsUrl.set("https://github.com/beatbrot/gradle-dependency-report.git")
    testSourceSet(java.sourceSets.getByName("blackboxTest"))
}

dependencies {
    compileOnly(libs.immutables)
    annotationProcessor(libs.immutables)

    testImplementation(gradleTestKit())
    testImplementation(libs.test.archunit)
    testImplementation(libs.test.mockserver)

    blackboxTestImplementation(libs.test.assertj)
    externalProject("jjohannes:idiomatic-gradle:master")
    externalProject("square:okhttp:master")
    externalProject("kotlin:kotlinx-atomicfu:0.19.0")
    externalProject("android:nowinandroid:main")
}

signing {
    useGpgCmd()
}
