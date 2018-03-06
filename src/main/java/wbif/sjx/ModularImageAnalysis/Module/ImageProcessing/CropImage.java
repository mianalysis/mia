package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.Duplicator;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Process.IntensityMinMax;

public class CropImage extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String LEFT = "Left coordinate";
    public static final String RIGHT = "Right coordinate";
    public static final String HEIGHT = "Top coordinate";
    public static final String BOTTOM = "Bottom coordinate";
    public static final String SHOW_IMAGE = "Show image";

    @Override
    public String getTitle() {
        return "Crop image";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected void run(Workspace workspace, boolean verbose) throws GenericMIAException {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        int left = parameters.getValue(LEFT);
        int right = parameters.getValue(RIGHT);
        int top = parameters.getValue(HEIGHT);
        int bottom = parameters.getValue(BOTTOM);

        // Applying the macro
        Roi roi = new Roi(left,top,right-left,bottom-top);
        inputImagePlus.setRoi(roi);
        ImagePlus outputImagePlus = inputImagePlus.crop();

        // If selected, displaying the image
        if (parameters.getValue(SHOW_IMAGE)) {
            ImagePlus dispIpl = new Duplicator().run(outputImagePlus);
            IntensityMinMax.run(dispIpl,true);
            dispIpl.show();
        }

        // If the image is being saved as a new image, adding it to the workspace
        if (verbose) System.out.println("["+moduleName+"] Adding image ("+outputImageName+") to workspace");
        Image outputImage = new Image(outputImageName,outputImagePlus);
        workspace.addImage(outputImage);

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.add(new Parameter(LEFT, Parameter.INTEGER,0));
        parameters.add(new Parameter(RIGHT, Parameter.INTEGER,512));
        parameters.add(new Parameter(HEIGHT, Parameter.INTEGER,0));
        parameters.add(new Parameter(BOTTOM, Parameter.INTEGER,512));
        parameters.add(new Parameter(SHOW_IMAGE, Parameter.BOOLEAN,false));
    }

    @Override
    protected void initialiseMeasurementReferences() {

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
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
    public void addRelationships(RelationshipCollection relationships) {

    }
}
