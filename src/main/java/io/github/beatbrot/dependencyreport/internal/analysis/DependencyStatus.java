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
public interface DependencyStatus extends Comparable<DependencyStatus>, Serializable {

    Comparator<DependencyStatus> COMPARATOR = Comparator.comparing(DependencyStatus::coordinate)
        .thenComparing(DependencyStatus::latestVersion);

    Coordinate coordinate();

    @Nullable
    String latestVersion();

    default boolean isUpToDate() {
        return Objects.equals(coordinate().version(), latestVersion());
    }

    @Override
    @SuppressWarnings("java:S1210") // Equals correctly implemented by Immutables
    default int compareTo(@Nonnull final DependencyStatus o) {
        return Objects.compare(this, o, COMPARATOR);
    }
}
