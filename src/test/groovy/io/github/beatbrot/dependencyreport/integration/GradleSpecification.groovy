package io.github.beatbrot.dependencyreport.integration

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.charset.StandardCharsets

abstract class GradleSpecification extends Specification {

    static final String PLUGINS_BLOCK = """
plugins {
    id("io.github.beatbrot.dependency-report")
}
"""

    @TempDir
    File testProjectDir
    File settingsFile
    File buildFile
    File propertiesFile

    def setup() {
        settingsFile = new File(testProjectDir, "settings.gradle")
        buildFile = new File(testProjectDir, 'build.gradle')
        propertiesFile = new File(testProjectDir, 'gradle.properties')
        propertiesFile << "org.gradle.warning.mode=fail"
    }

    protected GradleRunner gradleRunner(boolean stableConfigCache = true) {
        if (stableConfigCache) {
            if (!settingsFile.exists() || !settingsFile.getText(StandardCharsets.UTF_8.name()).contains("STABLE_CONFIGURATION_CACHE")) {
                settingsFile << "\nenableFeaturePreview(\"STABLE_CONFIGURATION_CACHE\")\n"
            }
        }
        return GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withPluginClasspath()
    }
}
