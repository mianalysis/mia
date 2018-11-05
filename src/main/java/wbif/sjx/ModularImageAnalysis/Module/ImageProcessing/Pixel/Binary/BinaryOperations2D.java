// TODO: What happens when 3D distance map is run on 4D or 5D image hyperstack?

package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.Binary;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.*;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by sc13967 on 06/06/2017.
 */
public class BinaryOperations2D extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String OPERATION_MODE = "Filter mode";
    public static final String NUM_ITERATIONS = "Number of iterations";


    public interface OperationModes {
        String DILATE = "Dilate";
        String ERODE = "Erode";
        String FILL_HOLES = "Fill holes";
        String OUTLINE = "Outline";
        String SKELETONISE = "Skeletonise";
        String WATERSHED = "Watershed";

        String[] ALL = new String[]{DILATE, ERODE, FILL_HOLES, OUTLINE, SKELETONISE, WATERSHED};

    }

    public interface IntensityModes {
        String DISTANCE = "Distance";
        String INPUT_IMAGE = "Input image intensity";

        String[] ALL = new String[]{DISTANCE,INPUT_IMAGE};

    }

    public interface Connectivity3D {
        String SIX = "6";
        String TWENTYSIX = "26";

        String[] ALL = new String[]{SIX,TWENTYSIX};

    }


    public static void process(ImagePlus ipl, String operationMode, int numIterations) {
        // Applying process to stack
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
                "\nPerforms 2D fill holes, dilate and erode using ImageJ functions." +
                "\nUses MorphoLibJ to do 3D operations.";

    }

    @Override
    public void run(Workspace workspace) {
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

        process(inputImagePlus,operationMode,numIterations);

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            writeMessage("Adding image ("+outputImageName+") to workspace");
            Image outputImage = new Image(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);
            if (showOutput) showImage(outputImage);

        } else {
            if (showOutput) showImage(inputImage);

        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(APPLY_TO_INPUT, Parameter.BOOLEAN,true));
        parameters.add(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.add(
                new Parameter(OPERATION_MODE, Parameter.CHOICE_ARRAY,OperationModes.DILATE,OperationModes.ALL));
        parameters.add(new Parameter(NUM_ITERATIONS, Parameter.INTEGER,1));

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
        returnedParameters.add(parameters.getParameter(NUM_ITERATIONS));

        return returnedParameters;

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
    public MetadataReferenceCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
