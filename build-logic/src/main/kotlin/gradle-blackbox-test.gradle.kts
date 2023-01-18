plugins {
    java
}


repositories {
    ivy {
        setUrl("https://github.com/")
        patternLayout {
            artifact("/[organisation]/[module]/archive/[revision].zip")
        }
        content {
            onlyForConfigurations("externalProject")
        }
        metadataSources { artifact() }
    }
}

val config = configurations.create("externalProject")
val externalProjectDir = project.layout.buildDirectory.dir("external")
val externalProjectTask = tasks.register<Sync>("downloadExternalProjects") {
    from(config.map { zipTree(it) })
    into(externalProjectDir)
}

testing {
    suites {
        register<JvmTestSuite>("blackboxTest") {
            dependencies {
                implementation(gradleTestKit())
            }
            targets {
                all {
                    testTask.configure {
                        dependsOn(externalProjectTask)
                        inputs.dir(externalProjectDir)
                            .withPathSensitivity(PathSensitivity.RELATIVE)
                            .withPropertyName("External projects")
                        javaLauncher.set(javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(11)) })
                    }
                }
            }
        }
    }
}
