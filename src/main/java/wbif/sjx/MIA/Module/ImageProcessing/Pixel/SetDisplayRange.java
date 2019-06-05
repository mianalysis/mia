package wbif.sjx.MIA.Module.ImageProcessing.Pixel;

import ij.ImagePlus;
import ij.plugin.Duplicator;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
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
    public static final String CLIP_FRACTION = "Clipping fraction";
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

    public static void setDisplayRangeAuto(ImagePlus ipl, String calculationMode, double clipFraction) {
        // Get min max values for whole stack
        double[] intRange;
        for (int c = 1; c <= ipl.getNChannels(); c++) {
            switch (calculationMode) {
                case CalculationModes.FAST:
                default:
                    intRange = IntensityMinMax.getWeightedChannelRangeFast(ipl, c - 1, clipFraction);
                    break;

                case CalculationModes.PRECISE:
                    intRange = IntensityMinMax.getWeightedChannelRangePrecise(ipl, c - 1, clipFraction);
                    break;
            }

            ipl.setDisplayRange(intRange[0],intRange[1]);

        }

        // Resetting location of the image
        ipl.setPosition(1,1,1);

    }


    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_PIXEL;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String calculationMode = parameters.getValue(CALCULATION_MODE);
        double clipFraction = parameters.getValue(CLIP_FRACTION);
        double minRange = parameters.getValue(MIN_RANGE);
        double maxRange = parameters.getValue(MAX_RANGE);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) inputImagePlus = new Duplicator().run(inputImagePlus);

        // Adjusting display range
        writeMessage("Adjusting display range");
        switch (calculationMode) {
            case CalculationModes.FAST:
            case CalculationModes.PRECISE:
                setDisplayRangeAuto(inputImagePlus,calculationMode,clipFraction);
                break;
            case CalculationModes.MANUAL:
                setDisplayRangeManual(inputImagePlus,calculationMode,new double[]{minRange,maxRange});
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

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this,true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new ParamSeparatorP(RANGE_SEPARATOR,this));
        parameters.add(new ChoiceP(CALCULATION_MODE,this,CalculationModes.FAST,CalculationModes.ALL));
        parameters.add(new DoubleP(CLIP_FRACTION,this,0d));
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
                returnedParameters.add(parameters.getParameter(CLIP_FRACTION));
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
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }

}