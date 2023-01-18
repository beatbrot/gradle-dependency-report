package io.github.beatbrot.dependencyreport.internal;

import org.immutables.value.Value;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Value.Style(
        allParameters = true,
        defaults = @Value.Immutable(builder = false)
)
@Retention(RetentionPolicy.SOURCE)
public @interface Tuple {
}
