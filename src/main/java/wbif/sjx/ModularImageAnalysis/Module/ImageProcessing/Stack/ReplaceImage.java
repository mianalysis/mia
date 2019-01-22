package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import ij.ImagePlus;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.InputImageP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.ParameterCollection;

public class ReplaceImage extends Module {
    public static final String INPUT_IMAGE1 = "Input image 1 (to be replaced)";
    public static final String INPUT_IMAGE2 = "Input image 2";

    @Override
    public String getTitle() {
        return "Replace image";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_STACK;
    }

    @Override
    public String getHelp() {
        return "This module duplicates an image into another, existing, image.  " +
                "\nThis is useful when dealing with optional modules, where a specific input is required later on.";
    }

    @Override
    protected boolean run(Workspace workspace) {
        // Getting input images
        String inputImageName1 = parameters.getValue(INPUT_IMAGE1);
        Image inputImage1 = workspace.getImages().get(inputImageName1);

        String inputImageName2 = parameters.getValue(INPUT_IMAGE2);
        Image inputImage2 = workspace.getImages().get(inputImageName2);
        ImagePlus inputImagePlus2 = inputImage2.getImagePlus();

        inputImage1.setImagePlus(inputImagePlus2);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE1,this));
        parameters.add(new InputImageP(INPUT_IMAGE2,this));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
