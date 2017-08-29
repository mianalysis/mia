package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import ij.ImagePlus;
import inra.ijpb.binary.conncomp.FloodFillComponentsLabeling3D;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by sc13967 on 06/06/2017.
 */
public class IdentifyObjects extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String WHITE_BACKGROUND = "Black objects/white background";

    @Override
    public String getTitle() {
        return "Identify objects";
    }

    @Override
    public String getHelp() {
        return "INCOMPLETE" +
                "\nTakes a binary image and uses connected components labelling to create objects" +
                "\nUses MorphoLibJ to perform connected components labelling in 3D";
    }

    @Override
    public void run(Workspace workspace, boolean verbose) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting output objects name
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        // Getting parameters
        boolean whiteBackground = parameters.getValue(WHITE_BACKGROUND);
        if (whiteBackground) {
            for (int z = 1; z <= inputImagePlus.getNSlices(); z++) {
                for (int c = 1; c <= inputImagePlus.getNChannels(); c++) {
                    for (int t = 1; t <= inputImagePlus.getNFrames(); t++) {
                        inputImagePlus.setPosition(c, z, t);
                        inputImagePlus.getProcessor().invert();
                    }
                }
            }

            inputImagePlus.setPosition(1,1,1);

        }

        // Applying connected components labelling
        if (verbose) System.out.println("["+moduleName+"] Applying connected components labelling");
        FloodFillComponentsLabeling3D ffcl3D = new FloodFillComponentsLabeling3D();
        inputImagePlus.setStack(ffcl3D.computeLabels(inputImagePlus.getImageStack()));

        // Converting image to objects
        if (verbose) System.out.println("["+moduleName+"] Converting image to objects");
        Image tempImage = new Image("Temp image",inputImagePlus);
        ObjSet outputObjects = new ObjectImageConverter().convertImageToObjects(tempImage,outputObjectsName);

        if (verbose) System.out.println("["+moduleName+"] "+outputObjects.size()+" objects detected");

        // Adding objects to workspace
        if (verbose) System.out.println("["+moduleName+"] Adding objects ("+outputObjectsName+") to workspace");
        workspace.addObjects(outputObjects);

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.addParameter(new Parameter(OUTPUT_OBJECTS, Parameter.OUTPUT_OBJECTS,null));
        parameters.addParameter(new Parameter(WHITE_BACKGROUND, Parameter.BOOLEAN,true));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        return parameters;
    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
