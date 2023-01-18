package io.github.beatbrot.dependencyreport.integration

import io.github.beatbrot.dependencyreport.DependencyReportTask
import io.github.beatbrot.dependencyreport.internal.Serialization
import io.github.beatbrot.dependencyreport.internal.analysis.AnalyzeDependenciesTask
import io.github.beatbrot.dependencyreport.internal.gradle.GradleVersionReport
import io.github.beatbrot.dependencyreport.internal.gradle.GradleVersionTask
import org.gradle.testkit.runner.TaskOutcome

class BasicTests extends GradleSpecification {

    def "Plugin can be applied"() {
        given:
        buildFile << PLUGINS_BLOCK

        when:
        gradleRunner().build()

        then:
        noExceptionThrown()
    }

    def "Run report task"() {
        given:
        buildFile << PLUGINS_BLOCK

        when:
        def result = gradleRunner().withArguments("depRepo").build()
        then:
        result.task(":${AnalyzeDependenciesTask.NAME}").outcome == TaskOutcome.SUCCESS
        result.task(":${DependencyReportTask.NAME}").outcome == TaskOutcome.SUCCESS
    }

    def "Gradle Task is disabled"() {
        given:
        buildFile << PLUGINS_BLOCK

        when:
        def result = gradleRunner().withArguments(DependencyReportTask.NAME, "--no-gradle-check", "-s").build()
        then:
        result.task(":${GradleVersionTask.NAME}") == null
        result.task(":${AnalyzeDependenciesTask.NAME}").outcome == TaskOutcome.SUCCESS
        result.task(":${DependencyReportTask.NAME}").outcome == TaskOutcome.SUCCESS
    }

    def "Gradle Update available"() {
        given:
        buildFile << PLUGINS_BLOCK

        when:
        def result = gradleRunner(false)
                .withGradleVersion("6.0")
                .withArguments(GradleVersionTask.NAME)
                .build()

        then:
        result.task(":${GradleVersionTask.NAME}").outcome == TaskOutcome.SUCCESS
        def gradleFile = new File(testProjectDir, "build/tmp/dependency-updates/gradle.ser")
        def createdReport = Serialization.<GradleVersionReport> read(gradleFile)
        createdReport.current() == "6.0"
        createdReport.latest() != "6.0"
    }
}