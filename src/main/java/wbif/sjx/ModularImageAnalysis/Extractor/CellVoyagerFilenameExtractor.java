package wbif.sjx.ModularImageAnalysis.Extractor;

import wbif.sjx.ModularImageAnalysis.Object.HCMetadata;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by steph on 30/04/2017.
 */
public class CellVoyagerFilenameExtractor implements Extractor {
    private static final String name = "CellVoyager filename";
    private static final String pattern = "W(\\d+?)F(\\d+?)T(\\d+?)Z(\\d+?)C(\\d+?)";

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
            result.setWell(fi_matcher.group(1));
            result.setField(Integer.parseInt(fi_matcher.group(2)));
            result.setTimepoint(Integer.parseInt(fi_matcher.group(3)));
            result.setZ(Integer.parseInt(fi_matcher.group(4)));
            result.setChannel(Integer.parseInt(fi_matcher.group(5)));

            return true;

        } else {
            return false;

        }
    }
}
