package io.github.mianalysis.mia.process.string;

import java.util.LinkedHashSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommaSeparatedStringInterpreter {
    public static int firstValue(String range) {
        Matcher matcher = Pattern.compile("[0-9]+").matcher(range);

        if (matcher.find())
            return Integer.valueOf(matcher.group(0));
        else
            return Integer.MAX_VALUE;
    }

    public static String removeEndRanges(String range, int endValue) {
        // Setting patterns for ranges and values
        String num = "\\d(?:\\.\\d+)";

        // Replacing any pre-end ranges (e.g. (end-4) which must be enclosed in
        // brackets)
        Pattern preEndRange = Pattern.compile("(\\(end-[" + num + "]+\\))");
        Matcher preEndRangeMatcher = preEndRange.matcher(range);
        StringBuffer out = new StringBuffer();
        while (preEndRangeMatcher.find()) {
            String oldValue = preEndRangeMatcher.group();
            int newValue = endValue - Integer.parseInt(oldValue.substring(5, oldValue.length() - 1));
            preEndRangeMatcher.appendReplacement(out, String.valueOf(newValue));
        }
        preEndRangeMatcher.appendTail(out);
        range = out.toString();

        // Replacing any simple "end" values
        range = range.replaceAll("end", String.valueOf(endValue));

        return range;

    }

    public static String removeInterval(String range) {
        // Removes anything after a potential second hyphen
        Pattern pattern = Pattern.compile("-");
        Matcher matcher = pattern.matcher(range.substring(1));
        int count = 0;
        while (matcher.find())
            if (count++ > 0)
                return range.substring(0, matcher.start());

        return range;

    }

    public static int[] interpretIntegers(String range, boolean ascendingOrder, int endValue) {
        // Creating a TreeSet to store the indices we've collected. This will order
        // numerically and remove duplicates.
        LinkedHashSet<Integer> values = new LinkedHashSet<>();

        // Removing white space
        range = range.replaceAll("\\s", "");

        // Removing sections involving "end"
        range = removeEndRanges(range, endValue);

        // Setting patterns for ranges and values
        String num = "\\d(?:\\.\\d+)";

        Pattern singleRangePattern = Pattern.compile("^([-]?[" + num + "]+)-([-]?[" + num + "]+)$");
        Pattern intervalRangePattern = Pattern
                .compile("^([-]?[" + num + "]+)-([-]?[" + num + "]+)-([-]?[" + num + "]+)$");
        Pattern singleValuePattern = Pattern.compile("^[-]?[" + num + "]+$");

        // First, splitting comma-delimited sections
        StringTokenizer stringTokenizer = new StringTokenizer(range, ",");
        while (stringTokenizer.hasMoreTokens()) {
            String token = stringTokenizer.nextToken();

            // If it matches the single range pattern processAutomatic as a range,
            // otherwise, check if it's a single value.
            Matcher singleRangeMatcher = singleRangePattern.matcher(token);
            Matcher intervalRangeMatcher = intervalRangePattern.matcher(token);
            Matcher singleValueMatcher = singleValuePattern.matcher(token);

            if (singleRangeMatcher.matches()) {
                int start = (int) Double.parseDouble(singleRangeMatcher.group(1));
                int end = (int) Double.parseDouble(singleRangeMatcher.group(2));
                int interval = (end >= start) ? 1 : -1;
                int nValues = (end - start) / interval + 1;

                for (int i = 0; i < nValues; i++) {
                    values.add(start);
                    start = start + interval;
                }

            } else if (intervalRangeMatcher.matches()) {
                int start = (int) Double.parseDouble(intervalRangeMatcher.group(1));
                int end = (int) Double.parseDouble(intervalRangeMatcher.group(2));
                int interval = (int) Double.parseDouble(intervalRangeMatcher.group(3));
                int nValues = (end - start) / interval + 1;

                for (int i = 0; i < nValues; i++) {
                    values.add(start);
                    start = start + interval;
                }

            } else if (singleValueMatcher.matches()) {
                values.add((int) Double.parseDouble(token));

            }
        }

        // Returning an array of the indices. If they should be in ascending order,
        // put them in a TreeSet first
        if (ascendingOrder)
            return new TreeSet<>(values).stream().mapToInt(Integer::intValue).toArray();
        else
            return values.stream().mapToInt(Integer::intValue).toArray();

    }

    // public static int[] interpretIntegers(String range, boolean ascendingOrder,
    // int end) {
    // // Getting the list of values
    // int[] values = interpretIntegers(range, ascendingOrder);

    // // If the final value is Integer.MAX_VALUE extending to the specified end
    // if (values[values.length-1] == Integer.MAX_VALUE) values =
    // extendRangeToEnd(values,end);

    // return values;

    // }

    public static int[] extendRangeToEnd(int[] inputRange, int end) {
        // Adding the numbers to a TreeSet, then returning as an array
        TreeSet<Integer> values = new TreeSet<>();

        // Checking for the special case where the only value is Integer.MAX_VALUE (i.e.
        // the range was only "end")
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
