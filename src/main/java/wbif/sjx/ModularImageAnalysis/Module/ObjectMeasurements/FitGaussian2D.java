//TODO: Add measurement outputs

package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.MeasureObjectCentroid;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.util.ArrayList;

/**
 * Created by sc13967 on 05/06/2017.
 */
public class FitGaussian2D extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String RADIUS_MODE = "Method to estimate spot radius";
    public static final String RADIUS = "Radius";
    public static final String RADIUS_MEASUREMENT = "Radius measurement";

    private static final String FIXED_VALUE = "Fixed value";
    private static final String MEASUREMENT = "Measurement";
    private static final String[] RADIUS_MODES = new String[]{FIXED_VALUE,MEASUREMENT};

    /**
     *
     * @param inputImage
     * @param pIn double[] containing all estimated parameters
     * @return double[] containing all fit parameters (x,y,sigma,A_0,A_BG)
     */
    public static double[] fitGaussian(HCImage inputImage, double[] pIn) {
        LevenbergMarquardtOptimizer lmo = new LevenbergMarquardtOptimizer();

        return null;

    }

    @Override
    public String getTitle() {
        return "Fit Gaussian 2D";
    }

    @Override
    public String getHelp() {
        return "INCOMPLETE" +
                "\n\nGaussian spot fitting.  Can take objects as estimated locations." +
                "\n***Only works in 2D***" +
                "\n***Only works for refinement of existing spots***";
    }

    @Override
    public void execute(HCWorkspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting input image
        HCName inputImageName = parameters.getValue(INPUT_IMAGE);
        HCImage inputImage = workspace.getImage(inputImageName);

        // Getting input objects to refine (if selected by used)
        HCName inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        HCObjectSet inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting output objects
        HCName outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        // Getting parameters
        String radiusMode = parameters.getValue(RADIUS_MODE);

        // Running through each object, doing the fitting
        for (HCObject inputObject:inputObjects.values()) {
            // Getting the centroid of the current object.  These should be single point objects, but this is a
            // precautionary measure
            ArrayList<Integer> xArray = inputObject.getCoordinates(HCObject.X);
            ArrayList<Integer> yArray = inputObject.getCoordinates(HCObject.Y);
            double x = MeasureObjectCentroid.calculateCentroid(xArray,MeasureObjectCentroid.MEAN);
            double y = MeasureObjectCentroid.calculateCentroid(yArray,MeasureObjectCentroid.MEAN);

            // Estimating parameters
            if (radiusMode.equals(FIXED_VALUE)) {
                double r = parameters.getValue(RADIUS);
            } else {
                double r = inputObject.getMeasurement(parameters.getValue(RADIUS_MEASUREMENT)).getValue();
            }



        }
    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(INPUT_IMAGE,HCParameter.INPUT_IMAGE,null));
        parameters.addParameter(new HCParameter(INPUT_OBJECTS,HCParameter.INPUT_OBJECTS,null));
        parameters.addParameter(new HCParameter(OUTPUT_OBJECTS,HCParameter.OUTPUT_OBJECTS,null));
        parameters.addParameter(new HCParameter(RADIUS_MODE,HCParameter.CHOICE_ARRAY,FIXED_VALUE,RADIUS_MODES));
        parameters.addParameter(new HCParameter(RADIUS,HCParameter.DOUBLE,null));
        parameters.addParameter(new HCParameter(RADIUS_MEASUREMENT,HCParameter.MEASUREMENT,null));

    }

    @Override
    public HCParameterCollection getActiveParameters() {
        HCParameterCollection returnedParameters = new HCParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.addParameter(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.addParameter(parameters.getParameter(OUTPUT_OBJECTS));
        returnedParameters.addParameter(parameters.getParameter(RADIUS_MODE));

        if (parameters.getValue(RADIUS_MODE) == FIXED_VALUE) {
            returnedParameters.addParameter(parameters.getParameter(RADIUS));

        } else if (parameters.getValue(RADIUS_MODE) == MEASUREMENT) {
            returnedParameters.addParameter(parameters.getParameter(RADIUS_MEASUREMENT));

        }


        return returnedParameters;

    }

    @Override
    public void addMeasurements(HCMeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(HCRelationshipCollection relationships) {
        HCName inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        HCName outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        relationships.addRelationship(inputObjectsName,outputObjectsName);

    }
}
