package io.github.beatbrot.dependencyreport.integration

import io.github.beatbrot.dependencyreport.DependencyReportTask
import io.github.beatbrot.dependencyreport.internal.analysis.AnalyzeDependenciesTask
import org.gradle.testkit.runner.TaskOutcome

class UpToDateTest extends GradleSpecification {
    def "Tasks are never up-to-date when printing to console"() {
        setup:
        buildFile << PLUGINS_BLOCK

        when:
        def result = gradleRunner().withArguments(DependencyReportTask.NAME).build()
        then:
        result.task(":${DependencyReportTask.NAME}").outcome == TaskOutcome.SUCCESS
        result.task(":${AnalyzeDependenciesTask.NAME}").outcome == TaskOutcome.SUCCESS

        when:
        def result2 = gradleRunner().withArguments(DependencyReportTask.NAME).build()
        then:
        println result2.output
        result2.task(":${DependencyReportTask.NAME}").outcome == TaskOutcome.SUCCESS
        result2.task(":${AnalyzeDependenciesTask.NAME}").outcome == TaskOutcome.SUCCESS
    }

    def "Tasks are up-to-date when not printing to console"() {
        setup:
        buildFile << PLUGINS_BLOCK

        when:
        def result = gradleRunner().withArguments(DependencyReportTask.NAME, "--$DependencyReportTask.PRINT_OPT=false").build()
        then:
        result.task(":${DependencyReportTask.NAME}").outcome == TaskOutcome.SUCCESS
        result.task(":${AnalyzeDependenciesTask.NAME}").outcome == TaskOutcome.SUCCESS

        when:
        def result2 = gradleRunner().withArguments(DependencyReportTask.NAME, "--$DependencyReportTask.PRINT_OPT=false").build()
        then:
        println result2.output
        result2.task(":${DependencyReportTask.NAME}").outcome == TaskOutcome.UP_TO_DATE
        result2.task(":${AnalyzeDependenciesTask.NAME}").outcome == TaskOutcome.SUCCESS
    }

    def "Supports configuration cache"() {
        setup:
        buildFile << PLUGINS_BLOCK

        when:
        def result = gradleRunner().withArguments(DependencyReportTask.NAME, "--configuration-cache").build()
        then:
        result.task(":${DependencyReportTask.NAME}").outcome == TaskOutcome.SUCCESS
        result.task(":${AnalyzeDependenciesTask.NAME}").outcome == TaskOutcome.SUCCESS

        when:
        def result2 = gradleRunner().withArguments(DependencyReportTask.NAME, "--configuration-cache").build()
        then:
        result2.output.contains("Reusing configuration cache.")
    }
}
