package io.github.beatbrot.dependencyreport;

import io.github.beatbrot.dependencyreport.internal.analysis.AnalyzeDependenciesTask;
import io.github.beatbrot.dependencyreport.internal.gradle.GradleVersionTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class DependencyReportPlugin implements Plugin<Project> {

    public static final String PLUGIN_SHOULD_BE_APPLIED_TO_ROOT = "WARN: The dependency-updates plugin must be applied to the root project to get a complete report.";

    @Inject
    public DependencyReportPlugin() {
    }

    @Override
    public void apply(final Project target) {
        if (target != target.getRootProject()) {
            target.getLogger().warn(PLUGIN_SHOULD_BE_APPLIED_TO_ROOT);
        }
        final TaskProvider<GradleVersionTask> gradleTask = target.getTasks().register(GradleVersionTask.NAME, GradleVersionTask.class);
        final List<TaskProvider<?>> analysisTasks = applyAnalysisTask(target);

        target.getTasks().register(DependencyReportTask.NAME, DependencyReportTask.class).configure(r -> {
            r.getGradleVersionFile().from(gradleTask);
            r.getDependencyFiles().from(analysisTasks);
        });

    }

    private static List<TaskProvider<?>> applyAnalysisTask(final Project target) {
        final List<TaskProvider<?>> result = new ArrayList<>();
        target.allprojects(p -> {
            final TaskProvider<?> task = p.getTasks().register(AnalyzeDependenciesTask.NAME, AnalyzeDependenciesTask.class);
            result.add(task);
        });
        return result;
    }
}
