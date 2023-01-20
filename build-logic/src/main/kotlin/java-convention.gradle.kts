import com.github.spotbugs.snom.SpotBugsTask
import java.nio.charset.StandardCharsets

plugins {
    java
    `maven-publish`
    groovy
    id("com.github.spotbugs-base")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    testImplementation(findDep(catalog, "test-spock"))
    testRuntimeOnly(findDep(catalog, "test-byteBuddy")) {
        because("Spock requires it for mocks.")
    }
    testRuntimeOnly(findDep(catalog, "test-junitEngine"))

    compileOnly(findDep(catalog, "spotbugsAnnotations"))
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    // language=groovy
    val spockConfig = project.resources.text.fromString(
        """
        runner {
            parallel {
                enabled true
            }
        }
    """.trimIndent()
    ).asFile(StandardCharsets.UTF_8.name())
    inputs.file(spockConfig).withPropertyName("Spock config").withPathSensitivity(PathSensitivity.NONE)
    jvmArgumentProviders.add(object : CommandLineArgumentProvider {
        override fun asArguments(): MutableIterable<String> {
            return mutableListOf("-Dspock.configuration=${spockConfig}")
        }
    })
}

spotbugs {
    // language=xml
    val excluded = project.resources.text.fromString(
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <FindBugsFilter>
            <Match>
                <Bug code="EI,EI2"/>
            </Match>
        </FindBugsFilter>
    """.trimIndent()
    )
    excludeFilter.set(excluded.asFile(StandardCharsets.UTF_8.name()))
}

val main = sourceSets["main"]!!
val spotbugsTask = tasks.register<SpotBugsTask>(main.getTaskName("spotbugs", null)) {
    description = "Run SpotBugs analysis for the source set main"
    sourceDirs = main.allSource.sourceDirectories
    classDirs = main.output
    auxClassPaths = main.compileClasspath

    reports {
        create("html") {
            required.set(true)
        }
    }
}

tasks.named(JavaBasePlugin.CHECK_TASK_NAME).configure {
    dependsOn(spotbugsTask)
}

tasks.withType(JavaCompile::class) {
    options.compilerArgs.add("-Werror")
    options.encoding = "UTF-8"
}

fun findDep(catalog: VersionCatalog, name: String): Provider<MinimalExternalModuleDependency> {
    return catalog.findLibrary(name).orElseThrow { throw NoSuchElementException("Cannot find dependency") }
}
