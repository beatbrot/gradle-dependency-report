package io.github.beatbrot.dependencyreport.internal.report;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MultiplexerWriter extends Writer {
    private final List<Writer> childWriters;

    public MultiplexerWriter(final List<Writer> childWriters) {
        this.childWriters = Objects.requireNonNull(childWriters);
    }

    @Override
    public void write(@Nonnull final char[] cbuf, final int off, final int len) throws IOException {
        runForAll(w -> w.write(cbuf, off, len));
    }

    @Override
    public void flush() throws IOException {
        runForAll(Writer::flush);
    }

    @Override
    public void close() throws IOException {
        runForAll(Writer::close);
    }

    private void runForAll(final Command command) throws IOException {
        List<Exception> suppressed = null;
        for (final Writer childWriter : childWriters) { //NOPMD
            try {
                command.call(childWriter);
            } catch (final Exception e) {
                if (suppressed == null) {
                    suppressed = new ArrayList<>(); //NOPMD
                }
                suppressed.add(e);
            }
        }
        if (suppressed != null) {
            final IOException ex = new IOException("Multiplexed Operation failed.");
            for (final Exception sup : suppressed) {
                ex.addSuppressed(sup);
            }
            throw ex;
        }
    }

    private interface Command {
        @SuppressWarnings("java:S112")
        void call(Writer writer) throws Exception;
    }
}
