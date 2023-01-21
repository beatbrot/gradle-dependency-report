package io.github.beatbrot.dependencyreport.internal.gradle;

import io.github.beatbrot.dependencyreport.internal.Tuple;
import org.gradle.util.GradleVersion;
import org.immutables.value.Value;

import java.io.Serializable;

@Value.Immutable
@Tuple
public interface GradleVersionReport extends Serializable {
    String currentInternal();

    String  latestInternal();

    default GradleVersion latest(){
        return GradleVersion.version(latestInternal());
    }

    default GradleVersion current() {
        return GradleVersion.version(currentInternal());
    }

    default boolean isUpToDate() {
        return current().compareTo(latest()) >= 0;
    }
}
