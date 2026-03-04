package io.github.beatbrot.dependencyreport.internal.gradle;

import io.github.beatbrot.dependencyreport.internal.Serialization;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.UntrackedTask;
import org.gradle.util.GradleVersion;

import javax.inject.Inject;

@UntrackedTask(because = "We fetch version information from the internet")
public abstract class GradleVersionTask extends DefaultTask {

    public static final String NAME = "analyzeGradleVersion";

    private final GradleVersionFetcher versionFetcher = new GradleVersionFetcher();

    private final RegularFileProperty gradleVersionFile;

    @Inject
    public GradleVersionTask(final ObjectFactory objects, final ProjectLayout layout) {
        gradleVersionFile = objects.fileProperty()
            .convention(layout.getBuildDirectory().file("tmp/dependency-updates/gradle.ser"));
    }

    @TaskAction
    public void performTask() {
        final GradleVersion currentGradleVersion = GradleVersion.current();
        GradleVersion latestGradleVersion = GradleVersion.version(versionFetcher.getLatestGradleVersion());
        final ImmutableGradleVersionReport report = ImmutableGradleVersionReport.of(currentGradleVersion.getVersion(), latestGradleVersion.getVersion());
        Serialization.write(gradleVersionFile.get().getAsFile(), report);
    }

    @OutputFile
    public RegularFileProperty getGradleVersionFile() {
        return gradleVersionFile;
    }

}
