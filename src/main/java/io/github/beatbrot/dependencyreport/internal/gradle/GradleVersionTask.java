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
import org.gradle.util.GradleVersion;

import javax.inject.Inject;

public class GradleVersionTask extends DefaultTask {

    public static final String NAME = "analyzeGradleVersion";

    private static final Spec<Task> NEVER = t -> false;

    private final GradleVersionFetcher versionFetcher = new GradleVersionFetcher();

    private final RegularFileProperty gradleVersionFile;

    @Inject
    public GradleVersionTask(final ObjectFactory objects, final ProjectLayout layout) {
        // We cast this to Task so that getOutputs returns the public API.
        ((Task) this).getOutputs().upToDateWhen(NEVER);
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
