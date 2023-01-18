package io.github.beatbrot.dependencyreport.internal.gradle;

import io.github.beatbrot.dependencyreport.internal.Tuple;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.Objects;

@Value.Immutable
@Tuple
public abstract class GradleVersionReport implements Serializable {
    public abstract String current();

    public abstract String latest();

    /**
     * @return {@code true}, if the {@link #current()} Gradle version differs from the {@link #latest()} version.
     */
    public boolean isUpToDate() {
        return Objects.equals(current(), latest());
    }
}
