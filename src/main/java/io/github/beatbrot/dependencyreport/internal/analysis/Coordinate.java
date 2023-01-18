package io.github.beatbrot.dependencyreport.internal.analysis;


import io.github.beatbrot.dependencyreport.internal.Tuple;
import org.gradle.api.artifacts.ResolvedDependency;
import org.immutables.value.Value;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

@Value.Immutable
public abstract class Coordinate implements Comparable<Coordinate>, Serializable {

    private static final Comparator<Coordinate> COMPARATOR = Comparator.comparing(Coordinate::group)
            .thenComparing(Coordinate::name)
            .thenComparing(Coordinate::version);

    public static Coordinate from(final ResolvedDependency r) {
        return ImmutableCoordinate.builder()
                .group(r.getModule().getId().getGroup())
                .name(r.getModule().getId().getName())
                .version(r.getModule().getId().getVersion())
                .build();
    }

    public abstract String group();

    public abstract String name();

    @Nullable
    public abstract String version();

    @Override
    public String toString() {
        return group() + ":" + name() + ":" + version();
    }

    @Value.Lazy
    public Key key() {
        return ImmutableKey.of(group(), name());
    }

    @Override
    public int compareTo(@Nonnull final Coordinate o) {
        return Objects.compare(this, o, COMPARATOR);
    }

    @Value.Immutable
    @Tuple
    public abstract static class Key {
        abstract String group();

        abstract String name();

        @Override
        public String toString() {
            return group() + ":" + name();
        }
    }
}
