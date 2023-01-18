plugins {
    `java-gradle-plugin`
    id("java-convention")
    id("gradle-blackbox-test")
    id("com.gradle.plugin-publish") version "1.1.0"
    signing
}

group = "io.github.beatbrot"
version = "1.0-SNAPSHOT"

gradlePlugin {
    plugins {
        register("dependencyUpdates") {
            id = "io.github.beatbrot.dependency-report"
            implementationClass = "io.github.beatbrot.dependencyreport.DependencyUpdatesPlugin"
            displayName = "Dependency Updates"
            description = "Show available dependency updates"
        }
    }
    testSourceSet(java.sourceSets.getByName("blackboxTest"))
}

dependencies {
    compileOnly(libs.immutables)
    annotationProcessor(libs.immutables)

    testImplementation(gradleTestKit())
    testImplementation(libs.test.archunit)
    testImplementation(libs.test.mockserver)

    blackboxTestImplementation("com.fasterxml.jackson.core:jackson-core:2.14.1")
    blackboxTestImplementation("org.assertj:assertj-core:3.24.0")
    externalProject("jjohannes:idiomatic-gradle:master")
    externalProject("square:okhttp:master")
    externalProject("kotlin:kotlinx-atomicfu:0.19.0")
    externalProject("android:nowinandroid:main")
}

signing {
    useGpgCmd()
}
