package wbif.sjx.MIA.Module.Visualisation.ImageRendering;

import ij.ImagePlus;
import ij.plugin.Duplicator;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.common.Process.IntensityMinMax;

/**
 * Created by sc13967 on 10/08/2017.
 */
public class SetDisplayRange extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String RANGE_SEPARATOR = "Intensity range";
    public static final String CALCULATION_MODE = "Calculation mode";
    public static final String CLIP_FRACTION_MIN = "Clipping fraction (min)";
    public static final String CLIP_FRACTION_MAX = "Clipping fraction (max)";
    public static final String SET_MINIMUM_VALUE = "Set minimum value";
    public static final String SET_MAXIMUM_VALUE = "Set maximum value";
    public static final String MIN_RANGE = "Minimum range value";
    public static final String MAX_RANGE = "Maximum range value";

    public SetDisplayRange(ModuleCollection modules) {
        super("Set intensity display range",modules);
    }


    public interface CalculationModes {
        String FAST = "Fast";
        String MANUAL = "Manual";
        String PRECISE = "Precise";

        String[] ALL = new String[]{FAST,MANUAL,PRECISE};

    }


    public static void setDisplayRangeManual(ImagePlus ipl, String calculationMode, double[] intRange) {
        // Get min max values for whole stack
        for (int c = 1; c <= ipl.getNChannels(); c++) {
            if (ipl.isHyperStack()) ipl.setPosition(c, 1, 1);
            else ipl.setPosition(c);

            ipl.setDisplayRange(intRange[0],intRange[1]);

        }

        // Resetting location of the image
        ipl.setPosition(1,1,1);

    }

    public static void setDisplayRangeAuto(ImagePlus ipl, String calculationMode, double[] clipFraction, boolean[] setRange) {
        for (int c = 1; c <= ipl.getNChannels(); c++) {
            // Get min max values for whole stack
            double[] intRange = new double[]{ipl.getDisplayRangeMin(),ipl.getDisplayRangeMax()};
            double[] newIntRange;

            switch (calculationMode) {
                case CalculationModes.FAST:
                default:
                    newIntRange = IntensityMinMax.getWeightedChannelRangeFast(ipl,c-1,clipFraction[0],clipFraction[1]);
                    break;

                case CalculationModes.PRECISE:
                    newIntRange = IntensityMinMax.getWeightedChannelRangePrecise(ipl,c-1,clipFraction[0],clipFraction[1]);
                    break;
            }

            if (setRange[0]) intRange[0] = newIntRange[0];
            if (setRange[1]) intRange[1] = newIntRange[1];

            ipl.setDisplayRange(intRange[0],intRange[1]);

        }

        // Resetting location of the image
        ipl.setPosition(1,1,1);

    }


    @Override
    public String getPackageName() {
        return PackageNames.VISUALISATION_IMAGE_RENDERING;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String calculationMode = parameters.getValue(CALCULATION_MODE);
        double clipFractionMin = parameters.getValue(CLIP_FRACTION_MIN);
        double clipFractionMax = parameters.getValue(CLIP_FRACTION_MAX);
        boolean setMinimumValue = parameters.getValue(SET_MINIMUM_VALUE);
        boolean setMaximumValue = parameters.getValue(SET_MAXIMUM_VALUE);
        double minRange = parameters.getValue(MIN_RANGE);
        double maxRange = parameters.getValue(MAX_RANGE);

        // If this image doesn't exist, skip this module.  This returns true, because this isn't terminal for the analysis.
        if (inputImage == null) return Status.PASS;

        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) inputImagePlus = new Duplicator().run(inputImagePlus);

        // Setting ranges
        double[] clipFraction = new double[]{clipFractionMin,clipFractionMax};
        boolean[] setRange = new boolean[]{setMinimumValue,setMaximumValue};
        double[] manualRange = new double[]{minRange,maxRange};

        // Adjusting display range
        writeStatus("Adjusting display range");
        switch (calculationMode) {
            case CalculationModes.FAST:
            case CalculationModes.PRECISE:
                setDisplayRangeAuto(inputImagePlus,calculationMode,clipFraction,setRange);
                break;
            case CalculationModes.MANUAL:
                setDisplayRangeManual(inputImagePlus,calculationMode,manualRange);
                break;
        }

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            String outputImageName = parameters.getValue(OUTPUT_IMAGE);
            Image outputImage = new Image(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);
            if (showOutput) outputImage.showImage(outputImageName,null,false,true);

        } else {
            if (showOutput) inputImage.showImage(inputImageName,null,false,true);

        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this,true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(RANGE_SEPARATOR,this));
        parameters.add(new ChoiceP(CALCULATION_MODE,this,CalculationModes.FAST,CalculationModes.ALL));
        parameters.add(new DoubleP(CLIP_FRACTION_MIN,this,0d));
        parameters.add(new DoubleP(CLIP_FRACTION_MAX,this,0d));
        parameters.add(new BooleanP(SET_MINIMUM_VALUE,this,true));
        parameters.add(new BooleanP(SET_MAXIMUM_VALUE,this,true));
        parameters.add(new DoubleP(MIN_RANGE,this,0));
        parameters.add(new DoubleP(MAX_RANGE,this,255));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(RANGE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CALCULATION_MODE));
        switch ((String) parameters.getValue(CALCULATION_MODE)) {
            case CalculationModes.FAST:
            case CalculationModes.PRECISE:
                returnedParameters.add(parameters.getParameter(CLIP_FRACTION_MIN));
                returnedParameters.add(parameters.getParameter(CLIP_FRACTION_MAX));
                returnedParameters.add(parameters.getParameter(SET_MINIMUM_VALUE));
                returnedParameters.add(parameters.getParameter(SET_MAXIMUM_VALUE));
                break;

            case CalculationModes.MANUAL:
                returnedParameters.add(parameters.getParameter(MIN_RANGE));
                returnedParameters.add(parameters.getParameter(MAX_RANGE));
                break;
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}