package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing;

import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.plugin.Filters3D;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Filters.DoG;

/**
 * Created by sc13967 on 30/05/2017.
 */
public class FilterImage extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String FILTER_MODE = "Filter mode";
    public static final String FILTER_RADIUS = "Filter radius (px)";
    public static final String CALIBRATED_UNITS = "Calibrated units";
    public static final String SHOW_IMAGE = "Show image";

    private static final String MEDIAN3D = "Median 3D";
    private static final String DOG2D = "Difference of Gaussian 2D";
    private static final String[] FILTER_MODES = new String[]{DOG2D,MEDIAN3D};

    @Override
    public String getTitle() {
        return "Filter image";
    }

    @Override
    public String getHelp() {
        return "+++INCOMPLETE+++";
    }

    @Override
    public void run(Workspace workspace, boolean verbose) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String filterMode = parameters.getValue(FILTER_MODE);
        double filterRadius = parameters.getValue(FILTER_RADIUS);
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS);

        if (calibratedUnits) {
            filterRadius = inputImagePlus.getCalibration().getRawX(filterRadius);
        }

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) {inputImagePlus = new Duplicator().run(inputImagePlus);}

        // Applying smoothing filter
        if (filterMode.equals(MEDIAN3D)) {
            if (verbose) System.out.println("[" + moduleName + "] Applying 3D median filter (radius = " + filterRadius + " px)");
            inputImagePlus.setStack(Filters3D.filter(inputImagePlus.getImageStack(), Filters3D.MEDIAN, (float) filterRadius, (float) filterRadius, (float) filterRadius));

        } else if (filterMode.equals(DOG2D)) {
            if (verbose) System.out.println("[" + moduleName + "] Applying 2D difference of Gaussian filter (radius = " + filterRadius + " px)");
            DoG.run(inputImagePlus,filterRadius,true);

        }

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            String outputImageName = parameters.getValue(OUTPUT_IMAGE);
            Image outputImage = new Image(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);

            // If selected, displaying the image
            if (parameters.getValue(SHOW_IMAGE)) {
                new Duplicator().run(outputImage.getImagePlus()).show();
            }

        } else {
            // If selected, displaying the image
            if (parameters.getValue(SHOW_IMAGE)) {
                new Duplicator().run(inputImagePlus).show();
            }
        }
    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.addParameter(new Parameter(APPLY_TO_INPUT, Parameter.BOOLEAN,true));
        parameters.addParameter(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.addParameter(new Parameter(FILTER_MODE, Parameter.CHOICE_ARRAY,FILTER_MODES[0],FILTER_MODES));
        parameters.addParameter(new Parameter(FILTER_RADIUS, Parameter.DOUBLE,2d));
        parameters.addParameter(new Parameter(CALIBRATED_UNITS, Parameter.BOOLEAN,false));
        parameters.addParameter(new Parameter(SHOW_IMAGE, Parameter.BOOLEAN,false));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.addParameter(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.addParameter(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.addParameter(parameters.getParameter(FILTER_MODE));
        returnedParameters.addParameter(parameters.getParameter(FILTER_RADIUS));
        returnedParameters.addParameter(parameters.getParameter(CALIBRATED_UNITS));
        returnedParameters.addParameter(parameters.getParameter(SHOW_IMAGE));

        return returnedParameters;

    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
