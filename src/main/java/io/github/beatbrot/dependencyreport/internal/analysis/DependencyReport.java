package io.github.beatbrot.dependencyreport.internal.analysis;

import io.github.beatbrot.dependencyreport.internal.gradle.GradleVersionReport;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

@Value.Immutable
public interface DependencyReport {
    static DependencyReport create(@Nullable final GradleVersionReport gradleVersionReport, final Collection<DependencyStatus> dependencyStatuses) {
        final SortedSet<DependencyStatus> upToDate = new TreeSet<>();
        final SortedSet<DependencyStatus> outDated = new TreeSet<>();
        for (final DependencyStatus status : dependencyStatuses) {
            (status.isUpToDate() ? upToDate : outDated).add(status);
        }
        return ImmutableDependencyReport.builder()
            .upToDateDependencies(upToDate)
            .outdatedDependencies(outDated)
            .gradleVersionReport(gradleVersionReport)
            .build();
    }

    SortedSet<DependencyStatus> upToDateDependencies();

    SortedSet<DependencyStatus> outdatedDependencies();

    @Nullable
    GradleVersionReport gradleVersionReport();
}
