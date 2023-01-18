package io.github.beatbrot.dependencyreport;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.beatbrot.dependencyreport.internal.Serialization;
import io.github.beatbrot.dependencyreport.internal.analysis.DependencyReport;
import io.github.beatbrot.dependencyreport.internal.analysis.DependencyStatus;
import io.github.beatbrot.dependencyreport.internal.gradle.GradleVersionReport;
import io.github.beatbrot.dependencyreport.internal.report.MultiplexerWriter;
import io.github.beatbrot.dependencyreport.internal.report.Reporter;
import io.github.beatbrot.dependencyreport.internal.report.TextReporter;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.HelpTasksPlugin;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class DependencyReportTask extends DefaultTask {

    public static final String NAME = "dependencyReport";

    private final ConfigurableFileCollection dependencyFiles;

    private final ConfigurableFileCollection gradleVersionFile;

    private final Property<Boolean> printToConsole;

    private final RegularFileProperty outputFile;

    @Inject
    public DependencyReportTask(final ObjectFactory objectFactory, final ProjectLayout projectLayout) {
        setGroup(HelpTasksPlugin.HELP_GROUP);
        setDescription("Creates a report about available dependency & Gradle updates.");
        this.dependencyFiles = objectFactory.fileCollection();
        this.gradleVersionFile = objectFactory.fileCollection();
        this.printToConsole = objectFactory.property(Boolean.class).convention(true);
        this.outputFile = objectFactory.fileProperty().convention(projectLayout.getBuildDirectory().file("reports/dependencies.txt"));
    }

    @TaskAction
    public void createReports() throws IOException {
        final LinkedHashSet<DependencyStatus> statusList = new LinkedHashSet<>();
        final File versionFile = getSingleFileOrNull(gradleVersionFile);
        final GradleVersionReport gradleReport = versionFile != null ? Serialization.read(versionFile) : null;
        for (final File file : dependencyFiles) {
            statusList.addAll(Serialization.read(file));
        }
        final DependencyReport report = DependencyReport.create(gradleReport, statusList);

        final Path outPath = outputFile.get().getAsFile().toPath();
        createParentDirs(outPath);
        try (final BufferedWriter bw = createWriter(outPath, printToConsole.get())) {
            final Reporter textReporter = new TextReporter();
            textReporter.report(bw, report);
        }
    }

    @SuppressFBWarnings("DM")
    @SuppressWarnings("java:S106")
    private BufferedWriter createWriter(final Path outPath, final boolean printToConsole) throws IOException {
        final BufferedWriter fileWriter = Files.newBufferedWriter(outPath, StandardCharsets.UTF_8, CREATE, TRUNCATE_EXISTING);
        if (printToConsole) {
            final PrintWriter stdoutWriter = new PrintWriter(System.out);
            return new BufferedWriter(new MultiplexerWriter(Arrays.asList(fileWriter, stdoutWriter)));
        } else {
            return fileWriter;
        }
    }

    private static void createParentDirs(final Path path) throws IOException {
        final Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

    @Nullable
    private static File getSingleFileOrNull(final FileCollection collection) {
        if (collection.isEmpty()) {
            return null;
        }
        return collection.getSingleFile();
    }

    @InputFiles
    public ConfigurableFileCollection getDependencyFiles() {
        return dependencyFiles;
    }

    @InputFiles
    public ConfigurableFileCollection getGradleVersionFile() {
        return gradleVersionFile;
    }

    @Input
    public Property<Boolean> getPrintToConsole() {
        return printToConsole;
    }

    @Option(description = "Do not check for Gradle updates.", option = "no-gradle-check")
    public void setNoGradleVersionCheck() {
        gradleVersionFile.setFrom();
    }
}
