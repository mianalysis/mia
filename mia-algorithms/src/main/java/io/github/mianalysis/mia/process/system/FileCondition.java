package io.github.mianalysis.mia.process.system;

import java.io.File;

/**
 * Created by sc13967 on 24/10/2016.
 */
public interface FileCondition {
    public enum Mode {
        INC_COMPLETE, // Including complete match for test string
        INC_PARTIAL, // Including partial match for test string
        EXC_COMPLETE, // Excluding complete match for test string
        EXC_PARTIAL; // Excluding partial match for test string
    }

    public boolean test(File file, boolean ignoreCase);

    public boolean test(File file);

    public void setIgnoreCase(boolean ignoreCase);

    public boolean getIgnoreCase();

}
