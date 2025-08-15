package io.github.mianalysis.mia.process.system;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

/**
 * Checks the input file against a list of acceptable extensions
 * Created by sc13967 on 24/10/2016.
 */
public class NameContainsString implements FileCondition {
    private String[] testStr;
    private Mode mode;
    private boolean ignoreCase = false;

    public NameContainsString(String testStr) {
        this.testStr = new String[] { testStr };
        this.mode = Mode.INC_PARTIAL;
    }

    public NameContainsString(String testStr, Mode mode) {
        this.testStr = new String[] { testStr };
        this.mode = mode;
    }

    public NameContainsString(String testStr, Mode mode, boolean ignoreCase) {
        this.testStr = new String[] { testStr };
        this.mode = mode;
        this.ignoreCase = ignoreCase;
    }

    public NameContainsString(String[] testStr) {
        this.testStr = testStr;
        this.mode = Mode.INC_PARTIAL;
    }

    public NameContainsString(String[] testStr, Mode mode) {
        this.testStr = testStr;
        this.mode = mode;
    }

    public NameContainsString(String[] testStr, Mode mode, boolean ignoreCase) {
        this.testStr = testStr;
        this.mode = mode;
        this.ignoreCase = ignoreCase;
    }

    public boolean test(String string, boolean ignoreCase) {
        if (ignoreCase)
            string = string.toLowerCase();

        for (String s : testStr) {
            if (ignoreCase)
                s = s.toLowerCase();

            switch (mode) {
                case INC_COMPLETE:
                    if (string.equals(s))
                        return true;
                    break;
                case INC_PARTIAL:
                    if (string.contains(s))
                        return true;
                    break;
                case EXC_COMPLETE:
                    if (string.equals(s))
                        return false;
                    break;
                case EXC_PARTIAL:
                    if (string.contains(s))
                        return false;
                    break;
            }
        }

        switch (mode) {
            case INC_COMPLETE:
            case INC_PARTIAL:
            default:
                return false;
            case EXC_COMPLETE:
            case EXC_PARTIAL:
                return true;

        }
    }

    public boolean test(File file, boolean ignoreCase) {
        if (file == null)
            return false;
        String name = FilenameUtils.removeExtension(file.getName());
        return test(name, ignoreCase);

    }

    public boolean test(File file) {
        return test(file, ignoreCase);
    }

    public String[] getTestStr() {
        return testStr;
    }

    public Mode getMode() {
        return mode;
    }

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    public boolean getIgnoreCase() {
        return ignoreCase;
    }
}