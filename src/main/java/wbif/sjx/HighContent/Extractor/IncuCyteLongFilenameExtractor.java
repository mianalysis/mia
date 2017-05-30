package wbif.sjx.HighContent.Extractor;

import wbif.sjx.HighContent.Object.HCMetadata;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by steph on 30/04/2017.
 */
public class IncuCyteLongFilenameExtractor implements Extractor {
    private static final String name = "IncuCyte (long) filename";
    private static final String pattern = "(.+)_([A-Z]\\d+?)_(\\d++)_(\\d{4})y(\\d{2})m(\\d{2})d_(\\d{2})h(\\d{2})m";

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
            result.setYear(Integer.parseInt(fi_matcher.group(4)));
            result.setMonth(Integer.parseInt(fi_matcher.group(5)));
            result.setDay(Integer.parseInt(fi_matcher.group(6)));
            result.setHour(Integer.parseInt(fi_matcher.group(7)));
            result.setMin(Integer.parseInt(fi_matcher.group(8)));

            return true;

        } else {
            return false;

        }
    }
}
