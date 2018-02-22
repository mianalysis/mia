// TODO: Add measurements

package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.util.ArrayList;

/**
 * Created by sc13967 on 11/05/2017.
 */
public class MeasureObjectCentroid extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String CENTROID_METHOD = "Centroid method";

    public interface Methods {
        String MEAN = "Mean";
        String MEDIAN = "Median";
        String BOTH = "Both";

        String[] ALL = new String[]{MEAN, MEDIAN, BOTH};

    }

    public interface Measurements {
        String MEAN_X_PX = "CENTROID//MEAN_X_PX";
        String MEAN_Y_PX = "CENTROID//MEAN_Y_PX";
        String MEAN_Z_SLICE = "CENTROID//MEAN_Z_SLICE";
        String MEAN_X_CAL = "CENTROID//MEAN_X_CAL";
        String MEAN_Y_CAL = "CENTROID//MEAN_Y_CAL";
        String MEAN_Z_CAL = "CENTROID//MEAN_Z_CAL";
        String MEDIAN_X_PX = "CENTROID//MEDIAN_X_PX";
        String MEDIAN_Y_PX = "CENTROID//MEDIAN_Y_PX";
        String MEDIAN_Z_SLICE = "CENTROID//MEDIAN_Z_SLICE";
        String MEDIAN_X_CAL = "CENTROID//MEDIAN_X_CAL";
        String MEDIAN_Y_CAL = "CENTROID//MEDIAN_Y_CAL";
        String MEDIAN_Z_CAL = "CENTROID//MEDIAN_Z_CAL";

    }


    @Override
    public String getTitle() {
        return "Measure object centroid";

    }

    @Override
    public String getHelp() {
        return "Z-coordinates are specified in terms of slices (not pixels)";
    }

    @Override
    public void run(Workspace workspace, boolean verbose) {
        // Getting current objects
        String inputObjectName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectName);

        // Getting which centroid measures to calculate
        String choice = parameters.getValue(CENTROID_METHOD);
        boolean useMean = choice.equals(Methods.MEAN) | choice.equals(Methods.BOTH);
        boolean useMedian = choice.equals(Methods.MEDIAN) | choice.equals(Methods.BOTH);
        writeMessage("Calculating centroid as "+choice,verbose);

        // Getting the centroids of each and saving them to the objects
        for (Obj object:inputObjects.values()) {
            ArrayList<Integer> x = object.getXCoords();
            ArrayList<Integer> y = object.getYCoords();
            ArrayList<Integer> z = object.getZCoords();

            if (useMean) {
                if (x != null) {
                    object.addMeasurement(new Measurement(Measurements.MEAN_X_PX,object.getXMean(true)));
                    object.addMeasurement(new Measurement(Measurements.MEAN_X_CAL,object.getXMean(false)));
                }
                if (y!= null) {
                    object.addMeasurement(new Measurement(Measurements.MEAN_Y_PX,object.getYMean(true)));
                    object.addMeasurement(new Measurement(Measurements.MEAN_Y_CAL,object.getYMean(false)));
                }
                if (z!= null) {
                    object.addMeasurement(new Measurement(Measurements.MEAN_Z_SLICE,object.getZMean(true,false)));
                    object.addMeasurement(new Measurement(Measurements.MEAN_Z_CAL,object.getZMean(false,false)));
                }
            }

            if (useMedian) {
                if (x != null) {
                    object.addMeasurement(new Measurement(Measurements.MEDIAN_X_PX,object.getXMedian(true)));
                    object.addMeasurement(new Measurement(Measurements.MEDIAN_X_CAL,object.getXMedian(false)));
                }
                if (y!= null) {
                    object.addMeasurement(new Measurement(Measurements.MEDIAN_Y_PX,object.getYMedian(true)));
                    object.addMeasurement(new Measurement(Measurements.MEDIAN_Y_CAL,object.getYMedian(false)));
                }
                if (z!= null) {
                    object.addMeasurement(new Measurement(Measurements.MEDIAN_Z_SLICE,object.getZMedian(true,false)));
                    object.addMeasurement(new Measurement(Measurements.MEDIAN_Z_CAL,object.getZMedian(false,false)));
                }
            }
        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(CENTROID_METHOD, Parameter.CHOICE_ARRAY,Methods.MEAN,Methods.ALL));

    }

    @Override
    protected void initialiseMeasurementReferences() {
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MEAN_X_PX));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MEAN_Y_PX));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MEAN_Z_SLICE));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MEAN_X_CAL));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MEAN_Y_CAL));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MEAN_Z_CAL));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MEDIAN_X_PX));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MEDIAN_Y_PX));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MEDIAN_Z_SLICE));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MEDIAN_X_CAL));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MEDIAN_Y_CAL));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.MEDIAN_Z_CAL));

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
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        MeasurementReference meanXPx = objectMeasurementReferences.get(Measurements.MEAN_X_PX);
        MeasurementReference meanYPx = objectMeasurementReferences.get(Measurements.MEAN_Y_PX);
        MeasurementReference meanZSlice = objectMeasurementReferences.get(Measurements.MEAN_Z_SLICE);
        MeasurementReference meanXCal = objectMeasurementReferences.get(Measurements.MEAN_X_CAL);
        MeasurementReference meanYCal = objectMeasurementReferences.get(Measurements.MEAN_Y_CAL);
        MeasurementReference meanZCal = objectMeasurementReferences.get(Measurements.MEAN_Z_CAL);
        MeasurementReference medianXPx = objectMeasurementReferences.get(Measurements.MEDIAN_X_PX);
        MeasurementReference medianYPx = objectMeasurementReferences.get(Measurements.MEDIAN_Y_PX);
        MeasurementReference medianZSlice = objectMeasurementReferences.get(Measurements.MEDIAN_Z_SLICE);
        MeasurementReference medianXCal = objectMeasurementReferences.get(Measurements.MEDIAN_X_CAL);
        MeasurementReference medianYCal = objectMeasurementReferences.get(Measurements.MEDIAN_Y_CAL);
        MeasurementReference medianZCal = objectMeasurementReferences.get(Measurements.MEDIAN_Z_CAL);

        meanXPx.setImageObjName(inputObjectsName);
        meanYPx.setImageObjName(inputObjectsName);
        meanZSlice.setImageObjName(inputObjectsName);
        meanXCal.setImageObjName(inputObjectsName);
        meanYCal.setImageObjName(inputObjectsName);
        meanZCal.setImageObjName(inputObjectsName);
        medianXPx.setImageObjName(inputObjectsName);
        medianYPx.setImageObjName(inputObjectsName);
        medianZSlice.setImageObjName(inputObjectsName);
        medianXCal.setImageObjName(inputObjectsName);
        medianYCal.setImageObjName(inputObjectsName);
        medianZCal.setImageObjName(inputObjectsName);

        String choice = parameters.getValue(CENTROID_METHOD);
        boolean useMean = choice.equals(Methods.MEAN) | choice.equals(Methods.BOTH);
        boolean useMedian = choice.equals(Methods.MEDIAN) | choice.equals(Methods.BOTH);

        meanXPx.setCalculated(false);
        meanYPx.setCalculated(false);
        meanZSlice.setCalculated(false);
        meanXCal.setCalculated(false);
        meanYCal.setCalculated(false);
        meanZCal.setCalculated(false);
        medianXPx.setCalculated(false);
        medianYPx.setCalculated(false);
        medianZSlice.setCalculated(false);
        medianXCal.setCalculated(false);
        medianYCal.setCalculated(false);
        medianZCal.setCalculated(false);

        if (useMean) {
            meanXPx.setCalculated(true);
            meanYPx.setCalculated(true);
            meanZSlice.setCalculated(true);
            meanXCal.setCalculated(true);
            meanYCal.setCalculated(true);
            meanZCal.setCalculated(true);
        }

        if (useMedian) {
            medianXPx.setCalculated(true);
            medianYPx.setCalculated(true);
            medianZSlice.setCalculated(true);
            medianXCal.setCalculated(true);
            medianYCal.setCalculated(true);
            medianZCal.setCalculated(true);
        }

        return objectMeasurementReferences;

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
