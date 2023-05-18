plugins {
    java
    id("net.ltgt.errorprone")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    errorprone(findBundle(catalog, "build-errorprone"))
}

tasks.withType<JavaCompile>().configureEach {
    val opts = (options as ExtensionAware).extensions.getByType<net.ltgt.gradle.errorprone.ErrorProneOptions>()
    if (name == "compileJava") {
        opts.apply {
            disableWarningsInGeneratedCode.set(true)
            // Recommended in Gradle
            disable("InjectOnConstructorOfAbstractClass")
            option("NullAway:AnnotatedPackages", "io.github.beatbrot")
        }
    } else {
        opts.isEnabled.set(false)
    }
}

fun findBundle(catalog: VersionCatalog, name: String) =
    catalog.findBundle(name).orElseThrow { throw NoSuchElementException("Cannot find dependency") }
