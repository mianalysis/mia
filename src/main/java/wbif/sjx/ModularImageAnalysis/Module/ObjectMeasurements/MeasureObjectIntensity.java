// TODO: Could add an optional parameter to select the channel of the input image to use for measurement

package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements;

import ij.ImagePlus;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MathFunc.CumStat;

import java.util.ArrayList;

/**
 * Created by sc13967 on 05/05/2017.
 */
public class MeasureObjectIntensity extends HCModule {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String INPUT_IMAGE = "Input image";
    public static final String MEASURE_MEAN = "Measure mean";
    public static final String MEASURE_STDEV = "Measure standard deviation";
    public static final String MEASURE_MIN = "Measure minimum";
    public static final String MEASURE_MAX = "Measure maximum";
    public static final String MEASURE_SUM = "Measure sum";
    public static final String MEASURE_WEIGHTED_CENTRE = "Measure weighted centre";


    private void measureIntensity(Obj object, ImagePlus ipl) {
        // Getting parameters
        String imageName = parameters.getValue(INPUT_IMAGE);

        // Initialising the cumulative statistics object to store pixel intensities
        CumStat cs = new CumStat();

        // Getting pixel coordinates
        ArrayList<Integer> x = object.getXCoords();
        ArrayList<Integer> y = object.getYCoords();
        ArrayList<Integer> z = object.getZCoords();
        int tPos = object.getT();

        // Running through all pixels in this object and adding the intensity to the MultiCumStat object
        for (int i=0;i<x.size();i++) {
            ipl.setPosition(1,z.get(i)+1,tPos+1);
            cs.addMeasure(ipl.getProcessor().getPixelValue(x.get(i),y.get(i)));

        }

        // Calculating mean, std, min and max intensity
        if (parameters.getValue(MEASURE_MEAN))
            object.addMeasurement(new MIAMeasurement(imageName+"_MEAN", cs.getMean()));
        if (parameters.getValue(MEASURE_MIN))
            object.addMeasurement(new MIAMeasurement(imageName+"_MIN", cs.getMin()));
        if (parameters.getValue(MEASURE_MAX))
            object.addMeasurement(new MIAMeasurement(imageName+"_MAX", cs.getMax()));
        if (parameters.getValue(MEASURE_STDEV))
            object.addMeasurement(new MIAMeasurement(imageName+"_STD", cs.getStd(CumStat.SAMPLE)));
        if (parameters.getValue(MEASURE_SUM))
            object.addMeasurement(new MIAMeasurement(imageName+"_SUM", cs.getSum()));

    }

    private void measureWeightedCentre(Obj object, ImagePlus ipl) {
        // Getting parameters
        String imageName = parameters.getValue(INPUT_IMAGE);

        // Initialising the cumulative statistics objects to store pixel intensities in each direction.
        CumStat csX = new CumStat();
        CumStat csY = new CumStat();
        CumStat csZ = new CumStat();

        // Getting pixel coordinates
        ArrayList<Integer> x = object.getXCoords();
        ArrayList<Integer> y = object.getYCoords();
        ArrayList<Integer> z = object.getZCoords();
        int tPos = object.getT();

        // Running through all pixels in this object and adding the intensity to the MultiCumStat object
        for (int i=0;i<x.size();i++) {
            ipl.setPosition(1,z.get(i)+1,tPos+1);
            csX.addMeasure(x.get(i),ipl.getProcessor().getPixelValue(x.get(i),y.get(i)));
            csY.addMeasure(y.get(i),ipl.getProcessor().getPixelValue(x.get(i),y.get(i)));
            csZ.addMeasure(z.get(i),ipl.getProcessor().getPixelValue(x.get(i),y.get(i)));

        }

        object.addMeasurement(new MIAMeasurement(imageName+"_X_CENTRE_MEAN (PX)", csX.getMean()));
        object.addMeasurement(new MIAMeasurement(imageName+"_X_CENTRE_STD (PX)", csX.getStd()));
        object.addMeasurement(new MIAMeasurement(imageName+"_Y_CENTRE_MEAN (PX)", csY.getMean()));
        object.addMeasurement(new MIAMeasurement(imageName+"_Y_CENTRE_STD (PX)", csY.getStd()));
        object.addMeasurement(new MIAMeasurement(imageName+"_Z_CENTRE_MEAN (SLICE)", csZ.getMean()));
        object.addMeasurement(new MIAMeasurement(imageName+"_Z_CENTRE_STD (SLICE)", csZ.getStd()));

    }

    @Override
    public String getTitle() {
        return "Measure object intensity";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void run(Workspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting input objects
        String objectName = parameters.getValue(INPUT_OBJECTS);
        ObjSet objects = workspace.getObjects().get(objectName);

        // Getting input image
        String imageName = parameters.getValue(INPUT_IMAGE);
        Image image = workspace.getImages().get(imageName);
        ImagePlus ipl = image.getImagePlus();

        // Measuring intensity for each object and adding the measurement to that object
        for (Obj object:objects.values()) measureIntensity(object,ipl);

        // If specified, measuring weighted centre for intensity
        if (parameters.getValue(MEASURE_WEIGHTED_CENTRE)) {
            for (Obj object:objects.values()) measureWeightedCentre(object,ipl);
        }

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.addParameter(new Parameter(MEASURE_MEAN, Parameter.BOOLEAN, true));
        parameters.addParameter(new Parameter(MEASURE_MIN, Parameter.BOOLEAN, true));
        parameters.addParameter(new Parameter(MEASURE_MAX, Parameter.BOOLEAN, true));
        parameters.addParameter(new Parameter(MEASURE_STDEV, Parameter.BOOLEAN, true));
        parameters.addParameter(new Parameter(MEASURE_SUM, Parameter.BOOLEAN, true));
        parameters.addParameter(new Parameter(MEASURE_WEIGHTED_CENTRE, Parameter.BOOLEAN, true));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        return parameters;

    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        if (parameters.getValue(MEASURE_MEAN))
            measurements.addMeasurement(inputObjectsName,inputImageName+"_MEAN_I");

        if (parameters.getValue(MEASURE_MIN))
            measurements.addMeasurement(inputObjectsName,inputImageName+"_MIN_I");

        if (parameters.getValue(MEASURE_MAX))
            measurements.addMeasurement(inputObjectsName,inputImageName+"_MAX_I");

        if (parameters.getValue(MEASURE_STDEV))
            measurements.addMeasurement(inputObjectsName,inputImageName+"_STD_I");

        if (parameters.getValue(MEASURE_SUM))
            measurements.addMeasurement(inputObjectsName,inputImageName+"_SUM_I");

        if (parameters.getValue(MEASURE_WEIGHTED_CENTRE)) {
            measurements.addMeasurement(inputObjectsName, inputImageName + "_X_CENTRE_MEAN (PX)");
            measurements.addMeasurement(inputObjectsName, inputImageName + "_X_CENTRE_STD (PX)");
            measurements.addMeasurement(inputObjectsName, inputImageName + "_Y_CENTRE_MEAN (PX)");
            measurements.addMeasurement(inputObjectsName, inputImageName + "_Y_CENTRE_STD (PX)");
            measurements.addMeasurement(inputObjectsName, inputImageName + "_Z_CENTRE_MEAN (SLICE)");
            measurements.addMeasurement(inputObjectsName, inputImageName + "_Z_CENTRE_STD (SLICE)");

        }
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
