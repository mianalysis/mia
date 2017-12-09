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

    public interface Measurements {
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
        parameters.add(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(MEASURE_MEAN, Parameter.BOOLEAN, true));
        parameters.add(new Parameter(MEASURE_MIN, Parameter.BOOLEAN, true));
        parameters.add(new Parameter(MEASURE_MAX, Parameter.BOOLEAN, true));
        parameters.add(new Parameter(MEASURE_STDEV, Parameter.BOOLEAN, true));
        parameters.add(new Parameter(MEASURE_SUM, Parameter.BOOLEAN, true));
        parameters.add(new Parameter(MEASURE_WEIGHTED_CENTRE, Parameter.BOOLEAN, true));

    }

    @Override
    protected void initialiseMeasurementReferences() {
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MEAN));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MIN));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MAX));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.STDEV));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.SUM));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.X_CENT_MEAN));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.X_CENT_STD));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.Y_CENT_MEAN));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.Y_CENT_STD));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.Z_CENT_MEAN));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.Z_CENT_STD));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        String inputImageName = parameters.getValue(INPUT_IMAGE);

        MeasurementReference mean = objectMeasurementReferences.get(Measurements.MEAN);
        mean.setCalculated(false);
        if (parameters.getValue(MEASURE_MEAN)) {
            mean.setCalculated(true);
            mean.setNickName(getFullName(inputImageName, Measurements.MEAN));
        }

        MeasurementReference min = objectMeasurementReferences.get(Measurements.MIN);
        min.setCalculated(false);
        if (parameters.getValue(MEASURE_MIN)) {
            min.setCalculated(true);
            min.setNickName(getFullName(inputImageName, Measurements.MIN));
        }

        MeasurementReference max = objectMeasurementReferences.get(Measurements.MAX);
        max.setCalculated(false);
        if (parameters.getValue(MEASURE_MAX)) {
            max.setCalculated(true);
            max.setNickName(getFullName(inputImageName, Measurements.MAX));
        }


        MeasurementReference stdev = objectMeasurementReferences.get(Measurements.STDEV);
        stdev.setCalculated(false);
        if (parameters.getValue(MEASURE_STDEV)) {
            stdev.setCalculated(true);
            stdev.setNickName(getFullName(inputImageName, Measurements.STDEV));
        }

        MeasurementReference sum = objectMeasurementReferences.get(Measurements.SUM);
        sum.setCalculated(false);
        if (parameters.getValue(MEASURE_SUM)) {
            sum.setCalculated(true);
            sum.setNickName(getFullName(inputImageName, Measurements.SUM));
        }

        MeasurementReference xCentMean = objectMeasurementReferences.get(Measurements.X_CENT_MEAN);
        xCentMean.setCalculated(false);
        if (parameters.getValue(MEASURE_WEIGHTED_CENTRE)) {
            xCentMean.setCalculated(true);
            xCentMean.setNickName(getFullName(inputImageName,Measurements.X_CENT_MEAN));
        }

        MeasurementReference xCentStdev = objectMeasurementReferences.get(Measurements.X_CENT_STD);
        xCentStdev.setCalculated(false);
        if (parameters.getValue(MEASURE_WEIGHTED_CENTRE)) {
            xCentStdev.setCalculated(true);
            xCentStdev.setNickName(getFullName(inputImageName,Measurements.X_CENT_STD));
        }

        MeasurementReference yCentMean = objectMeasurementReferences.get(Measurements.Y_CENT_MEAN);
        yCentMean.setCalculated(false);
        if (parameters.getValue(MEASURE_WEIGHTED_CENTRE)) {
            yCentMean.setCalculated(true);
            yCentMean.setNickName(getFullName(inputImageName,Measurements.Y_CENT_MEAN));
        }

        MeasurementReference yCentStdev = objectMeasurementReferences.get(Measurements.Y_CENT_STD);
        yCentStdev.setCalculated(false);
        if (parameters.getValue(MEASURE_WEIGHTED_CENTRE)) {
            yCentStdev.setCalculated(true);
            yCentStdev.setNickName(getFullName(inputImageName,Measurements.Y_CENT_STD));
        }

        MeasurementReference zCentMean = objectMeasurementReferences.get(Measurements.Z_CENT_MEAN);
        zCentMean.setCalculated(false);
        if (parameters.getValue(MEASURE_WEIGHTED_CENTRE)) {
            zCentMean.setCalculated(true);
            zCentMean.setNickName(getFullName(inputImageName,Measurements.Z_CENT_MEAN));
        }

        MeasurementReference zCentStdev = objectMeasurementReferences.get(Measurements.Z_CENT_STD);
        zCentStdev.setCalculated(false);
        if (parameters.getValue(MEASURE_WEIGHTED_CENTRE)) {
            zCentStdev.setCalculated(true);
            zCentStdev.setNickName(getFullName(inputImageName,Measurements.Z_CENT_STD));
        }

        return objectMeasurementReferences;

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
