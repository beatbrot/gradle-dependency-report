package io.github.beatbrot.dependencyreport;

import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;

public class DependencyReportPlugin implements Plugin<Settings> {
    @Override
    public void apply(Settings target) {
        target.getGradle().rootProject(p -> {
            p.apply(e -> e.type(DependencyReportRootProjectPlugin.class));
        });
        target.getGradle().allprojects(p -> {
            p.apply(e -> e.type(DependencyReportProjectPlugin.class));
        });
    }
}
