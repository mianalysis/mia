package wbif.sjx.ModularImageAnalysis.Module;

import ij.ImagePlus;
import inra.ijpb.binary.conncomp.FloodFillComponentsLabeling3D;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by sc13967 on 06/06/2017.
 */
public class IdentifyObjects extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_OBJECTS = "Output objects";

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
    public void execute(HCWorkspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting input image
        HCName inputImageName = parameters.getValue(INPUT_IMAGE);
        HCImage inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting output objects name
        HCName outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        // Applying connected components labelling
        if (verbose) System.out.println("["+moduleName+"] Applying connected components labelling");
        FloodFillComponentsLabeling3D ffcl3D = new FloodFillComponentsLabeling3D();
        inputImagePlus.setStack(ffcl3D.computeLabels(inputImagePlus.getImageStack()));

        // Converting image to objects
        if (verbose) System.out.println("["+moduleName+"] Converting image to objects");
        HCImage tempImage = new HCImage(new HCName("Temp image"),inputImagePlus);
        HCObjectSet outputObjects = new ObjectImageConverter().convertImageToObjects(tempImage,outputObjectsName);

        // Adding objects to workspace
        if (verbose) System.out.println("["+moduleName+"] Adding objects ("+outputObjectsName.getName()+") to workspace");
        workspace.addObjects(outputObjects);

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(INPUT_IMAGE,HCParameter.INPUT_IMAGE,null));
        parameters.addParameter(new HCParameter(OUTPUT_OBJECTS,HCParameter.OUTPUT_OBJECTS,null));

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

    }
}
