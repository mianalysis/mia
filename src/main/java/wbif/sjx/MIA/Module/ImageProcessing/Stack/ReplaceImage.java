package wbif.sjx.MIA.Module.ImageProcessing.Stack;

import ij.ImagePlus;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;

public class ReplaceImage extends Module {
    public static final String INPUT_IMAGE1 = "Input image 1 (to be replaced)";
    public static final String INPUT_IMAGE2 = "Input image 2";

    public ReplaceImage(ModuleCollection modules) {
        super("Replace image",modules);
    }


    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_STACK;
    }

    @Override
    public String getDescription() {
        return "This module duplicates an image into another, existing, image.  " +
                "\nThis is useful when dealing with optional modules, where a specific input is required later on.";
    }

    @Override
    public boolean process(Workspace workspace) {
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
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
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

    @Override
    public boolean verify() {
        return true;
    }
}
