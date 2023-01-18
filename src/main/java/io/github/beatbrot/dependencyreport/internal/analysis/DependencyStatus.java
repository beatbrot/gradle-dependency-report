package io.github.beatbrot.dependencyreport.internal.analysis;

import io.github.beatbrot.dependencyreport.internal.Tuple;
import org.immutables.value.Value;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

@Value.Immutable
@Tuple
public abstract class DependencyStatus implements Comparable<DependencyStatus>, Serializable {

    private static final Comparator<DependencyStatus> COMPARATOR = Comparator.comparing(DependencyStatus::coordinate)
            .thenComparing(DependencyStatus::latestVersion);

    public abstract Coordinate coordinate();

    @Nullable
    public abstract String latestVersion();

    public boolean isUpToDate() {
        return Objects.equals(coordinate().version(), latestVersion());
    }

    @Override
    public int compareTo(@Nonnull final DependencyStatus o) {
        return Objects.compare(this, o, COMPARATOR);
    }
}
