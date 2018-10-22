package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import edu.emory.mathcs.backport.java.util.Arrays;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.plugin.SubHyperstackMaker;
import net.imglib2.ops.parse.token.Int;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Process.IntensityMinMax;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by sc13967 on 18/01/2018.
 */
public class ExtractSubstack extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String SELECTION_MODE = "Selection mode";
//    public static final String USE_ALL_C = "Use all channels";
//    public static final String STARTING_C = "Starting channel";
//    public static final String ENDING_C = "Ending channel";
//    public static final String INTERVAL_C = "Channel interval";
//    public static final String USE_ALL_Z = "Use all Z-slices";
//    public static final String STARTING_Z = "Starting Z-slice";
//    public static final String ENDING_Z = "Ending Z-slice";
//    public static final String INTERVAL_Z = "Slice interval";
//    public static final String USE_ALL_T = "Use all timepoints";
//    public static final String STARTING_T = "Starting timepoint";
//    public static final String ENDING_T = "Ending timepoint";
//    public static final String INTERVAL_T = "Timepoint interval";
    public static final String CHANNELS = "Channels";
    public static final String SLICES = "Slices";
    public static final String FRAMES = "Frames";


    public interface SelectionModes {
        String FIXED = "Fixed";
        String MANUAL = "Manual";

        String[] ALL = new String[]{FIXED,MANUAL};

    }

    public static int[] interpretRange(String range) {
        // Creating a TreeSet to store the indices we've collected.  This will order numerically and remove duplicates.
        TreeSet<Integer> values = new TreeSet<>();

        // Removing white space
        range = range.replaceAll("\\s","");

        // Setting patterns for ranges and values
        Pattern singleRangePattern = Pattern.compile("^(\\d+)-(\\d+)$");
        Pattern singleRangeEndPattern = Pattern.compile("^(\\d+)-end$");
        Pattern intervalRangePattern = Pattern.compile("^(\\d+)-(\\d+)-(\\d+)$");
        Pattern intervalRangeEndPattern = Pattern.compile("^(\\d+)-end-(\\d+)$");
        Pattern singleValuePattern = Pattern.compile("^\\d+$");

        // First, splitting comma-delimited sections
        StringTokenizer stringTokenizer = new StringTokenizer(range,",");
        while (stringTokenizer.hasMoreTokens()) {
            String token = stringTokenizer.nextToken();

            // If it matches the single range pattern process as a range, otherwise, check if it's a single value.
            Matcher singleRangeMatcher = singleRangePattern.matcher(token);
            Matcher singleRangeEndMatcher = singleRangeEndPattern.matcher(token);
            Matcher intervalRangeMatcher = intervalRangePattern.matcher(token);
            Matcher intervalRangeEndMatcher = intervalRangeEndPattern.matcher(token);
            Matcher singleValueMatcher = singleValuePattern.matcher(token);

            if (singleRangeMatcher.matches()) {
                int start = Integer.parseInt(singleRangeMatcher.group(1));
                int end = Integer.parseInt(singleRangeMatcher.group(2));

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

                for (int value=start;value<=end;value = value + interval) values.add(value);

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

        // Returning an array of the indices
        return values.stream().mapToInt(Integer::intValue).toArray();

    }

    public static int[] extendRangeToEnd(int[] inputRange, int end) {
        // Adding the numbers to a TreeSet, then returning as an array
        TreeSet<Integer> values = new TreeSet<>();

        // Adding the explicitly-named values
        for (int i=0;i<inputRange.length-3;i++) values.add(inputRange[i]);

        // Adding the range values
        int start = inputRange[inputRange.length-3];
        int interval = inputRange[inputRange.length-2] - start;
        for (int i=start;i<=end;i=i+interval) values.add(i);

        return values.stream().mapToInt(Integer::intValue).toArray();

    }

    @Override
    public String getTitle() {
        return "Extract substack";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_STACK;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected void run(Workspace workspace) throws GenericMIAException {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
//        boolean useAllC = parameters.getValue(USE_ALL_C);
//        int startingC = parameters.getValue(STARTING_C);
//        int endingC = parameters.getValue(ENDING_C);
//        int intervalC = parameters.getValue(INTERVAL_C);
//        boolean useAllZ = parameters.getValue(USE_ALL_Z);
//        int startingZ = parameters.getValue(STARTING_Z);
//        int endingZ = parameters.getValue(ENDING_Z);
//        int intervalZ = parameters.getValue(INTERVAL_Z);
//        boolean useAllT = parameters.getValue(USE_ALL_T);
//        int startingT = parameters.getValue(STARTING_T);
//        int endingT = parameters.getValue(ENDING_T);
//        int intervalT = parameters.getValue(INTERVAL_T);
        String channels = parameters.getValue(CHANNELS);
        String slices = parameters.getValue(SLICES);
        String frames = parameters.getValue(FRAMES);

        int[] channelsList = interpretRange(channels);
        if (channelsList[channelsList.length-1] == Integer.MAX_VALUE) channelsList = extendRangeToEnd(channelsList,inputImagePlus.getNChannels());

        int[] slicesList = interpretRange(slices);
        if (slicesList[slicesList.length-1] == Integer.MAX_VALUE) slicesList = extendRangeToEnd(slicesList,inputImagePlus.getNSlices());

        int[] framesList = interpretRange(frames);
        if (framesList[framesList.length-1] == Integer.MAX_VALUE) framesList = extendRangeToEnd(framesList,inputImagePlus.getNFrames());

        List<Integer> cList = java.util.Arrays.stream(channelsList).boxed().collect(Collectors.toList());
        List<Integer> zList = java.util.Arrays.stream(slicesList).boxed().collect(Collectors.toList());
        List<Integer> tList = java.util.Arrays.stream(framesList).boxed().collect(Collectors.toList());

        // Generating the substack and adding to the workspace
        ImagePlus outputImagePlus =  SubHyperstackMaker.makeSubhyperstack(inputImagePlus,cList,zList,tList).duplicate();
        Image outputImage = new Image(outputImageName,outputImagePlus);
        workspace.addImage(outputImage);

        // If selected, displaying the image
        if (showOutput) {
            ImagePlus dispIpl = new Duplicator().run(outputImagePlus);
            IntensityMinMax.run(dispIpl,true);

            dispIpl.show();
        }
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.add(new Parameter(SELECTION_MODE, Parameter.CHOICE_ARRAY, SelectionModes.FIXED, SelectionModes.ALL));
//        parameters.add(new Parameter(USE_ALL_C, Parameter.BOOLEAN,true));
//        parameters.add(new Parameter(STARTING_C, Parameter.INTEGER,1));
//        parameters.add(new Parameter(ENDING_C, Parameter.INTEGER,1));
//        parameters.add(new Parameter(INTERVAL_C, Parameter.INTEGER,1));
//        parameters.add(new Parameter(USE_ALL_Z, Parameter.BOOLEAN,true));
//        parameters.add(new Parameter(STARTING_Z, Parameter.INTEGER,1));
//        parameters.add(new Parameter(ENDING_Z, Parameter.INTEGER,1));
//        parameters.add(new Parameter(INTERVAL_Z, Parameter.INTEGER,1));
//        parameters.add(new Parameter(USE_ALL_T, Parameter.BOOLEAN,true));
//        parameters.add(new Parameter(STARTING_T, Parameter.INTEGER,1));
//        parameters.add(new Parameter(ENDING_T, Parameter.INTEGER,1));
//        parameters.add(new Parameter(INTERVAL_T, Parameter.INTEGER,1));
        parameters.add(new Parameter(CHANNELS,Parameter.STRING,""));
        parameters.add(new Parameter(SLICES,Parameter.STRING,""));
        parameters.add(new Parameter(FRAMES,Parameter.STRING,""));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(SELECTION_MODE));
        switch ((String) parameters.getValue(SELECTION_MODE)) {
            case SelectionModes.FIXED:
                returnedParameters.add(parameters.getParameter(CHANNELS));
                returnedParameters.add(parameters.getParameter(SLICES));
                returnedParameters.add(parameters.getParameter(FRAMES));
                break;
        }

//        returnedParameters.add(parameters.getParameter(STARTING_C));
//        returnedParameters.add(parameters.getParameter(INTERVAL_C));
//        returnedParameters.add(parameters.getParameter(USE_ALL_C));
//        if (!(boolean) parameters.getValue(USE_ALL_C)) {
//            returnedParameters.add(parameters.getParameter(ENDING_C));
//        }
//
//        returnedParameters.add(parameters.getParameter(STARTING_Z));
//        returnedParameters.add(parameters.getParameter(INTERVAL_Z));
//        returnedParameters.add(parameters.getParameter(USE_ALL_Z));
//        if (!(boolean) parameters.getValue(USE_ALL_Z)) {
//            returnedParameters.add(parameters.getParameter(ENDING_Z));
//        }
//
//        returnedParameters.add(parameters.getParameter(STARTING_T));
//        returnedParameters.add(parameters.getParameter(INTERVAL_T));
//        returnedParameters.add(parameters.getParameter(USE_ALL_T));
//        if (!(boolean) parameters.getValue(USE_ALL_T)) {
//            returnedParameters.add(parameters.getParameter(ENDING_T));
//        }

        return returnedParameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
