package io.github.beatbrot.dependencyreport.integration

import io.github.beatbrot.dependencyreport.DependencyReportPlugin
import io.github.beatbrot.dependencyreport.internal.analysis.AnalyzeDependenciesTask
import org.gradle.testkit.runner.TaskOutcome

import java.nio.file.Files
import java.nio.file.Path

class MultiProjectTest extends GradleSpecification {

    def "Multiple projects without dependencies"() {
        setup:
        buildFile << PLUGINS_BLOCK
        createSubproject("a")
        createSubproject("b")
        when:
        def result = gradleRunner()
                .withArguments("dependencyReport")
                .build()
        then:
        !result.output.contains(DependencyReportPlugin.PLUGIN_SHOULD_BE_APPLIED_TO_ROOT)
        result.task(":${AnalyzeDependenciesTask.NAME}").outcome == TaskOutcome.SUCCESS
        result.task(":a:${AnalyzeDependenciesTask.NAME}").outcome == TaskOutcome.SUCCESS
        result.task(":b:${AnalyzeDependenciesTask.NAME}").outcome == TaskOutcome.SUCCESS
    }

    def "Plugin is applied to non-root project"() {
        setup:
        createSubproject("a").resolve("build.gradle") << PLUGINS_BLOCK
        createSubproject("b")
        when:
        def result = gradleRunner().build()
        then:
        result.output.contains(DependencyReportPlugin.PLUGIN_SHOULD_BE_APPLIED_TO_ROOT)
    }

    private Path createSubproject(String name) {
        def result = testProjectDir.toPath().resolve(name)
        Files.createDirectory(result)
        settingsFile << "\ninclude(\"$name\")\n"
        return result
    }
}
