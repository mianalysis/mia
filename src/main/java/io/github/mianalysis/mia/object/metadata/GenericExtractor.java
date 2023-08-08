package io.github.mianalysis.mia.object.metadata;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class GenericExtractor implements NameExtractor {
    private static final String name = "Generic";
    private final String pattern;
    private final String[] groups;

    public GenericExtractor(String pattern, String[] groups) {
        this.pattern = pattern;
        this.groups = groups;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public boolean extract(Metadata result, String str) {
        return extract(result, str, false);
    }

    public boolean extract(Metadata result, String str, boolean caseInsensitive) {
        try {
            Pattern fi_pattern = caseInsensitive ? Pattern.compile(pattern, Pattern.CASE_INSENSITIVE) : Pattern.compile(pattern);
            Matcher fi_matcher = fi_pattern.matcher(str);

            if (fi_matcher.find()) {
                int nGroups = Math.min(fi_matcher.groupCount(), groups.length);

                for (int i = 0; i < nGroups; i++) {
                    String group = groups[i];
                    String value = fi_matcher.group(i + 1);
                    result.put(group, value);

                }
                return true;

            } else {
                return false;

            }
        } catch (PatternSyntaxException e) {
            return false;
        }
    }
}
