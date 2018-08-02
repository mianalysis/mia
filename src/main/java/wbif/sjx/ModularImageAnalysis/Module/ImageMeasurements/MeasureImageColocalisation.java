package wbif.sjx.ModularImageAnalysis.Module.ImageMeasurements;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Analysis.ColocalisationCalculator;
import wbif.sjx.common.MathFunc.CumStat;

import java.util.HashMap;

public class MeasureImageColocalisation extends Module {
    public static final String INPUT_IMAGE_1 = "Input image 1";
    public static final String INPUT_IMAGE_2 = "Input image 2";
    public static final String MASKING_MODE = "Masking mode";
    public static final String INPUT_OBJECTS = "Input objects";


    public interface MaskingModes {
        String NONE = "None";
        String MEASURE_INSIDE_OBJECTS = "Measure inside objects";
        String MEASURE_OUTSIDE_OBJECTS = "Measure outside objects";

        String[] ALL = new String[]{NONE,MEASURE_INSIDE_OBJECTS,MEASURE_OUTSIDE_OBJECTS};

    }

    public interface Measurements {
        String MEAN_PCC = "MEAN_PCC";
    }


    public static Image getMaskImage(ObjCollection objects, Image templateImage, String maskingMode) {
        switch (maskingMode) {
            case MaskingModes.MEASURE_INSIDE_OBJECTS:
                // Creating the new Obj
                HashMap<Integer, Float> hues = objects.getHues(ObjCollection.ColourModes.SINGLE_COLOUR, "", false);
                Image image = objects.convertObjectsToImage("Mask",templateImage.getImagePlus(),ObjCollection.ColourModes.SINGLE_COLOUR,hues);
                InvertIntensity.process(image.getImagePlus());
                return image;

            case MaskingModes.MEASURE_OUTSIDE_OBJECTS:
                // Creating the new Obj
                hues = objects.getHues(ObjCollection.ColourModes.SINGLE_COLOUR, "", false);
                return objects.convertObjectsToImage("Mask",templateImage.getImagePlus(),ObjCollection.ColourModes.SINGLE_COLOUR,hues);

            case MaskingModes.NONE:
                return null;

            default:
                return null;
        }
    }

    public static String getFullName(String imageName2, String measurement) {
        return "COLOCALISATION // "+imageName2+"_"+measurement;
    }

    public static void measurePCC(Image image1, Image image2, Image mask) {
        CumStat cs = new CumStat();
        ImagePlus ipl1 = image1.getImagePlus();
        ImagePlus ipl2 = image2.getImagePlus();
        ImagePlus maskIpl = mask == null ? null : mask.getImagePlus();

        // Iterating over all stacks
        for (int c=0;c<ipl1.getNChannels();c++) {
            for (int t=0;t<ipl1.getNFrames();t++) {
                ipl1.setPosition(c+1,1,t+1);
                ipl2.setPosition(c+1,1,t+1);

                if (mask == null) {
                    cs.addMeasure(ColocalisationCalculator.calculatePCC(ipl1.getStack(),ipl2.getStack()));
                } else {
                    maskIpl.setPosition(c+1,1,t+1);
                    cs.addMeasure(ColocalisationCalculator.calculatePCC(ipl1.getStack(),ipl2.getStack(),maskIpl.getStack()));
                }
            }
        }

        // Adding the measurement to the image
        String name = getFullName(image2.getName(),Measurements.MEAN_PCC);
        image1.addMeasurement(new Measurement(name,cs.getMean()));

    }

    @Override
    public String getTitle() {
        return "Measure image colocalisation";
    }

    @Override
    public String getPackageName() {
        return "Image measurements\\";
    }

    @Override
    public String getHelp() {
        return "Calculates PCC, averaged across all timepoints and channels.";
    }

    @Override
    protected void run(Workspace workspace) throws GenericMIAException {
        // Getting input images
        String imageName1 = parameters.getValue(INPUT_IMAGE_1);
        Image image1 = workspace.getImages().get(imageName1);

        String imageName2 = parameters.getValue(INPUT_IMAGE_2);
        Image image2 = workspace.getImages().get(imageName2);

        // Getting input objects
        String objectName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection objects = workspace.getObjects().get(objectName);

        // Getting parameters
        String maskingMode = parameters.getValue(MASKING_MODE);

        // If objects are to be used as a mask a binary image is created.  Otherwise, null is returned
        Image mask = getMaskImage(objects,image1,maskingMode);

        // Running measurements
        measurePCC(image1,image2,mask);

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE_1,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(INPUT_IMAGE_2,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(MASKING_MODE,Parameter.CHOICE_ARRAY,MaskingModes.NONE,MaskingModes.ALL));
        parameters.add(new Parameter(INPUT_OBJECTS,Parameter.INPUT_OBJECTS,null));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_IMAGE_1));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE_2));

        returnedParameters.add(parameters.getParameter(MASKING_MODE));
        switch ((String) parameters.getValue(MASKING_MODE)) {
            case MaskingModes.MEASURE_INSIDE_OBJECTS:
            case MaskingModes.MEASURE_OUTSIDE_OBJECTS:
                returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
                break;
        }

        return returnedParameters;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        String inputImage1Name = parameters.getValue(INPUT_IMAGE_1);
        String inputImage2Name = parameters.getValue(INPUT_IMAGE_2);

        imageMeasurementReferences.setAllCalculated(false);

        String name = getFullName(inputImage2Name,Measurements.MEAN_PCC);
        MeasurementReference reference = imageMeasurementReferences.getOrPut(name);
        reference.setImageObjName(inputImage1Name);
        reference.setCalculated(true);

        return imageMeasurementReferences;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
