plugins {
    `java-gradle-plugin`
    id("java-convention")
    id("gradle-blackbox-test")
    alias(this@Build_gradle.libs.plugins.gradlePublish)
    signing
}

group = "io.github.beatbrot"
version = "1.0-SNAPSHOT"

@Suppress("UnstableApiUsage")
gradlePlugin {
    plugins {
        register("dependencyUpdates") {
            id = "io.github.beatbrot.dependency-report"
            implementationClass = "io.github.beatbrot.dependencyreport.DependencyReportPlugin"
            displayName = "Dependency Report"
            description = "Show available dependency updates"
            tags.set(listOf("dependencies", "versions", "updates", "report"))
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
    blackboxTestImplementation(libs.test.commonsIo)
    externalProject("jjohannes:idiomatic-gradle:master")
    externalProject("square:okhttp:master")
    externalProject("kotlin:kotlinx-atomicfu:0.19.0")
    externalProject("android:nowinandroid:main")
}

signing {
    useGpgCmd()
}
