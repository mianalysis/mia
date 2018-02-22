package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Object.Track;

import java.util.TreeMap;

/**
 * Created by steph on 24/05/2017.
 */
public class MeasureTrackMotion extends Module {
    public static final String INPUT_TRACK_OBJECTS = "Input track objects";
    public static final String INPUT_SPOT_OBJECTS = "Input spot objects";


    private interface Measurements {
        String DURATION = "TRACK_ANALYSIS//DURATION";
        String TOTAL_PATH_LENGTH = "TRACK_ANALYSIS//TOTAL_PATH_LENGTH";
        String EUCLIDEAN_DISTANCE = "TRACK_ANALYSIS//EUCLIDEAN_DISTANCE";
        String DIRECTIONALITY_RATIO = "TRACK_ANALYSIS//DIRECTIONALITY_RATIO";
        String INSTANTANEOUS_VELOCITY = "TRACK_ANALYSIS//INSTANTANEOUS_VELOCITY";
        String CUMULATIVE_PATH_LENGTH = "TRACK_ANALYSIS//CUMULATIVE_PATH_LENGTH";
        String ROLLING_EUCLIDEAN_DISTANCE = "TRACK_ANALYSIS//ROLLING_EUCLIDEAN_DISTANCE";
        String ROLLING_DIRECTIONALITY_RATIO = "TRACK_ANALYSIS//ROLLING_DIRECTIONALITY_RATIO";

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
                inputTrackObject.addMeasurement(new Measurement(Measurements.DURATION, Double.NaN));
                inputTrackObject.addMeasurement(new Measurement(Measurements.EUCLIDEAN_DISTANCE, Double.NaN));
                inputTrackObject.addMeasurement(new Measurement(Measurements.TOTAL_PATH_LENGTH, Double.NaN));
                inputTrackObject.addMeasurement(new Measurement(Measurements.DIRECTIONALITY_RATIO, Double.NaN));

            } else {
                // Adding measurements to track objects
                inputTrackObject.addMeasurement(new Measurement(Measurements.DURATION, track.getDuration()));
                inputTrackObject.addMeasurement(new Measurement(Measurements.EUCLIDEAN_DISTANCE, track.getEuclideanDistance(false)));
                inputTrackObject.addMeasurement(new Measurement(Measurements.TOTAL_PATH_LENGTH, track.getTotalPathLength(false)));
                inputTrackObject.addMeasurement(new Measurement(Measurements.DIRECTIONALITY_RATIO, track.getDirectionalityRatio(false)));
            }

            // Calculating rolling values
            TreeMap<Integer, Double> velocity = track.getInstantaneousVelocity(false);
            TreeMap<Integer, Double> pathLength = track.getRollingTotalPathLength(false);
            TreeMap<Integer, Double> euclidean = track.getRollingEuclideanDistance(false);
            TreeMap<Integer, Double> dirRatio = track.getRollingDirectionalityRatio(false);

            for (Obj spotObject : inputTrackObject.getChildren(inputSpotObjectsName).values()) {
                int t = spotObject.getT();
                spotObject.addMeasurement(new Measurement(Measurements.INSTANTANEOUS_VELOCITY, velocity.get(t)));
                spotObject.addMeasurement(new Measurement(Measurements.CUMULATIVE_PATH_LENGTH, pathLength.get(t)));
                spotObject.addMeasurement(new Measurement(Measurements.ROLLING_EUCLIDEAN_DISTANCE, euclidean.get(t)));
                spotObject.addMeasurement(new Measurement(Measurements.ROLLING_DIRECTIONALITY_RATIO, dirRatio.get(t)));
            }
        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_TRACK_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(INPUT_SPOT_OBJECTS, Parameter.CHILD_OBJECTS,null));

    }

    @Override
    protected void initialiseMeasurementReferences() {
        objectMeasurementReferences.add(new MeasurementReference(Measurements.DIRECTIONALITY_RATIO));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.EUCLIDEAN_DISTANCE));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.TOTAL_PATH_LENGTH));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.DURATION));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.INSTANTANEOUS_VELOCITY));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.CUMULATIVE_PATH_LENGTH));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.ROLLING_EUCLIDEAN_DISTANCE));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.ROLLING_DIRECTIONALITY_RATIO));

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

        String inputSpotObjects  = parameters.getValue(INPUT_SPOT_OBJECTS);
        objectMeasurementReferences.updateImageObjectName(Measurements.INSTANTANEOUS_VELOCITY,inputSpotObjects);
        objectMeasurementReferences.updateImageObjectName(Measurements.CUMULATIVE_PATH_LENGTH,inputSpotObjects);
        objectMeasurementReferences.updateImageObjectName(Measurements.ROLLING_EUCLIDEAN_DISTANCE,inputSpotObjects);
        objectMeasurementReferences.updateImageObjectName(Measurements.ROLLING_DIRECTIONALITY_RATIO,inputSpotObjects);

        return objectMeasurementReferences;

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
