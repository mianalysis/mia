package wbif.sjx.MIA.Module.ImageMeasurements;

import ij.ImagePlus;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.MIA.Process.ColourFactory;
import wbif.sjx.common.Analysis.ColocalisationCalculator;
import wbif.sjx.common.MathFunc.CumStat;

import java.util.HashMap;

public class MeasureImageColocalisation extends Module {
    public static final String INPUT_IMAGE_1 = "Input image 1";
    public static final String INPUT_IMAGE_2 = "Input image 2";
    public static final String MASKING_MODE = "Masking mode";
    public static final String INPUT_OBJECTS = "Input objects";

    public MeasureImageColocalisation(ModuleCollection modules) {
        super(modules);
    }


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
                HashMap<Integer, Float> hues = ColourFactory.getSingleColourHues(objects,ColourFactory.SingleColours.WHITE);
                Image image = objects.convertObjectsToImage("Mask",templateImage,hues,8,false);
                InvertIntensity.process(image.getImagePlus());
                return image;

            case MaskingModes.MEASURE_OUTSIDE_OBJECTS:
                // Creating the new Obj
                hues = ColourFactory.getSingleColourHues(objects,ColourFactory.SingleColours.WHITE);
                return objects.convertObjectsToImage("Mask",templateImage,hues,8,false);

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
        return PackageNames.IMAGE_MEASUREMENTS;
    }

    @Override
    public String getHelp() {
        return "Calculates PCC, averaged across all timepoints and channels.";
    }

    @Override
    public boolean process(Workspace workspace) {
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

        if (showOutput) image1.showMeasurements(this);
        if (showOutput) image2.showMeasurements(this);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE_1,this));
        parameters.add(new InputImageP(INPUT_IMAGE_2,this));
        parameters.add(new ChoiceP(MASKING_MODE,this,MaskingModes.NONE,MaskingModes.ALL));
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this));

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
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        ImageMeasurementRefCollection returnedRefs = new ImageMeasurementRefCollection();

        String inputImage1Name = parameters.getValue(INPUT_IMAGE_1);
        String inputImage2Name = parameters.getValue(INPUT_IMAGE_2);

        String name = getFullName(inputImage2Name,Measurements.MEAN_PCC);
        ImageMeasurementRef reference = imageMeasurementRefs.getOrPut(name);
        reference.setImageName(inputImage1Name);
        returnedRefs.add(reference);

        return returnedRefs;

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
