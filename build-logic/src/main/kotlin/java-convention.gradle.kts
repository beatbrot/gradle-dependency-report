import java.nio.charset.StandardCharsets.UTF_8

plugins {
    java
    `maven-publish`
    groovy
    pmd
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
    testRuntimeOnly(findDep(catalog, "test-byteBuddy")) {
        because("Spock requires it for mocks.")
    }

    compileOnly(findDep(catalog, "spotbugsAnnotations"))
}

@Suppress("UnstableApiUsage")
testing {
    suites {
        named<JvmTestSuite>("test") {
            useSpock()
        }
    }
}

pmd {
    toolVersion = "6.54.0"
    ruleSets = listOf()
    //language=xml
    ruleSetConfig = project.resources.text.fromString("""
        <?xml version="1.0"?>
        <ruleset name="Custom Rules"
            xmlns="http://pmd.sourceforge.net/ruleset/2.0.0"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://pmd.sourceforge.net/ruleset/2.0.0 https://pmd.sourceforge.io/ruleset_2_0_0.xsd">

            <description>
                My custom rules
            </description>
            <rule ref='category/java/bestpractices.xml'/>
            <rule ref='category/java/performance.xml'/>
            <rule ref='category/java/errorprone.xml'>
                <exclude name='MissingSerialVersionUID'/>
            </rule>
        </ruleset>
    """.trimIndent())
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
