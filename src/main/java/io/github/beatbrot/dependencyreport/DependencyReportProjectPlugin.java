package io.github.beatbrot.dependencyreport;

import io.github.beatbrot.dependencyreport.internal.analysis.AnalyzeDependenciesTask;
import io.github.beatbrot.dependencyreport.internal.gradle.GradleVersionTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import static io.github.beatbrot.dependencyreport.DependencyReportTask.NAME;

public class DependencyReportProjectPlugin implements Plugin<Project> {
    @Override
    public void apply(Project target) {
        TaskContainer tasks = target.getTasks();
        TaskContainer rootTasks = target.getRootProject().getTasks();

        final TaskProvider<?> analysisTask = tasks.register(AnalyzeDependenciesTask.NAME, AnalyzeDependenciesTask.class);
        rootTasks.named(NAME, DependencyReportTask.class).configure(t -> {
            t.getDependencyFiles().from(analysisTask);
        });
    }
}
