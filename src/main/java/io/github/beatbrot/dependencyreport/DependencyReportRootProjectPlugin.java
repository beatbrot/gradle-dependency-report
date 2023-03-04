package io.github.beatbrot.dependencyreport;

import io.github.beatbrot.dependencyreport.internal.gradle.GradleVersionTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

public class DependencyReportRootProjectPlugin implements Plugin<Project> {
    @Override
    public void apply(Project target) {
        final TaskProvider<GradleVersionTask> gradleTask = target.getTasks().register(GradleVersionTask.NAME, GradleVersionTask.class);

        target.getTasks().register(DependencyReportTask.NAME, DependencyReportTask.class).configure(r -> {
            r.getGradleVersionFile().from(gradleTask);
        });
    }
}
