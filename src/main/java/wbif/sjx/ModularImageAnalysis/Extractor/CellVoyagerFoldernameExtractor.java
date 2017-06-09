package wbif.sjx.ModularImageAnalysis.Extractor;

import wbif.sjx.ModularImageAnalysis.Object.HCMetadata;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by steph on 30/04/2017.
 */
public class CellVoyagerFoldernameExtractor implements Extractor {
    private static final String name = "CellVoyager folder name";
    private static final String pattern = "(\\d{4})(\\d{2})(\\d{2})T(\\d{2})(\\d{2})(\\d{2})_([^_]+?)_([^_\\\\]++)_?([.[^\\\\]]++)?";

    public String getName() {
        return name;
        
    }

    public String getPattern() {
        return pattern;

    }

    public boolean extract(HCMetadata result, String str) {
        Pattern fo_pattern = Pattern.compile(pattern);
        Matcher fo_matcher = fo_pattern.matcher(str);

        if (fo_matcher.find()) {
            result.setYear(Integer.parseInt(fo_matcher.group(1)));
            result.setMonth(Integer.parseInt(fo_matcher.group(2)));
            result.setDay(Integer.parseInt(fo_matcher.group(3)));
            result.setHour(Integer.parseInt(fo_matcher.group(4)));
            result.setMin(Integer.parseInt(fo_matcher.group(5)));
            result.setSec(Integer.parseInt(fo_matcher.group(6)));
            result.setMag(fo_matcher.group(7));
            result.setCelltype(fo_matcher.group(8));
            result.setComment(fo_matcher.group(9));

            return true;

        } else {
            return false;

        }
    }
}
