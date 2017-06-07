package wbif.sjx.ModularImageAnalysis.Module;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by sc13967 on 06/06/2017.
 */
public class BinaryOperations extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String OPERATION_MODE = "Filter mode";
    public static final String NUM_ITERATIONS = "Number of iterations";

    private static final String DILATE = "Dilate 2D";
    private static final String ERODE = "Erode 2D";
    private static final String FILL_HOLES_2D = "Fill holes 2D";
    private static final String[] OPERATION_MODES = new String[]{DILATE,ERODE,FILL_HOLES_2D};

    @Override
    public String getTitle() {
        return "Binary operations";
    }

    @Override
    public String getHelp() {
        return "Performs 2D fill holes, dilate and erode using ImageJ functions";

    }

    @Override
    public void execute(HCWorkspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting input image
        HCName inputImageName = parameters.getValue(INPUT_IMAGE);
        HCImage inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String operationMode = parameters.getValue(OPERATION_MODE);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) {inputImagePlus = new Duplicator().run(inputImagePlus);}

        // Applying process to stack
        if (operationMode.equals(DILATE)) {
            int numIterations = parameters.getValue(NUM_ITERATIONS);
            if (verbose) System.out.println("["+moduleName+"] Dilate ("+numIterations+"x)");
            for (int i=0;i<numIterations;i++) {
                IJ.run(inputImagePlus, "Dilate", "stack");
            }

        } else if (operationMode.equals(ERODE)) {
            int numIterations = parameters.getValue(NUM_ITERATIONS);
            if (verbose) System.out.println("["+moduleName+"] Erode ("+numIterations+"x)");
            for (int i=0;i<numIterations;i++) {
                IJ.run(inputImagePlus, "Erode", "stack");
            }

        } else if (operationMode.equals(FILL_HOLES_2D)) {
            if (verbose) System.out.println("["+moduleName+"] Filling binary holes");
            IJ.run(inputImagePlus,"Fill Holes", "stack");

        }

        if (!applyToInput) {
            HCName outputImageName = parameters.getValue(OUTPUT_IMAGE);
            if (verbose) System.out.println("["+moduleName+"] Adding image ("+outputImageName+") to workspace");
            HCImage outputImage = new HCImage(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);

        }
    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(INPUT_IMAGE,HCParameter.INPUT_IMAGE,null));
        parameters.addParameter(new HCParameter(APPLY_TO_INPUT,HCParameter.BOOLEAN,true));
        parameters.addParameter(new HCParameter(OUTPUT_IMAGE,HCParameter.OUTPUT_IMAGE,null));
        parameters.addParameter(new HCParameter(OPERATION_MODE,HCParameter.CHOICE_ARRAY,FILL_HOLES_2D,OPERATION_MODES));
        parameters.addParameter(new HCParameter(NUM_ITERATIONS,HCParameter.INTEGER,1));

    }

    @Override
    public HCParameterCollection getActiveParameters() {
        HCParameterCollection returnedParameters = new HCParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.addParameter(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.addParameter(parameters.getParameter(OUTPUT_IMAGE));

        }

        returnedParameters.addParameter(parameters.getParameter(OPERATION_MODE));

        if (parameters.getValue(OPERATION_MODE).equals(DILATE) | parameters.getValue(OPERATION_MODE).equals(ERODE)) {
            returnedParameters.addParameter(parameters.getParameter(NUM_ITERATIONS));

        }

        return returnedParameters;

    }

    @Override
    public void addMeasurements(HCMeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(HCRelationshipCollection relationships) {

    }
}
