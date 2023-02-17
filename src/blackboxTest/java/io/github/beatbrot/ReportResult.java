package io.github.beatbrot;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class ReportResult {
    private static final String UTD_PREAMBLE = "The following dependencies are UP-TO-DATE:\n";
    private static final String NUTD_PREAMBLE = "These dependencies have updates available:";
    private final int upToDateDeps;
    private final int outdatedDeps;

    public ReportResult(final int upToDateDeps, final int outdatedDeps) {
        this.upToDateDeps = upToDateDeps;
        this.outdatedDeps = outdatedDeps;
    }

    public static ReportResult parseMine(final String gradleOutput) {
        final int utdIndex = gradleOutput.indexOf(UTD_PREAMBLE);
        final int nutdIndex = gradleOutput.indexOf(NUTD_PREAMBLE);
        final String upToDate = gradleOutput.substring(utdIndex + UTD_PREAMBLE.length(), nutdIndex);
        final String notUpToDate = gradleOutput.substring(nutdIndex);

        return new ReportResult(
            upToDate.split("- ").length - 1,
            notUpToDate.split("- ").length - 1
        );
    }

    public int getOutdatedDeps() {
        return outdatedDeps;
    }

    public int getUpToDateDeps() {
        return upToDateDeps;
    }

    @Override
    public String toString() {
        return "ReportResult{" +
            "upToDateDeps=" + upToDateDeps +
            ", outdatedDeps=" + outdatedDeps +
            '}';
    }
}
