package io.github.beatbrot.dependencyreport.internal;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public final class Serialization {
    private Serialization() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("TypeParameterUnusedInFormals")
    public static <T> T read(final Path path) {
        try (final ObjectInputStream o = new ObjectInputStream(Files.newInputStream(path))) {
            return uncheckedCast(o.readObject());
        } catch (final IOException | ClassNotFoundException e) {
            throw new SerializationException("Unable to read serialized object from " + path, e);
        }
    }

    @SuppressWarnings("TypeParameterUnusedInFormals")
    public static <T> T read(final File file) {
        return read(file.toPath());
    }

    public static void write(final File file, final Object object) {
        try (final ObjectOutputStream o = new ObjectOutputStream(Files.newOutputStream(file.toPath(), CREATE, TRUNCATE_EXISTING))) {
            o.writeObject(object);
        } catch (final IOException e) {
            throw new SerializationException("Unable to write object to " + file, e);
        }
    }

    public static class SerializationException extends RuntimeException {
        public SerializationException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

    @SuppressWarnings({"unchecked", "TypeParameterUnusedInFormals"})
    private static <T> T uncheckedCast(final Object o) {
        return (T) o;
    }
}
