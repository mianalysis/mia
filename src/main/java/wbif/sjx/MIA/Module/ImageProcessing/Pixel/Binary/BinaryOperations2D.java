// TODO: What happens when 3D distance map is generateModuleList on 4D or 5D image hyperstack?

package wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;

/**
 * Created by sc13967 on 06/06/2017.
 */
public class BinaryOperations2D extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String OPERATION_MODE = "Filter mode";
    public static final String NUM_ITERATIONS = "Number of iterations";

    public BinaryOperations2D(ModuleCollection modules) {
        super(modules);
    }


    public interface OperationModes {
        String DILATE = "Dilate";
        String DISTANCE_MAP = "Distance map";
        String ERODE = "Erode";
        String FILL_HOLES = "Fill holes";
        String OUTLINE = "Outline";
        String SKELETONISE = "Skeletonise";
        String WATERSHED = "Watershed";

        String[] ALL = new String[]{DILATE, DISTANCE_MAP, ERODE, FILL_HOLES, OUTLINE, SKELETONISE, WATERSHED};

    }

    public static void process(ImagePlus ipl, String operationMode, int numIterations) {
        process(new Image("Image",ipl),operationMode,numIterations);
    }

    public static void process(Image image, String operationMode, int numIterations) {
        ImagePlus ipl = image.getImagePlus();

        // Applying processAutomatic to stack
        switch (operationMode) {
            case OperationModes.DILATE:
                IJ.run(ipl,"Options...", "iterations="+numIterations+" count=1 do=Dilate stack");
                break;

            case OperationModes.ERODE:
                IJ.run(ipl,"Options...", "iterations="+numIterations+" count=1 do=Erode stack");
                break;

            case OperationModes.FILL_HOLES:
                IJ.run(ipl,"Options...", "iterations="+numIterations+" count=1 do=[Fill Holes] stack");
                break;

            case OperationModes.OUTLINE:
                IJ.run(ipl,"Outline", "stack");
                break;

            case OperationModes.SKELETONISE:
                IJ.run(ipl,"Options...", "iterations="+numIterations+" count=1 do=Skeletonize stack");
                break;

            case OperationModes.WATERSHED:
                IJ.run(ipl,"Watershed", "stack");
                break;

        }
    }


    @Override
    public String getTitle() {
        return "Binary operations 2D";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_PIXEL_BINARY;
    }

    @Override
    public String getHelp() {
        return "Expects black objects on a white background." +
                "\nPerforms 2D fill holes, dilate and erode using ImageJ functions.";

    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String operationMode = parameters.getValue(OPERATION_MODE);
        int numIterations = parameters.getValue(NUM_ITERATIONS);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) inputImagePlus = new Duplicator().run(inputImagePlus);

        if (operationMode.equals(OperationModes.DISTANCE_MAP)) {
            IJ.run(inputImagePlus,"Distance Map", "stack");
        } else {
            process(inputImagePlus, operationMode, numIterations);
        }

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            writeMessage("Adding image ("+outputImageName+") to workspace");
            Image outputImage = new Image(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);
            if (showOutput) outputImage.showImage();

        } else {
            if (showOutput) inputImage.showImage();

        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE,this));
        parameters.add(new BooleanP(APPLY_TO_INPUT,this,true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE,this));
        parameters.add(new ChoiceP(OPERATION_MODE,this,OperationModes.DILATE,OperationModes.ALL));
        parameters.add(new IntegerP(NUM_ITERATIONS,this,1));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(OPERATION_MODE));
        switch ((String) parameters.getValue(OPERATION_MODE)) {
            case OperationModes.DILATE:
            case OperationModes.ERODE:
            case OperationModes.FILL_HOLES:
            case OperationModes.OUTLINE:
            case OperationModes.SKELETONISE:
            case OperationModes.WATERSHED:
                returnedParameters.add(parameters.getParameter(NUM_ITERATIONS));
                break;
        }

        return returnedParameters;

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

}
