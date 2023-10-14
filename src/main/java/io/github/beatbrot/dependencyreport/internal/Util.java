package io.github.beatbrot.dependencyreport.internal;

public class Util {
    private Util() {

    }

    @SuppressWarnings({"unchecked", "TypeParameterUnusedInFormals"})
    public static <T> T uncheckedCast(Object input) {
        return (T) input;
    }
}
