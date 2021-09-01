package io.github.mianalysis.MIA.Process.AnalysisHandling;

import java.io.File;

public class AnalysisJob {
    private final File file;
    private final int seriesNumber;
    private final String seriesName;
    private final boolean complete = false;

    public AnalysisJob(File file, int seriesNumber, String seriesName) {
        this.file = file;
        this.seriesNumber = seriesNumber;
        this.seriesName = seriesName;

    }

}
