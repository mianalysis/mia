// TODO: Add object linking for 4D - linking should be done on spatial overlap (similar to how its done in 3D)

package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import ij.ImagePlus;
import ij.plugin.SubHyperstackMaker;
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
                "\nUses MorphoLibJ to perform connected components labelling in 3D" +
                "\nDoesn't currently link objects in time.";
    }

    @Override
    public void run(Workspace workspace, boolean verbose) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting output objects name
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        ObjSet outputObjects = new ObjSet(outputObjectsName);

        // Getting parameters
        boolean whiteBackground = parameters.getValue(WHITE_BACKGROUND);

            for (int t = 1; t <= inputImagePlus.getNFrames(); t++) {
                if (verbose) System.out.println("[" + moduleName + "] Processing frame "+t+" of "+inputImagePlus.getNFrames());
                // Creating a copy of the input image
                ImagePlus currStack = SubHyperstackMaker.makeSubhyperstack(inputImagePlus,1+"-"+inputImagePlus.getNChannels(),1+"-"+inputImagePlus.getNSlices(),t+"-"+t);

                if (whiteBackground) {
                    for (int c = 1; c <= inputImagePlus.getNChannels(); c++) {
                        for (int z = 1; z <= currStack.getNSlices(); z++) {
                            currStack.setPosition(c, z, 1);
                            currStack.updateChannelAndDraw();
                            currStack.getProcessor().invert();
                        }
                    }
                }

                // Applying connected components labelling
                FloodFillComponentsLabeling3D ffcl3D = new FloodFillComponentsLabeling3D(26);
                currStack.setStack(ffcl3D.computeLabels(currStack.getImageStack()));

                // Converting image to objects
                Image tempImage = new Image("Temp image", currStack);
                ObjSet currOutputObjects = ObjectImageConverter.convertImageToObjects(tempImage, outputObjectsName);

                // Updating the current objects (setting the real frame number and offsetting the ID)
                int maxID = 0;
                for (Obj object:outputObjects.values()) {
                    maxID = Math.max(object.getID(),maxID);
                }

                for (Obj object:currOutputObjects.values()) {
                    object.setID(object.getID() + maxID + 1);
                    object.setT(t-1);
                    outputObjects.put(object.getID(),object);
                }
            }

//        // Creating a copy of the input image
//        inputImagePlus = new Duplicator().run(inputImagePlus);
//
//        // Getting output objects name
//        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
//
//        // Getting parameters
//        boolean whiteBackground = parameters.getValue(WHITE_BACKGROUND);
//        if (whiteBackground) {
//            for (int z = 1; z <= inputImagePlus.getNSlices(); z++) {
//                for (int c = 1; c <= inputImagePlus.getNChannels(); c++) {
//                    for (int t = 1; t <= inputImagePlus.getNFrames(); t++) {
//                        inputImagePlus.setPosition(c, z, t);
//                        inputImagePlus.getProcessor().invert();
//                    }
//                }
//            }
//
//            inputImagePlus.setPosition(1,1,1);
//
//        }
//
//        // Applying connected components labelling
//        if (verbose) System.out.println("["+moduleName+"] Applying connected components labelling");
//        FloodFillComponentsLabeling3D ffcl3D = new FloodFillComponentsLabeling3D(26);
//        inputImagePlus.setStack(ffcl3D.computeLabels(inputImagePlus.getImageStack()));
//
//        // Converting image to objects
//        if (verbose) System.out.println("["+moduleName+"] Converting image to objects");
//        Image tempImage = new Image("Temp image",inputImagePlus);
//        ObjSet outputObjects = ObjectImageConverter.convertImageToObjects(tempImage,outputObjectsName,0);

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
