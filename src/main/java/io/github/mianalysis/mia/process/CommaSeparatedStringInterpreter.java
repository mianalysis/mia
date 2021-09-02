package io.github.mianalysis.mia.process;

import java.util.LinkedHashSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommaSeparatedStringInterpreter {
    public static int[] interpretIntegers(String range, boolean ascendingOrder) {
        // Creating a TreeSet to store the indices we've collected.  This will order numerically and remove duplicates.
        LinkedHashSet<Integer> values = new LinkedHashSet<>();

        // Removing white space
        range = range.replaceAll("\\s","");

        // Setting patterns for ranges and values
        String num = "\\d(?:\\.\\d+)";
        Pattern singleRangePattern = Pattern.compile("^([-]?["+num+"]+)-([-]?["+num+"]+)$");
        Pattern singleRangeEndPattern = Pattern.compile("^([-]?["+num+"]+)-end$");
        Pattern intervalRangePattern = Pattern.compile("^([-]?["+num+"]+)-([-]?["+num+"]+)-([-]?["+num+"]+)$");
        Pattern intervalRangeEndPattern = Pattern.compile("^([-]?["+num+"]+)-end-([-]?["+num+"]+)$");
        Pattern singleValuePattern = Pattern.compile("^[-]?["+num+"]+$");
        Pattern endOnlyPattern = Pattern.compile("end");

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
            Matcher endOnlyMatcher = endOnlyPattern.matcher(token);

            if (singleRangeMatcher.matches()) {
                int start = (int) Double.parseDouble(singleRangeMatcher.group(1));
                int end = (int) Double.parseDouble(singleRangeMatcher.group(2));
                int interval = (end >= start) ? 1 : -1;
                int nValues = (end-start)/interval + 1;

                for (int i=0;i<nValues;i++) {
                    values.add(start);
                    start = start + interval;
                }

            } else if (singleRangeEndMatcher.matches()) {
                // If the numbers should proceed to the end, the last three added are the starting number, the starting
                // number plus one and the maximum value
                int start = (int) Double.parseDouble(singleRangeEndMatcher.group(1));

                values.add(start);
                values.add(start+1);
                values.add(Integer.MAX_VALUE);

            } else if (intervalRangeMatcher.matches()) {
                int start = (int) Double.parseDouble(intervalRangeMatcher.group(1));
                int end = (int) Double.parseDouble(intervalRangeMatcher.group(2));
                int interval = (int) Double.parseDouble(intervalRangeMatcher.group(3));
                int nValues = (end-start)/interval + 1;

                for (int i=0;i<nValues;i++) {
                    values.add(start);
                    start = start + interval;
                }

            } else if (intervalRangeEndMatcher.matches()) {
                // If the numbers should proceed to the end, the last three added are the starting number, the starting
                // number plus the interval and the maximum value
                int start = (int) Double.parseDouble(intervalRangeEndMatcher.group(1));
                int interval = (int) Double.parseDouble(intervalRangeEndMatcher.group(2));

                values.add(start);
                values.add(start+interval);
                values.add(Integer.MAX_VALUE);

            } else if (singleValueMatcher.matches()) {
                values.add((int) Double.parseDouble(token));

            } else if (endOnlyMatcher.matches()) {
                values.add(Integer.MAX_VALUE);

            }
        }

        // Returning an array of the indices.  If they should be in ascending order, put them in a TreeSet first
        if (ascendingOrder) return new TreeSet<>(values).stream().mapToInt(Integer::intValue).toArray();
        else return values.stream().mapToInt(Integer::intValue).toArray();

    }

    public static int[] interpretIntegers(String range, boolean ascendingOrder, int end) {
        // Getting the list of values
        int[] values = interpretIntegers(range, ascendingOrder);

        // If the final value is Integer.MAX_VALUE extending to the specified end
        if (values[values.length-1] == Integer.MAX_VALUE) values = extendRangeToEnd(values,end);

        return values;

    }

    public static int[] extendRangeToEnd(int[] inputRange, int end) {
        // Adding the numbers to a TreeSet, then returning as an array
        TreeSet<Integer> values = new TreeSet<>();

        // Checking for the special case where the only value is Integer.MAX_VALUE (i.e. the range was only "end")
        if (inputRange.length == 1 && inputRange[0] == Integer.MAX_VALUE) {
            values.add(end);
        } else if (inputRange.length == 1) {
            values.add(inputRange[0]);
        } else {
            // Adding the explicitly-named values
            for (int i = 0; i < inputRange.length - 3; i++)
                values.add(inputRange[i]);

            // Adding the range values
            int start = inputRange[inputRange.length - 3];
            int interval = inputRange[inputRange.length - 2] - start;
            for (int i = start; i <= end; i = i + interval)
                values.add(i);

        }

        return values.stream().mapToInt(Integer::intValue).toArray();

    }

}
