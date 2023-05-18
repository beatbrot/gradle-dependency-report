package io.github.beatbrot.dependencyreport;

import io.github.beatbrot.dependencyreport.internal.Serialization;
import io.github.beatbrot.dependencyreport.internal.analysis.DependencyReport;
import io.github.beatbrot.dependencyreport.internal.analysis.DependencyStatus;
import io.github.beatbrot.dependencyreport.internal.gradle.GradleVersionReport;
import io.github.beatbrot.dependencyreport.internal.report.MultiplexerWriter;
import io.github.beatbrot.dependencyreport.internal.report.TextReporter;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.HelpTasksPlugin;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.options.Option;
import org.gradle.api.tasks.options.OptionValues;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public abstract class DependencyReportTask extends DefaultTask {

    public static final String NAME = "dependencyReport";
    public static final String PRINT_DESC = "Print the dependency report to stdout.";
    public static final String CHECK_DESC = "Do not check for Gradle updates.";
    public static final String PRINT_OPT = "print-to-console";
    public static final String CHECK_OPT = "no-gradle-check";

    private final ConfigurableFileCollection dependencyFiles;

    private final ConfigurableFileCollection gradleVersionFile;

    private final Property<Boolean> printToConsole;

    private final RegularFileProperty outputFile;

    @Inject
    public DependencyReportTask(final ObjectFactory objectFactory, final ProjectLayout projectLayout) {
        setGroup(HelpTasksPlugin.HELP_GROUP);
        setDescription("Creates a report about available dependency and Gradle updates.");
        ((Task) this).getOutputs().upToDateWhen(t -> !((DependencyReportTask) t).printToConsole.get());
        this.dependencyFiles = objectFactory.fileCollection();
        this.gradleVersionFile = objectFactory.fileCollection();
        this.printToConsole = objectFactory.property(Boolean.TYPE).convention(true);
        this.outputFile = objectFactory.fileProperty().convention(projectLayout.getBuildDirectory().file("reports/dependencies.txt"));
    }

    @TaskAction
    public void createReports() throws IOException {
        final File versionFile = getSingleFileOrNull(gradleVersionFile);
        final GradleVersionReport gradleReport = versionFile != null ? Serialization.read(versionFile) : null;

        final LinkedHashSet<DependencyStatus> statusList = new LinkedHashSet<>();
        for (final File file : dependencyFiles) {
            statusList.addAll(Serialization.read(file));
        }

        final DependencyReport report = DependencyReport.create(gradleReport, statusList);

        final Path outPath = outputFile.get().getAsFile().toPath();
        createParentDirs(outPath);
        try (final BufferedWriter bw = createWriter(outPath, printToConsole.get())) {
            final TextReporter textReporter = new TextReporter();
            textReporter.report(bw, report);
        }
    }

    @SuppressWarnings("DefaultCharset")
    private BufferedWriter createWriter(final Path outPath, final boolean printToConsole) throws IOException {
        final BufferedWriter fileWriter = Files.newBufferedWriter(outPath, UTF_8, CREATE, TRUNCATE_EXISTING);
        if (printToConsole) {
            final PrintWriter stdoutWriter = new PrintWriter(System.out); // NOPMD
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

    /**
     * Returns a {@link ConfigurableFileCollection} containing the dependency analysis files that will be aggregated to a report.
     * <p>
     * Usually created by {@code analyzeDependencies} tasks
     * </p>
     *
     * @return The dependency analysis files that will  be aggregated.
     */
    @InputFiles
    @PathSensitive(PathSensitivity.NONE)
    public ConfigurableFileCollection getDependencyFiles() {
        return dependencyFiles;
    }

    /**
     * Returns a {@code ConfigurableFileCollection} containing the gradle version analysis file that will be reported.
     * If the collection is empty, the Gradle version will not be reported.
     * <p>
     * Usually created by the {@code analyzeGradleVersion} task
     * </p>
     *
     * @return The gradle version analysis file that will be added to the report.
     */
    @InputFiles
    @PathSensitive(PathSensitivity.NONE)
    public ConfigurableFileCollection getGradleVersionFile() {
        return gradleVersionFile;
    }

    /**
     * Returns a property that defines, whether the report should be printed to console. By convention, this is {@code true}.
     * <p>
     * This task is never up-to-date if this property resolves to {@code true}.
     * </p>
     *
     * @return Whether the report will be printed to console.
     */
    @Input
    public Property<Boolean> getPrintToConsole() {
        return printToConsole;
    }

    @Option(description = PRINT_DESC, option = PRINT_OPT)
    void setPrintToConsole(String value) {
        getPrintToConsole().set(Boolean.valueOf(value));
    }

    @OptionValues(PRINT_OPT)
    @SuppressWarnings("unused")
    Collection<Boolean> getBooleanOptions() {
        return Arrays.asList(false, true);
    }

    @Option(description = CHECK_DESC, option = CHECK_OPT)
    @SuppressWarnings("unused")
    public void setNoGradleVersionCheck() {
        gradleVersionFile.setFrom();
    }

    /**
     * Returns a property defining the location where the report shall be saved.
     *
     * @return The location of the produced report.
     */
    @OutputFile
    public RegularFileProperty getOutputFile() {
        return outputFile;
    }
}
