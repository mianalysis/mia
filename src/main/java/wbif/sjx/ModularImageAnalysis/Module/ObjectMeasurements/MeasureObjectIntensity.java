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

    private Reference inputObjects;
    private MeasurementReference mean;
    private MeasurementReference min;
    private MeasurementReference max;
    private MeasurementReference sum;
    private MeasurementReference stdev;
    private MeasurementReference xCentMean;
    private MeasurementReference xCentStdev;
    private MeasurementReference yCentMean;
    private MeasurementReference yCentStdev;
    private MeasurementReference zCentMean;
    private MeasurementReference zCentStdev;

    private interface Measurements {
        String MEAN = "MEAN";
        String MIN = "MIN";
        String MAX = "MAX";
        String SUM = "SUM";
        String STDEV = "STDEV";

        String X_CENT_MEAN = "X_CENTRE_MEAN (PX)";
        String X_CENT_STD = "X_CENTRE_STD (PX)";
        String Y_CENT_MEAN = "Y_CENTRE_MEAN (PX)";
        String Y_CENT_STD = "Y_CENTRE_STD (PX)";
        String Z_CENT_MEAN = "Z_CENTRE_MEAN (SLICE)";
        String Z_CENT_STD = "Z_CENTRE_STD (SLICE)";

    }


    private String getFullName(String imageName, String measurement) {
        return "INTENSITY//"+imageName+"_"+measurement;
    }

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
            object.addMeasurement(new Measurement(getFullName(imageName,Measurements.MEAN), cs.getMean()));
        if (parameters.getValue(MEASURE_MIN))
            object.addMeasurement(new Measurement(getFullName(imageName,Measurements.MIN), cs.getMin()));
        if (parameters.getValue(MEASURE_MAX))
            object.addMeasurement(new Measurement(getFullName(imageName,Measurements.MAX), cs.getMax()));
        if (parameters.getValue(MEASURE_STDEV))
            object.addMeasurement(new Measurement(getFullName(imageName,Measurements.STDEV), cs.getStd(CumStat.SAMPLE)));
        if (parameters.getValue(MEASURE_SUM))
            object.addMeasurement(new Measurement(getFullName(imageName,Measurements.SUM), cs.getSum()));

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

        object.addMeasurement(new Measurement(getFullName(imageName,Measurements.X_CENT_MEAN), csX.getMean()));
        object.addMeasurement(new Measurement(getFullName(imageName,Measurements.X_CENT_STD), csX.getStd()));
        object.addMeasurement(new Measurement(getFullName(imageName,Measurements.Y_CENT_MEAN), csY.getMean()));
        object.addMeasurement(new Measurement(getFullName(imageName,Measurements.Y_CENT_STD), csY.getStd()));
        object.addMeasurement(new Measurement(getFullName(imageName,Measurements.Z_CENT_MEAN), csZ.getMean()));
        object.addMeasurement(new Measurement(getFullName(imageName,Measurements.Z_CENT_STD), csZ.getStd()));

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
        ObjCollection objects = workspace.getObjects().get(objectName);

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
    public void initialiseReferences() {
        inputObjects = new Reference();
        objectReferences.add(inputObjects);

        mean = new MeasurementReference(Measurements.MEAN);
        min = new MeasurementReference(Measurements.MIN);
        max = new MeasurementReference(Measurements.MAX);
        stdev = new MeasurementReference(Measurements.STDEV);
        sum = new MeasurementReference(Measurements.SUM);
        xCentMean = new MeasurementReference(Measurements.X_CENT_MEAN);
        xCentStdev = new MeasurementReference(Measurements.X_CENT_STD);
        yCentMean = new MeasurementReference(Measurements.Y_CENT_MEAN);
        yCentStdev = new MeasurementReference(Measurements.Y_CENT_STD);
        zCentMean = new MeasurementReference(Measurements.Z_CENT_MEAN);
        zCentStdev = new MeasurementReference(Measurements.Z_CENT_STD);

        inputObjects.addMeasurementReference(mean);
        inputObjects.addMeasurementReference(min);
        inputObjects.addMeasurementReference(max);
        inputObjects.addMeasurementReference(stdev);
        inputObjects.addMeasurementReference(sum);
        inputObjects.addMeasurementReference(xCentMean);
        inputObjects.addMeasurementReference(xCentStdev);
        inputObjects.addMeasurementReference(yCentMean);
        inputObjects.addMeasurementReference(yCentStdev);
        inputObjects.addMeasurementReference(zCentMean);
        inputObjects.addMeasurementReference(zCentStdev);

    }

    @Override
    public ReferenceCollection updateAndGetImageReferences() {
        return null;
    }

    @Override
    public ReferenceCollection updateAndGetObjectReferences() {
        inputObjects.setName(parameters.getValue(INPUT_OBJECTS));

        String inputImageName = parameters.getValue(INPUT_IMAGE);

        mean.setCalculated(false);
        min.setCalculated(false);
        max.setCalculated(false);
        stdev.setCalculated(false);
        sum.setCalculated(false);
        xCentMean.setCalculated(false);
        xCentStdev.setCalculated(false);
        yCentMean.setCalculated(false);
        yCentStdev.setCalculated(false);
        zCentMean.setCalculated(false);
        zCentStdev.setCalculated(false);

        if (parameters.getValue(MEASURE_MEAN)) {
            mean.setCalculated(true);
            mean.setMeasurementName(getFullName(inputImageName, Measurements.MEAN));
        }

        if (parameters.getValue(MEASURE_MIN)) {
            min.setCalculated(true);
            min.setMeasurementName(getFullName(inputImageName, Measurements.MIN));
        }

        if (parameters.getValue(MEASURE_MAX)) {
            max.setCalculated(true);
            max.setMeasurementName(getFullName(inputImageName, Measurements.MAX));
        }

        if (parameters.getValue(MEASURE_STDEV)) {
            stdev.setCalculated(true);
            stdev.setMeasurementName(getFullName(inputImageName, Measurements.STDEV));
        }

        if (parameters.getValue(MEASURE_SUM)) {
            sum.setCalculated(true);
            sum.setMeasurementName(getFullName(inputImageName, Measurements.SUM));
        }

        if (parameters.getValue(MEASURE_WEIGHTED_CENTRE)) {
            xCentMean.setCalculated(true);
            xCentStdev.setCalculated(true);
            yCentMean.setCalculated(true);
            yCentStdev.setCalculated(true);
            zCentMean.setCalculated(true);
            zCentStdev.setCalculated(true);
            xCentMean.setMeasurementName(getFullName(inputImageName,Measurements.X_CENT_MEAN));
            xCentStdev.setMeasurementName(getFullName(inputImageName,Measurements.X_CENT_STD));
            yCentMean.setMeasurementName(getFullName(inputImageName,Measurements.Y_CENT_MEAN));
            yCentStdev.setMeasurementName(getFullName(inputImageName,Measurements.Y_CENT_STD));
            zCentMean.setMeasurementName(getFullName(inputImageName,Measurements.Z_CENT_MEAN));
            zCentStdev.setMeasurementName(getFullName(inputImageName,Measurements.Z_CENT_STD));

        }

        return objectReferences;

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
