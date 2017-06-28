package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import ij.ImagePlus;
import ij.measure.Calibration;
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
    public void execute(HCWorkspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        HCImage inputImage = workspace.getImages().get(inputImageName);
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
        HCImage tempImage = new HCImage(new String("Temp image"),inputImagePlus);
        HCObjectSet outputObjects = new ObjectImageConverter().convertImageToObjects(tempImage,outputObjectsName);

        // Adding distance calibration to each object
        Calibration calibration = inputImagePlus.getCalibration();
        for (HCObject object:outputObjects.values()) {
            object.addCalibration(HCObject.X,calibration.getX(1));
            object.addCalibration(HCObject.Y,calibration.getY(1));
            object.addCalibration(HCObject.Z,calibration.getZ(1));
            object.addCalibration(HCObject.C,1);
            object.addCalibration(HCObject.T,1);
            object.setCalibratedUnits(calibration.getUnits());

        }

        if (verbose) System.out.println("["+moduleName+"] "+outputObjects.size()+" objects detected");

        // Adding objects to workspace
        if (verbose) System.out.println("["+moduleName+"] Adding objects ("+outputObjectsName+") to workspace");
        workspace.addObjects(outputObjects);

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(INPUT_IMAGE,HCParameter.INPUT_IMAGE,null));
        parameters.addParameter(new HCParameter(OUTPUT_OBJECTS,HCParameter.OUTPUT_OBJECTS,null));
        parameters.addParameter(new HCParameter(WHITE_BACKGROUND,HCParameter.BOOLEAN,true));

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
