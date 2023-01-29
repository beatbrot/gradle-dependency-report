package io.github.beatbrot.dependencyreport.internal.report;

import io.github.beatbrot.dependencyreport.internal.analysis.DependencyReport;
import io.github.beatbrot.dependencyreport.internal.analysis.DependencyStatus;
import io.github.beatbrot.dependencyreport.internal.gradle.GradleVersionReport;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.SortedSet;

public class TextReporter {

    private static final char NEWLINE = '\n';
    private static final String GRADLE_UTD_STRING = "UP-TO-DATE";

    public void report(final Writer writer, final DependencyReport report) throws IOException {
        writeUpToDateDeps(writer, report.upToDateDependencies());
        writeOutdatedDeps(writer, report.outdatedDependencies());

        final GradleVersionReport gradleReport = report.gradleVersionReport();
        if (gradleReport != null) {
            writeAvailableGradleUpdate(writer, gradleReport);
        }
    }

    private static void writeUpToDateDeps(Writer writer, Collection<DependencyStatus> upToDateDeps) throws IOException {
        writer.write("The following dependencies are " + GRADLE_UTD_STRING + ":" + NEWLINE);
        for (final DependencyStatus status : upToDateDeps) {
            writer.write("- " + status.coordinate());
            writer.write(NEWLINE);
        }
        writer.write(NEWLINE);
    }

    private static void writeOutdatedDeps(Writer writer, SortedSet<DependencyStatus> outdatedDeps) throws IOException {
        writer.write("These dependencies have updates available:");
        writer.write(NEWLINE);
        for (final DependencyStatus status : outdatedDeps) {
            writer.write("- " + status.coordinate().key() + " " + versionUpgrade(status.coordinate().version(), status.latestVersion()));
            writer.write(NEWLINE);
        }
        writer.write(NEWLINE);
    }

    private static void writeAvailableGradleUpdate(Writer writer, GradleVersionReport gradleReport) throws IOException {
        if (gradleReport.isUpToDate()) {
            writer.write("The Gradle version is " + GRADLE_UTD_STRING + "." + NEWLINE);
        } else {
            final String updateString = versionUpgrade(gradleReport.current().getVersion(), gradleReport.latest().getVersion());
            writer.write("An update for Gradle is available: " + updateString + NEWLINE);
        }
    }

    private static String versionUpgrade(final String current, final String latest) {
        return "[" + current + " -> " + latest + "]";
    }
}
