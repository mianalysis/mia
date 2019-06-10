package wbif.sjx.MIA.Process;

import java.util.LinkedHashSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class CommaSeparatedStringInterpreter {
    public static int[] interpretIntegers(String range, boolean ascendingOrder) {
        // Creating a TreeSet to store the indices we've collected.  This will order numerically and remove duplicates.
        LinkedHashSet<Integer> values = new LinkedHashSet<>();

        // Removing white space
        range = range.replaceAll("\\s","");

        // Setting patterns for ranges and values
        Pattern singleRangePattern = Pattern.compile("^([-]?[\\d]+)-([-]?[\\d]+)$");
        Pattern singleRangeEndPattern = Pattern.compile("^([-]?[\\d]+)-end$");
        Pattern intervalRangePattern = Pattern.compile("^([-]?[\\d]+)-([-]?[\\d]+)-([-]?[\\d]+)$");
        Pattern intervalRangeEndPattern = Pattern.compile("^([-]?[\\d]+)-end-([-]?[\\d]+)$");
        Pattern singleValuePattern = Pattern.compile("^[-]?[\\d]+$");

        // First, splitting comma-delimited sections
        StringTokenizer stringTokenizer = new StringTokenizer(range,",");
        while (stringTokenizer.hasMoreTokens()) {
            String token = stringTokenizer.nextToken();

            // If it matches the single range pattern processAutomatic as a range, otherwise, check if it's a single value.
            Matcher singleRangeMatcher = singleRangePattern.matcher(token);
            Matcher singleRangeEndMatcher = singleRangeEndPattern.matcher(token);
            Matcher intervalRangeMatcher = intervalRangePattern.matcher(token);
            Matcher intervalRangeEndMatcher = intervalRangeEndPattern.matcher(token);
            Matcher singleValueMatcher = singleValuePattern.matcher(token);

            if (singleRangeMatcher.matches()) {
                int start = Integer.parseInt(singleRangeMatcher.group(1));
                int end = Integer.parseInt(singleRangeMatcher.group(2));
//                int interval = (end > start) ? 1 : -1;
//                int nValues = (end-start)/interval + 1;
//
//                for (int i=0;i<nValues;i++) {
//                    values.add(start);
//                    start = start + interval;
//                }

                for (int value=start;value<=end;value++) values.add(value);

            } else if (singleRangeEndMatcher.matches()) {
                // If the numbers should proceed to the end, the last three added are the starting number, the starting
                // number plus one and the maximum value
                int start = Integer.parseInt(singleRangeEndMatcher.group(1));

                values.add(start);
                values.add(start+1);
                values.add(Integer.MAX_VALUE);

            } else if (intervalRangeMatcher.matches()) {
                int start = Integer.parseInt(intervalRangeMatcher.group(1));
                int end = Integer.parseInt(intervalRangeMatcher.group(2));
                int interval = Integer.parseInt(intervalRangeMatcher.group(3));
                int nValues = (end-start)/interval + 1;

                for (int i=0;i<nValues;i++) {
                    values.add(start);
                    start = start + interval;
                }

            } else if (intervalRangeEndMatcher.matches()) {
                // If the numbers should proceed to the end, the last three added are the starting number, the starting
                // number plus the interval and the maximum value
                int start = Integer.parseInt(intervalRangeEndMatcher.group(1));
                int interval = Integer.parseInt(intervalRangeEndMatcher.group(2));

                values.add(start);
                values.add(start+interval);
                values.add(Integer.MAX_VALUE);

            } else if (singleValueMatcher.matches()) {
                values.add(Integer.parseInt(token));

            }
        }

        // Returning an array of the indices.  If they should be in ascending order, put them in a TreeSet first
        if (ascendingOrder) return new TreeSet<>(values).stream().mapToInt(Integer::intValue).toArray();
        else return values.stream().mapToInt(Integer::intValue).toArray();

    }
}
