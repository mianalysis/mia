package io.github.mianalysis.mia.process.system;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

/**
 * Created by sc13967 on 24/10/2016.
 */
public class NameContainsPattern implements FileCondition {   
    private Pattern[] patterns;
    private Mode mode;

    public NameContainsPattern(Pattern pattern) {
        this.patterns = new Pattern[] { pattern };
        this.mode = Mode.INC_PARTIAL;

    }

    public NameContainsPattern(Pattern pattern, Mode mode) {
        this.patterns = new Pattern[] { pattern };
        this.mode = mode;

    }

    public NameContainsPattern(Pattern[] pattern) {
        this.patterns = pattern;
        this.mode = Mode.INC_PARTIAL;

    }

    public NameContainsPattern(Pattern[] pattern, Mode mode) {
        this.patterns = pattern;
        this.mode = mode;

    }

    public boolean test(File file, boolean ignoreCase) {
        if (file != null) {
            String name = FilenameUtils.removeExtension(file.getName());
            
            for (Pattern pattern : patterns) {
                if (ignoreCase)
                    pattern = Pattern.compile(pattern.toString(),Pattern.CASE_INSENSITIVE);

                Matcher matcher = pattern.matcher(name);

                switch (mode) {
                    case INC_COMPLETE:
                        if (matcher.matches())
                            return true;
                        break;
                    case INC_PARTIAL:
                        if (matcher.find())
                            return true;
                        break;
                    case EXC_COMPLETE:
                        if (matcher.matches())
                            return false;
                        break;
                    case EXC_PARTIAL:
                        if (matcher.find())
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

    public Pattern[] getPattern() {
        return patterns;
    }

    public Mode getMode() {
        return mode;
    }
}
