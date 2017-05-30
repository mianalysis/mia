package wbif.sjx.HighContent.Extractor;

import wbif.sjx.HighContent.Object.HCMetadata;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by steph on 30/04/2017.
 */
public class IncuCyteShortFilenameExtractor implements Extractor {
    private static final String name = "IncuCyte (short) filename";
    private static final String pattern = "(.+)_([A-Z]\\d+?)_(\\d++)";

    public String getName() {
        return name;

    }

    public String getPattern() {
        return pattern;

    }

    public boolean extract(HCMetadata result, String str) {
        Pattern fi_pattern = Pattern.compile(pattern);
        Matcher fi_matcher = fi_pattern.matcher(str);

        int loc = str.lastIndexOf(".");
        if (loc >= 0) {
            result.setExt(str.substring(loc + 1));
        }

        if (fi_matcher.find()) {
            result.setComment(fi_matcher.group(1));
            result.setWell(fi_matcher.group(2));
            result.setField(Integer.parseInt(fi_matcher.group(3)));

            return true;

        } else {
            return false;

        }
    }
}
