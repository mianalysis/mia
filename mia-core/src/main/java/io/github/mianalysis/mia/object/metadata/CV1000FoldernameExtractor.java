package io.github.mianalysis.mia.object.metadata;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extractor for the Yokogawa CV1000 format (e.g. 20210806T113905_10x_K01_MySample1)
 * 
 * Created by steph on 30/04/2017.
 * 
 */
public class CV1000FoldernameExtractor implements NameExtractor {
    private static final String name = "CV1000 folder name";
    private static final String pattern = "(\\d{4})(\\d{2})(\\d{2})T(\\d{2})(\\d{2})(\\d{2})_([^_]+?)_([^_\\\\]++)_?([.[^\\\\]]++)?";

    public String getName() {
        return name;
        
    }

    public String getPattern() {
        return pattern;

    }

    public boolean extract(Metadata result, String str) {
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
