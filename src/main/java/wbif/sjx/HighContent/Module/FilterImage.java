package wbif.sjx.HighContent.Module;

import ij.ImagePlus;
import wbif.sjx.HighContent.Object.*;

/**
 * Created by sc13967 on 30/05/2017.
 */
public class FilterImage extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String FILTER_MODE = "Filter mode";
    public static final String FILTER_RADIUS = "Filter radius (px)";
    public static final String CALIBRATED_UNITS = "Calibrated units";

    private static final String[] FILTER_MODES = new String[]{};

    @Override
    public String getTitle() {
        return "Filter image";
    }

    @Override
    public void execute(HCWorkspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting input image
        HCName inputImageName = parameters.getValue(INPUT_IMAGE);
        HCImage inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting output image name
        HCName outputImageName = parameters.getValue(OUTPUT_IMAGE);

        // Getting parameters
        String filterMode = parameters.getValue(FILTER_MODE);
        double filterRadius = parameters.getValue(FILTER_RADIUS);
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS);

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(INPUT_IMAGE,HCParameter.INPUT_IMAGE,null));
        parameters.addParameter(new HCParameter(OUTPUT_IMAGE,HCParameter.OUTPUT_IMAGE,null));
        parameters.addParameter(new HCParameter(FILTER_MODE,HCParameter.CHOICE_ARRAY,FILTER_MODES[0],FILTER_MODES));
        parameters.addParameter(new HCParameter(FILTER_RADIUS,HCParameter.DOUBLE,2d));
        parameters.addParameter(new HCParameter(CALIBRATED_UNITS,HCParameter.BOOLEAN,false));

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
