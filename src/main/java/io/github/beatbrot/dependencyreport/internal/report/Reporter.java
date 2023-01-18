package io.github.beatbrot.dependencyreport.internal.report;

import io.github.beatbrot.dependencyreport.internal.analysis.DependencyReport;

import java.io.IOException;
import java.io.Writer;

public interface Reporter {
    void report(Writer writer, DependencyReport report) throws IOException;
}
