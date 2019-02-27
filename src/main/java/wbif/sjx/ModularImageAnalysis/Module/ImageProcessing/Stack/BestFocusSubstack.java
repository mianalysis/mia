package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import ij.ImagePlus;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;
import wbif.sjx.common.MathFunc.CumStat;

import java.util.HashMap;

public class BestFocusSubstack extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String RADIUS = "Variance calculation range";
    public static final String CALIBRATED_UNITS = "Calibrated units";
    public static final String BEST_FOCUS_CALCULATION = "Best-focus calculation";
    public static final String SLICES_BELOW = "Slices below best focus";
    public static final String SLICES_ABOVE = "Slices above best focus";
    public static final String CHANNEL_MODE = "Channel mode";
    public static final String CHANNEL = "Channel";

    public interface BestFocusCalculations {
        String MAX_MEAN_VARIANCE = "Largest mean variance";
        String MAX_VARIANCE = "Largest maximum variance";

        String[] ALL = new String[]{MAX_MEAN_VARIANCE,MAX_VARIANCE};

    }

    public interface ChannelModes {
        String USE_ALL = "Use all channels";
        String USE_SINGLE = "Use single channel";

        String[] ALL = new String[]{USE_ALL, USE_SINGLE};

    }

//    public interface Measurements {
//        String MAX_MEAN_VARIANCE = "BEST_FOCUS // MAX_MEAN_VARIANCE";
//        String MAX_MEAN_VARIANCE_SLICE = "BEST_FOCUS // MAX_MEAN_VARIANCE_SLICE";
//        String MAX_VARIANCE = "BEST_FOCUS // MAX_VARIANCE";
//        String MAX_VARIANCE_SLICE = "BEST_FOCUS // MAX_VARIANCE_SLICE";
//
//    }

    public static String getFullName(String measurement, int channel) {
        return measurement + "_(CH" + channel + ")";

    }

    @Override
    public String getTitle() {
        return "Best focus stack";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_MEASUREMENTS;
    }

    @Override
    public String getHelp() {
        return "Extract a Z-substack from an input stack based on an automatically-calculated best-focus slice.  " +
                "Best focus is determined using the local 2D variance of pixels in each slice.  It is possible to " +
                "extract a fixed number of slices above and below the determined best-focus slice.";
    }

    @Override
    protected boolean run(Workspace workspace) {
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);
        ImagePlus inputIpl = inputImage.getImagePlus();

        // Measuring the statistics for each slice
        HashMap<Integer,CumStat> stats = new HashMap<>();
        for (int z=0;z<inputIpl.getNSlices();z++) {
            // Initialising this statistics store
            CumStat cs = new CumStat();
            stats.put(z,cs);

            // Iterating over each
        }

        if (showOutput) inputImage.showMeasurements(this);

        return true;
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE,this,"Image to extract substack from."));
        parameters.add(new DoubleP(RADIUS,this,1d,"Radius of filter for determining best focus stack.  If \"Calibrated units\" is false, this value is in pixel units, but if true this value is in calibrated units."));
        parameters.add(new BooleanP(CALIBRATED_UNITS,this,false,"Controls if the radius is specified in pixel (false) or calibrated (true) units."));
        parameters.add(new ChoiceP(BEST_FOCUS_CALCULATION,this,BestFocusCalculations.MAX_MEAN_VARIANCE,BestFocusCalculations.ALL,"Method for determining the best-focus slice.  \""+BestFocusCalculations.MAX_MEAN_VARIANCE+"\" calculates the mean variance of each slice, then takes the slice with the largeest mean.  \""+BestFocusCalculations.MAX_VARIANCE+"\" simply takes the slice with the largest variance."));
        parameters.add(new IntegerP(SLICES_BELOW,this,0,"Number of slices below the best-focus slice to include in the final substack."));
        parameters.add(new IntegerP(SLICES_ABOVE,this,0,"Number of slices above the best-focus slice to include in the final substack."));
        parameters.add(new ChoiceP(CHANNEL_MODE,this,ChannelModes.USE_SINGLE,ChannelModes.ALL,"How many channels to use when calculating the best-focus slice.  \""+ChannelModes.USE_ALL+"\" will use all channels, whereas \""+ChannelModes.USE_SINGLE+"\" will base the calculation on a single, user-defined channel."));
        parameters.add(new IntegerP(CHANNEL,this,1,"Channel to base the best-focus calculation on."));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(RADIUS));
        returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));
        returnedParameters.add(parameters.getParameter(SLICES_BELOW));
        returnedParameters.add(parameters.getParameter(SLICES_ABOVE));

        returnedParameters.add(parameters.getParameter(CHANNEL_MODE));
        switch ((String) parameters.getValue(CHANNEL_MODE)) {
            case ChannelModes.USE_SINGLE:
                returnedParameters.add(parameters.getParameter(CHANNEL));
                break;
        }

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
//        imageMeasurementRefs.setAllCalculated(false);
//
//        String inputImageName = parameters.getValue(INPUT_IMAGE);
//
//        MeasurementRef measurementRef = new MeasurementRef(Measurements.MAX_MEAN_VARIANCE);
//        measurementRef.setCalculated(true);
//        measurementRef.setImageObjName(inputImageName);
//        imageMeasurementRefs.add(measurementRef);
//
//        measurementRef = new MeasurementRef(Measurements.MAX_MEAN_VARIANCE_SLICE);
//        measurementRef.setCalculated(true);
//        measurementRef.setImageObjName(inputImageName);
//        imageMeasurementRefs.add(measurementRef);
//
//        measurementRef = new MeasurementRef(Measurements.MAX_VARIANCE);
//        measurementRef.setCalculated(true);
//        measurementRef.setImageObjName(inputImageName);
//        imageMeasurementRefs.add(measurementRef);
//
//        measurementRef = new MeasurementRef(Measurements.MAX_VARIANCE_SLICE);
//        measurementRef.setCalculated(true);
//        measurementRef.setImageObjName(inputImageName);
//        imageMeasurementRefs.add(measurementRef);
//
//        return imageMeasurementRefs;

        return null;

    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
