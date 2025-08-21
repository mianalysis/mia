package io.github.mianalysis.mia.process.system;

import java.io.File;

/**
 * Checks the input file against a list of acceptable extensions
 * Created by sc13967 on 24/10/2016.
 */
public class ParentContainsString implements FileCondition {
    private String[] testStrs;
    private Mode mode;
    private boolean ignoreCase = false;

    public ParentContainsString(String testStr) {
        this.testStrs = new String[] { testStr };
        this.mode = Mode.INC_PARTIAL;
    }

    public ParentContainsString(String testStr, Mode mode) {
        this.testStrs = new String[] { testStr };
        this.mode = mode;
    }

    public ParentContainsString(String testStr, Mode mode, boolean ignoreCase) {
        this.testStrs = new String[] { testStr };
        this.mode = mode;
        this.ignoreCase = ignoreCase;
    }

    public ParentContainsString(String[] testStr) {
        this.testStrs = testStr;
        this.mode = Mode.INC_PARTIAL;
    }

    public ParentContainsString(String[] testStr, Mode mode) {
        this.testStrs = testStr;
        this.mode = mode;
    }

    public ParentContainsString(String[] testStr, Mode mode, boolean ignoreCase) {
        this.testStrs = testStr;
        this.mode = mode;
        this.ignoreCase = ignoreCase;
    }

    public boolean test(File file, boolean ignoreCase) {
        if (file != null) {
            String name = file.getParent();
            if (ignoreCase)
                name = name.toLowerCase();

            for (String testStr : testStrs) {
                if (ignoreCase)
                    testStr = testStr.toLowerCase();

                switch (mode) {
                    case INC_COMPLETE:
                        if (name.equals(testStr))
                            return true;
                        break;
                    case INC_PARTIAL:
                        if (name.contains(testStr))
                            return true;
                        break;
                    case EXC_COMPLETE:
                        if (name.equals(testStr))
                            return false;
                        break;
                    case EXC_PARTIAL:
                        if (name.contains(testStr))
                            return false;
                        break;
                }
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

    public boolean test(File file) {
        return test(file, ignoreCase);
    }

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    public boolean getIgnoreCase() {
        return ignoreCase;
    }
}