package io.github.beatbrot.dependencyreport.internal.report

import io.github.beatbrot.dependencyreport.internal.analysis.DependencyReport
import io.github.beatbrot.dependencyreport.internal.analysis.ImmutableCoordinate
import io.github.beatbrot.dependencyreport.internal.analysis.ImmutableDependencyStatus
import io.github.beatbrot.dependencyreport.internal.gradle.ImmutableGradleVersionReport
import spock.lang.Shared
import spock.lang.Specification

class TextReporterTest extends Specification {

    @Shared
    TextReporter reporter = new TextReporter()

    StringWriter s = new StringWriter()

    def "Normal report is created"() {
        setup:
        def input = DependencyReport.create(
            ImmutableGradleVersionReport.of("1.0", "2.0"),
            [
                ImmutableDependencyStatus.of(ImmutableCoordinate.of("g","n","1"), "1"),
                ImmutableDependencyStatus.of(ImmutableCoordinate.of("g","n2","1"), "1"),

                ImmutableDependencyStatus.of(ImmutableCoordinate.of("g2","n","2"), "3"),
                ImmutableDependencyStatus.of(ImmutableCoordinate.of("g2","n2","2"), "3"),
            ]
        )
        when:
        reporter.report(s, input)
        then:
        s.close()
        println s.toString()
        s.toString() == """The following dependencies are UP-TO-DATE:
- g:n:1
- g:n2:1

These dependencies have updates available:
- g2:n [2 -> 3]
- g2:n2 [2 -> 3]

An update for Gradle is available: [1.0 -> 2.0]
"""
    }
}
