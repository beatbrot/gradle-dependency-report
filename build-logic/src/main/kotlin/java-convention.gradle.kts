import java.nio.charset.StandardCharsets.UTF_8

plugins {
    java
    `maven-publish`
    groovy
    id("java-codestyle")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType(Test::class.java).configureEach {
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(8))
    })
}

dependencies {
    testRuntimeOnly(findDep(catalog, "test-byteBuddy")) {
        because("Spock requires it for mocks.")
    }
}

@Suppress("UnstableApiUsage")
testing {
    suites {
        named<JvmTestSuite>("test") {
            useSpock()
        }
    }
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
    ).asFile(UTF_8.name())
    inputs.file(spockConfig).withPropertyName("Spock config").withPathSensitivity(PathSensitivity.NONE)
    jvmArgumentProviders.add(CommandLineArgumentProvider { mutableListOf("-Dspock.configuration=${spockConfig}") })
}

tasks.withType(JavaCompile::class) {
    options.compilerArgs.add("-Werror")
    options.encoding = "UTF-8"
}

fun findDep(catalog: VersionCatalog, name: String): Provider<MinimalExternalModuleDependency> {
    return catalog.findLibrary(name).orElseThrow { throw NoSuchElementException("Cannot find dependency") }
}
