package io.github.beatbrot.dependencyreport.internal.report;

import io.github.beatbrot.dependencyreport.internal.analysis.DependencyReport;
import io.github.beatbrot.dependencyreport.internal.analysis.DependencyStatus;
import io.github.beatbrot.dependencyreport.internal.gradle.GradleVersionReport;

import java.io.IOException;
import java.io.Writer;

public class TextReporter implements Reporter {

    private static final char NEWLINE = '\n';
    private static final String GRADLE_UTD_STRING = "UP-TO-DATE";

    @Override
    public void report(final Writer writer, final DependencyReport report) throws IOException {
        writer.write("The following dependencies are " + GRADLE_UTD_STRING + ":");
        writer.write(NEWLINE);
        for (final DependencyStatus status : report.upToDateDependencies()) {
            writer.write("- " + status.coordinate());
            writer.write(NEWLINE);
        }

        writer.write(NEWLINE);
        writer.write("These dependencies have updates available:");
        writer.write(NEWLINE);
        for (final DependencyStatus status : report.outdatedDependencies()) {
            writer.write("- " + status.coordinate().key() + " " + versionUpgrade(status.coordinate().version(), status.latestVersion()));
            writer.write(NEWLINE);
        }

        writer.write(NEWLINE);
        final GradleVersionReport gradleReport = report.gradleVersionReport();
        if (gradleReport != null) {
            if (gradleReport.isUpToDate()) {
                writer.write("The Gradle version is " + GRADLE_UTD_STRING + ".");
            } else {
                writer.write("An update for Gradle is available: " + versionUpgrade(gradleReport.current(), gradleReport.latest()));
            }
        }
    }

    private String versionUpgrade(final String current, final String latest) {
        return "[" + current + " -> " + latest + "]";
    }
}
