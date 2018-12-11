package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel;

import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.process.ImageProcessor;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Object.Point;
import wbif.sjx.common.Process.IntensityMinMax;

import com.drew.lang.annotations.Nullable;

/**
 * Created by sc13967 on 10/08/2017.
 */
public class NormaliseIntensity extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String REGION_MODE = "Region mode";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String CALCULATION_MODE = "Calculation mode";
    public static final String CLIP_FRACTION = "Clipping fraction";

    public interface RegionModes {
        String ENTIRE_IMAGE = "Entire image";
        String PER_OBJECT = "Per object";

        String[] ALL = new String[]{ENTIRE_IMAGE,PER_OBJECT};

    }

    public interface CalculationModes {
        String FAST = "Fast";
        String PRECISE = "Precise";

        String[] ALL = new String[]{FAST,PRECISE};

    }


    public static void applyNormalisation(ImagePlus ipl, double clipFraction, String calculationMode) {
        applyNormalisation(ipl,clipFraction,calculationMode,null);
    }

    public static void applyNormalisation(ImagePlus ipl, double clipFraction, String calculationMode, @Nullable Obj maskObject) {
        int bitDepth = ipl.getProcessor().getBitDepth();

        // Get min max values for whole stack
        if (maskObject == null) {
            for (int c = 1; c <= ipl.getNChannels(); c++) {
                double[] range;
                switch (calculationMode) {
                    case CalculationModes.FAST:
                    default:
                        range = IntensityMinMax.getWeightedChannelRangeFast(ipl, c - 1, clipFraction);
                        break;

                    case CalculationModes.PRECISE:
                        range = IntensityMinMax.getWeightedChannelRangePrecise(ipl, c - 1, clipFraction);
                        break;
                }

                double min = range[0];
                double max = range[1];

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
                double[] range;
                switch (calculationMode) {
                    case CalculationModes.FAST:
                    default:
                        range = IntensityMinMax.getWeightedChannelRangeFast(ipl, maskObject, c - 1, frame, clipFraction);
                        break;

                    case CalculationModes.PRECISE:
                        range = IntensityMinMax.getWeightedChannelRangePrecise(ipl, maskObject, c - 1, frame, clipFraction);
                        break;
                }

                double min = range[0];
                double max = range[1];

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
    public String getTitle() {
        return "Normalise intensity";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_PIXEL;
    }

    @Override
    public String getHelp() {
        return "Sets the intensity to maximise the dynamic range of the image.\n" +
                "\"Clipping fraction\" is the fraction of pixels at either end of the range that gets clipped." +
                "The \"Per object\" region mode will normalise all pixels within each object.";
    }

    @Override
    public boolean run(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String regionMode = parameters.getValue(REGION_MODE);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String calculationMode = parameters.getValue(CALCULATION_MODE);
        double clipFraction = parameters.getValue(CLIP_FRACTION);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) inputImagePlus = new Duplicator().run(inputImagePlus);

        // Running intensity normalisation
        switch (regionMode) {
            case RegionModes.ENTIRE_IMAGE:
                applyNormalisation(inputImagePlus,clipFraction,calculationMode);
                break;

            case RegionModes.PER_OBJECT:
                ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);
                int count = 0;
                int total = inputObjects.size();
                for (Obj inputObject:inputObjects.values()) {
                    writeMessage("Processing "+(++count)+" of "+total+" objects");
                    applyNormalisation(inputImagePlus,clipFraction,calculationMode,inputObject);
                }
                break;
        }

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            String outputImageName = parameters.getValue(OUTPUT_IMAGE);
            Image outputImage = new Image(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);
            if (showOutput) showImage(outputImage);

        } else {
            if (showOutput) showImage(inputImage);

        }

        return true;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(APPLY_TO_INPUT, Parameter.BOOLEAN,true));
        parameters.add(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.add(new Parameter(REGION_MODE,Parameter.CHOICE_ARRAY,RegionModes.ENTIRE_IMAGE,RegionModes.ALL));
        parameters.add(new Parameter(INPUT_OBJECTS,Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(CALCULATION_MODE,Parameter.CHOICE_ARRAY,CalculationModes.FAST,CalculationModes.ALL));
        parameters.add(new Parameter(CLIP_FRACTION,Parameter.DOUBLE,0d));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(REGION_MODE));
        switch ((String) parameters.getValue(REGION_MODE)) {
            case RegionModes.PER_OBJECT:
                returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
                break;
        }

        returnedParameters.add(parameters.getParameter(CALCULATION_MODE));
        returnedParameters.add(parameters.getParameter(CLIP_FRACTION));

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
    public MetadataReferenceCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}