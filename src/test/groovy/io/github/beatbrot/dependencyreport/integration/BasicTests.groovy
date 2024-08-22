package io.github.beatbrot.dependencyreport.integration

import io.github.beatbrot.dependencyreport.DependencyReportTask
import io.github.beatbrot.dependencyreport.Java8Util
import io.github.beatbrot.dependencyreport.internal.Serialization
import io.github.beatbrot.dependencyreport.internal.analysis.AnalyzeDependenciesTask
import io.github.beatbrot.dependencyreport.internal.gradle.GradleVersionReport
import io.github.beatbrot.dependencyreport.internal.gradle.GradleVersionTask
import org.gradle.api.plugins.HelpTasksPlugin
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
        def result = gradleRunner().withArguments(DependencyReportTask.NAME).build()
        then:
        result.task(":${AnalyzeDependenciesTask.NAME}").outcome == TaskOutcome.SUCCESS
        result.task(":${DependencyReportTask.NAME}").outcome == TaskOutcome.SUCCESS
    }

    def "Gradle Task is disabled"() {
        given:
        buildFile << PLUGINS_BLOCK

        when:
        def result = gradleRunner().withArguments(DependencyReportTask.NAME, "--${DependencyReportTask.CHECK_OPT}", "-s").build()
        then:
        result.task(":${GradleVersionTask.NAME}") == null
        result.task(":${AnalyzeDependenciesTask.NAME}").outcome == TaskOutcome.SUCCESS
        result.task(":${DependencyReportTask.NAME}").outcome == TaskOutcome.SUCCESS
    }

    def "Gradle Update available"() {
        given:
        buildFile << PLUGINS_BLOCK
        Java8Util.configureJavaHome(propertiesFile)

        when:
        def result = gradleRunner(false)
            .withGradleVersion("6.0")
            .withArguments(GradleVersionTask.NAME)
            .build()

        then:
        result.task(":${GradleVersionTask.NAME}").outcome == TaskOutcome.SUCCESS
        def gradleFile = new File(testProjectDir, "build/tmp/dependency-updates/gradle.ser")
        def createdReport = Serialization.<GradleVersionReport> read(gradleFile)
        createdReport.currentInternal() == "6.0"
        createdReport.latestInternal() != "6.0"
    }

    def "Various Gradle versions"(String version) {
        given:
        buildFile << PLUGINS_BLOCK

        when:
        def result = gradleRunner()
            .withArguments(DependencyReportTask.NAME)
            .build()
        then:
        result.task(":${AnalyzeDependenciesTask.NAME}").outcome == TaskOutcome.SUCCESS
        result.task(":${DependencyReportTask.NAME}").outcome == TaskOutcome.SUCCESS

        where:
        version << ["6.0", "6.9", "7.0", "7.4", "8.0-rc2"]
    }

    def "Task help works"() {
        given:
        buildFile << PLUGINS_BLOCK

        when:
        def result = gradleRunner()
            .withArguments(HelpTasksPlugin.HELP_GROUP, "--task", DependencyReportTask.NAME)
            .forwardOutput()
            .build()
        then:
        result.output.contains(DependencyReportTask.CHECK_DESC)
        result.output.contains(DependencyReportTask.CHECK_OPT)
        result.output.contains(DependencyReportTask.PRINT_DESC)
        result.output.contains(DependencyReportTask.PRINT_OPT)
        result.output.contains(HelpTasksPlugin.HELP_GROUP)
    }

    def "Task parameters work"(String param) {
        given:
        buildFile << PLUGINS_BLOCK

        when:
        gradleRunner()
            .withArguments(DependencyReportTask.NAME, "--print-to-console=false")
            .build()
        then:
        noExceptionThrown()

        where:
        param << ["--${DependencyReportTask.PRINT_OPT}=false", "--${DependencyReportTask.CHECK_OPT}"]
    }
}
