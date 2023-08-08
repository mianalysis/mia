package io.github.mianalysis.mia.object.metadata;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sc13967 on 18/07/2017.
 */
public class OperaFilenameExtractor implements NameExtractor {
    private static final String name = "Opera filename";
    private static final String pattern = "(\\d{3})(\\d{3})(\\d{3})";


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
        Pattern fi_pattern = Pattern.compile(pattern);
        Matcher fi_matcher = fi_pattern.matcher(str);

        int loc = str.lastIndexOf(".");
        if (loc >= 0) {
            result.setExt(str.substring(loc + 1));
        }

        if (fi_matcher.find()) {
            result.setRow(Integer.parseInt(fi_matcher.group(1)));
            result.setCol(Integer.parseInt(fi_matcher.group(2)));
            result.setField(Integer.parseInt(fi_matcher.group(3)));

            String rowLetter = String.valueOf((char) (Integer.parseInt(fi_matcher.group(1)) + 64));
            String colLetter = String.valueOf(Integer.parseInt(fi_matcher.group(2)));
            result.setWell(rowLetter+colLetter);

            return true;

        } else {
            return false;

        }
    }
}
