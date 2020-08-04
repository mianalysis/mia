package wbif.sjx.MIA.Module.ImageProcessing.Pixel;

import com.drew.lang.annotations.Nullable;import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.process.ImageProcessor;
import ij.process.LUT;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.common.Object.Point;
import wbif.sjx.common.Process.IntensityMinMax;

import java.awt.*;

/**
 * Created by sc13967 on 10/08/2017.
 */
public class NormaliseIntensity extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String OUTPUT_SEPARATOR = "Output controls";
    public static final String REGION_MODE = "Region mode";
    public static final String INPUT_OBJECTS = "Input objects";

    public static final String NORMALISATION_SEPARATOR = "Intensity normalisation";
    public static final String CALCULATION_MODE = "Calculation mode";
    public static final String CLIP_FRACTION_MIN = "Clipping fraction (min)";
    public static final String CLIP_FRACTION_MAX = "Clipping fraction (max)";
    public static final String MIN_RANGE = "Minimum range value";
    public static final String MAX_RANGE = "Maximum range value";

    public NormaliseIntensity(ModuleCollection modules) {
        super("Normalise intensity",modules);
    }


    public interface RegionModes {
        String ENTIRE_IMAGE = "Entire image";
        String PER_OBJECT = "Per object";

        String[] ALL = new String[]{ENTIRE_IMAGE,PER_OBJECT};

    }

    public interface CalculationModes {
        String FAST = "Fast";
        String MANUAL = "Manual";
        String PRECISE = "Precise";

        String[] ALL = new String[]{FAST,MANUAL,PRECISE};

    }


    public static void applyNormalisation(ImagePlus ipl, String calculationMode, double[] clipFraction, @Nullable double[] intRange) {
        applyNormalisation(ipl,calculationMode,clipFraction,intRange,null);
    }

    public static void applyNormalisation(ImagePlus ipl, String calculationMode, double[] clipFraction, @Nullable double[] intRange, @Nullable Obj maskObject) {
        int bitDepth = ipl.getProcessor().getBitDepth();

        // Get min max values for whole stack
        if (maskObject == null) {
            for (int c = 1; c <= ipl.getNChannels(); c++) {
                switch (calculationMode) {
                    case CalculationModes.FAST:
                        intRange = IntensityMinMax.getWeightedChannelRangeFast(ipl,c-1,clipFraction[0],clipFraction[1]);
                        break;

                    case CalculationModes.PRECISE:
                        intRange = IntensityMinMax.getWeightedChannelRangePrecise(ipl,c-1,clipFraction[0],clipFraction[1]);
                        break;
                }

                if (intRange == null) return;

                double min = intRange[0];
                double max = intRange[1];

                // Applying normalisation
                double factor = bitDepth == 32 ? 1 : Math.pow(2, bitDepth) - 1;
                double mult = factor / (max - min);

                for (int z = 1; z <= ipl.getNSlices(); z++) {
                    for (int t = 1; t <= ipl.getNFrames(); t++) {
                        ipl.setPosition(c, z, t);
                        ipl.getProcessor().subtract(min);
                        ipl.getProcessor().multiply(mult);
                    }
                }
            }

        } else {
            for (int c = 1; c <= ipl.getNChannels(); c++) {
                int frame = maskObject.getT();
                switch (calculationMode) {
                    case CalculationModes.FAST:
                        intRange = IntensityMinMax.getWeightedChannelRangeFast(ipl,maskObject,c-1,frame,clipFraction[0],clipFraction[1]);
                        break;

                    case CalculationModes.PRECISE:
                        intRange = IntensityMinMax.getWeightedChannelRangePrecise(ipl,maskObject,c-1,frame,clipFraction[0],clipFraction[1]);
                        break;
                }

                if (intRange == null) return;

                double min = intRange[0];
                double max = intRange[1];

                // Applying normalisation
                double factor = bitDepth == 32 ? 1 : Math.pow(2, bitDepth) - 1;
                double mult = factor / (max - min);

                for (Point<Integer> point:maskObject.getPoints()) {
                    ipl.setPosition(c,point.getZ()+1,frame+1);

                    ImageProcessor ipr = ipl.getProcessor();
                    double val = ipr.getf(point.getX(),point.getY());
                    val = (val - min)*mult;
                    ipl.getProcessor().setf(point.getX(),point.getY(),(float) val);

                }
            }
        }

        // Resetting location of the image
        ipl.setPosition(1,1,1);

        // Set brightness/contrast
        IntensityMinMax.run(ipl,true);

    }


    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_PIXEL;
    }

    @Override
    public String getDescription() {
        return "Sets the intensity to maximise the dynamic range of the image.\n" +
                "\"Clipping fraction\" is the fraction of pixels at either end of the range that gets clipped." +
                "The \"Per object\" region mode will normalise all pixels within each object.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String regionMode = parameters.getValue(REGION_MODE);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String calculationMode = parameters.getValue(CALCULATION_MODE);
        double clipFractionMin = parameters.getValue(CLIP_FRACTION_MIN);
        double clipFractionMax = parameters.getValue(CLIP_FRACTION_MAX);
        double minRange = parameters.getValue(MIN_RANGE);
        double maxRange = parameters.getValue(MAX_RANGE);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) inputImagePlus = new Duplicator().run(inputImagePlus);

        // Setting ranges
        double[] clipFraction = new double[]{clipFractionMin,clipFractionMax};
        double[] intRange = new double[]{minRange,maxRange};

        // Running intensity normalisation
                switch (regionMode) {
                    case RegionModes.ENTIRE_IMAGE:
                        writeStatus("Applying entire-image pixel normalisation");
                        applyNormalisation(inputImagePlus,calculationMode,clipFraction,intRange);
                        break;

                    case RegionModes.PER_OBJECT:
                        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);
                        int count = 0;
                        int total = inputObjects.size();
                        for (Obj inputObject:inputObjects.values()) {
                            writeStatus("Processing "+(++count)+" of "+total+" objects");
                            applyNormalisation(inputImagePlus,calculationMode,clipFraction,intRange,inputObject);
                        }
                        break;
                }

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            String outputImageName = parameters.getValue(OUTPUT_IMAGE);
            Image outputImage = new Image(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);
            if (showOutput) outputImage.showImage(outputImageName,LUT.createLutFromColor(Color.WHITE),false,true);

        } else {
            if (showOutput) inputImage.showImage(inputImageName,LUT.createLutFromColor(Color.WHITE),false,true);

        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this,true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new ParamSeparatorP(OUTPUT_SEPARATOR,this));
        parameters.add(new ChoiceP(REGION_MODE,this,RegionModes.ENTIRE_IMAGE,RegionModes.ALL));
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this));

        parameters.add(new ParamSeparatorP(NORMALISATION_SEPARATOR,this));
        parameters.add(new ChoiceP(CALCULATION_MODE,this,CalculationModes.FAST,CalculationModes.ALL));
        parameters.add(new DoubleP(CLIP_FRACTION_MIN,this,0d));
        parameters.add(new DoubleP(CLIP_FRACTION_MAX,this,0d));
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

        returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));
            returnedParameters.add(parameters.getParameter(REGION_MODE));
            switch ((String) parameters.getValue(REGION_MODE)) {
                case RegionModes.PER_OBJECT:
                    returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
                    break;
            }

        returnedParameters.add(parameters.getParameter(NORMALISATION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CALCULATION_MODE));
        switch ((String) parameters.getValue(CALCULATION_MODE)) {
            case CalculationModes.FAST:
            case CalculationModes.PRECISE:
                returnedParameters.add(parameters.getParameter(CLIP_FRACTION_MIN));
                returnedParameters.add(parameters.getParameter(CLIP_FRACTION_MAX));
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