package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import fiji.threshold.Auto_Threshold;
import ij.ImagePlus;
import ij.plugin.Filters3D;
import inra.ijpb.morphology.Morphology;
import inra.ijpb.morphology.Strel3D;
import inra.ijpb.plugins.MorphologicalFilter3DPlugin;
import inra.ijpb.segment.Threshold;
import inra.ijpb.watershed.Watershed;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;


/**
 * Created by sc13967 on 03/05/2017.
 */
public class IdentifySecondaryObjects extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String MEDIAN_FILTER_RADIUS = "Median filter radius";
    public static final String THRESHOLD_METHOD = "Threshold method";

    private static final String[] thresholdMethods = new String[]{"Otsu","Huang","Li"};

    @Override
    public String getTitle() {
        return "Identify secondary objects";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void execute(HCWorkspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting relevant parameters
        double medFiltR = parameters.getValue(MEDIAN_FILTER_RADIUS);
        String thrMeth = parameters.getValue(THRESHOLD_METHOD);

        // Loading images and objects into workspace
        HCName inputImageName = parameters.getValue(INPUT_IMAGE);
        HCImage inputImage2 = workspace.getImages().get(inputImageName);
        ImagePlus image2 = inputImage2.getImagePlus();

        HCName inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        HCObjectSet objects1 = workspace.getObjects().get(inputObjectsName);

        // Initialising the output objects ArrayList
        HCName outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        // Getting nuclei objects as image
        if (verbose) System.out.println("["+moduleName+"] Converting objects to image");
        ImagePlus image1 = new ObjectImageConverter().convertObjectsToImage(objects1,new HCName("Temp image"),inputImage2,true).getImagePlus();

        // Segmenting cell image
        // Filtering cell image
        if (verbose) System.out.println("["+moduleName+"] Applying filter (radius = "+medFiltR+" px)");
        image2.setStack(Filters3D.filter(image2.getImageStack(), Filters3D.MEDIAN, (float) medFiltR, (float) medFiltR,1));

        // Thresholded cell image
        if (verbose) System.out.println("["+moduleName+"] Applying threshold (method = "+thrMeth+")");
        Auto_Threshold auto_threshold = new Auto_Threshold();
        Object[] results2 = auto_threshold.exec(image2,thrMeth,true,false,true,true,false,true);
        ImagePlus imMask = Threshold.threshold(image2,(Integer) results2[0],Integer.MAX_VALUE);

        // Gradient of cell image
        if (verbose) System.out.println("["+moduleName+"] Calculating gradient image");
        Strel3D strel3D = Strel3D.Shape.BALL.fromRadius(1);
        image2 = new MorphologicalFilter3DPlugin().process(image2, Morphology.Operation.INTERNAL_GRADIENT, strel3D);

        // Getting the labelled cells
        if (verbose) System.out.println("["+moduleName+"] Applying watershed segmentation");
        ImagePlus im2 = Watershed.computeWatershed(image2,image1,imMask,8,false,false);

        // Converting the labelled cell image to objects
        if (verbose) System.out.println("["+moduleName+"] Converting image to objects");
        HCImage tempImage = new HCImage(new HCName("Temp image"),im2);
        HCObjectSet objects2 = new ObjectImageConverter().convertImageToObjects(tempImage,outputObjectsName);

        // Watershed will give one cell per nucleus and these should already have the same labelling number.
        if (verbose) System.out.println("["+moduleName+"] Linking primary and secondary objects by ID number");
        RelateObjects.linkMatchingIDs(objects1,objects2);

        // Adding objects to workspace
        if (verbose) System.out.println("["+moduleName+"] Adding objects ("+outputObjectsName.getName()+") to workspace");
        workspace.addObjects(objects2);

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(INPUT_IMAGE, HCParameter.INPUT_IMAGE,null));
        parameters.addParameter(new HCParameter(INPUT_OBJECTS, HCParameter.INPUT_OBJECTS,null));
        parameters.addParameter(new HCParameter(OUTPUT_OBJECTS, HCParameter.OUTPUT_OBJECTS,null));
        parameters.addParameter(new HCParameter(MEDIAN_FILTER_RADIUS, HCParameter.DOUBLE,2.0));
        parameters.addParameter(new HCParameter(THRESHOLD_METHOD, HCParameter.CHOICE_ARRAY,thresholdMethods[0],thresholdMethods));

    }

    @Override
    public HCParameterCollection getActiveParameters() {
        return parameters;
    }

    @Override
    public void addMeasurements(HCMeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(HCRelationshipCollection relationships) {
        relationships.addRelationship(parameters.getValue(INPUT_OBJECTS),parameters.getValue(OUTPUT_OBJECTS));

    }
}
