package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Object.Track;

/**
 * Created by steph on 24/05/2017.
 */
public class MeasureTrackMotion extends HCModule {
    public static final String INPUT_TRACK_OBJECTS = "Input track objects";
    public static final String INPUT_SPOT_OBJECTS = "Input spot objects";


    private interface Measurements {
        String DIRECTIONALITY_RATIO = "TRACK_ANALYSIS//DIRECTIONALITY_RATIO";
        String DURATION = "TRACK_ANALYSIS//DURATION";
        String TOTAL_PATH_LENGTH = "TRACK_ANALYSIS//TOTAL_PATH_LENGTH";
        String EUCLIDEAN_DISTANCE = "TRACK_ANALYSIS//EUCLIDEAN_DISTANCE";

    }


    @Override
    public String getTitle() {
        return "Measure track motion";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void run(Workspace workspace, boolean verbose) {
        // Getting input track objects
        String inputTrackObjectsName = parameters.getValue(INPUT_TRACK_OBJECTS);
        ObjCollection inputTrackObjects = workspace.getObjects().get(inputTrackObjectsName);

        // Getting input spot objects
        String inputSpotObjectsName = parameters.getValue(INPUT_SPOT_OBJECTS);

        // Converting objects to Track class object
        for (Obj inputTrackObject:inputTrackObjects.values()) {

            // Initialising stores for coordinates
            double[] x = new double[inputTrackObject.getChildren(inputSpotObjectsName).size()];
            double[] y = new double[inputTrackObject.getChildren(inputSpotObjectsName).size()];
            double[] z = new double[inputTrackObject.getChildren(inputSpotObjectsName).size()];
            int[] f = new int[inputTrackObject.getChildren(inputSpotObjectsName).size()];

            // Getting the corresponding spots for this track
            int iter = 0;
            for (Obj spotObject : inputTrackObject.getChildren(inputSpotObjectsName).values()) {
                x[iter] = spotObject.getXMean(true);
                y[iter] = spotObject.getYMean(true);
                z[iter] = spotObject.getZMean(true,true);
                f[iter] = spotObject.getT();
                iter++;

            }

            // Create track object
            Track track = new Track(x, y, z, f);

            if (x.length == 0) {
                // Adding measurements to track objects
                Measurement measurement = new Measurement(Measurements.DIRECTIONALITY_RATIO, Double.NaN);
                measurement.setSource(this);
                inputTrackObject.addMeasurement(measurement);

                measurement = new Measurement(Measurements.EUCLIDEAN_DISTANCE, Double.NaN);
                measurement.setSource(this);
                inputTrackObject.addMeasurement(measurement);

                measurement = new Measurement(Measurements.TOTAL_PATH_LENGTH, Double.NaN);
                measurement.setSource(this);
                inputTrackObject.addMeasurement(measurement);

                measurement = new Measurement(Measurements.DURATION, Double.NaN);
                measurement.setSource(this);
                inputTrackObject.addMeasurement(measurement);

            } else {
                // Adding measurements to track objects
                Measurement measurement = new Measurement(Measurements.DIRECTIONALITY_RATIO, track.getDirectionalityRatio(false));
                measurement.setSource(this);
                inputTrackObject.addMeasurement(measurement);

                measurement = new Measurement(Measurements.EUCLIDEAN_DISTANCE, track.getEuclideanDistance(false));
                measurement.setSource(this);
                inputTrackObject.addMeasurement(measurement);

                measurement = new Measurement(Measurements.TOTAL_PATH_LENGTH, track.getTotalPathLength(false));
                measurement.setSource(this);
                inputTrackObject.addMeasurement(measurement);

                measurement = new Measurement(Measurements.DURATION, track.getDuration());
                measurement.setSource(this);
                inputTrackObject.addMeasurement(measurement);

            }

        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter( INPUT_TRACK_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter( INPUT_SPOT_OBJECTS, Parameter.CHILD_OBJECTS,null));

    }

    @Override
    protected void initialiseMeasurementReferences() {
        objectMeasurementReferences.add(new MeasurementReference(Measurements.DIRECTIONALITY_RATIO));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.EUCLIDEAN_DISTANCE));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.TOTAL_PATH_LENGTH));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.DURATION));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_TRACK_OBJECTS));
        returnedParameters.add(parameters.getParameter(INPUT_SPOT_OBJECTS));

        // Updating measurements with measurement choices from currently-selected object
        String objectName = parameters.getValue(INPUT_TRACK_OBJECTS);
        if (objectName != null) {
            parameters.updateValueSource(INPUT_SPOT_OBJECTS, objectName);

        } else {
            parameters.updateValueSource(INPUT_SPOT_OBJECTS, null);

        }

        return returnedParameters;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        String inputTrackObjects = parameters.getValue(INPUT_TRACK_OBJECTS);

        objectMeasurementReferences.updateImageObjectName(Measurements.DIRECTIONALITY_RATIO,inputTrackObjects);
        objectMeasurementReferences.updateImageObjectName(Measurements.EUCLIDEAN_DISTANCE,inputTrackObjects);
        objectMeasurementReferences.updateImageObjectName(Measurements.TOTAL_PATH_LENGTH,inputTrackObjects);
        objectMeasurementReferences.updateImageObjectName(Measurements.DURATION,inputTrackObjects);

        return objectMeasurementReferences;

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
