package io.github.mianalysis.mia.object.metadata;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sc13967 on 18/07/2017.
 */
public class OperaFoldernameExtractor implements NameExtractor {
    private static final String name = "Opera foldername";
    private static final String pattern = "Meas_(\\d{2})\\((\\d{4})-(\\d{2})-(\\d{2})_(\\d{2})-(\\d{2})-(\\d{2})\\)";

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
        Pattern fo_pattern = Pattern.compile(pattern);
        Matcher fo_matcher = fo_pattern.matcher(str);

        if (fo_matcher.find()) {
            result.setYear(Integer.parseInt(fo_matcher.group(2)));
            result.setMonth(Integer.parseInt(fo_matcher.group(3)));
            result.setDay(Integer.parseInt(fo_matcher.group(4)));
            result.setHour(Integer.parseInt(fo_matcher.group(5)));
            result.setMin(Integer.parseInt(fo_matcher.group(6)));
            result.setSec(Integer.parseInt(fo_matcher.group(7)));

            return true;

        } else {
            return false;

        }
    }
}
