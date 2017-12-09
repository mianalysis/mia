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
                Measurement measurement = new Measurement(Measurement.DIRECTIONALITY_RATIO, Double.NaN);
                measurement.setSource(this);
                inputTrackObject.addMeasurement(measurement);

                measurement = new Measurement(Measurement.EUCLIDEAN_DISTANCE, Double.NaN);
                measurement.setSource(this);
                inputTrackObject.addMeasurement(measurement);

                measurement = new Measurement(Measurement.TOTAL_PATH_LENGTH, Double.NaN);
                measurement.setSource(this);
                inputTrackObject.addMeasurement(measurement);

                measurement = new Measurement(Measurement.DURATION, Double.NaN);
                measurement.setSource(this);
                inputTrackObject.addMeasurement(measurement);

            } else {
                // Adding measurements to track objects
                Measurement measurement = new Measurement(Measurement.DIRECTIONALITY_RATIO, track.getDirectionalityRatio(false));
                measurement.setSource(this);
                inputTrackObject.addMeasurement(measurement);

                measurement = new Measurement(Measurement.EUCLIDEAN_DISTANCE, track.getEuclideanDistance(false));
                measurement.setSource(this);
                inputTrackObject.addMeasurement(measurement);

                measurement = new Measurement(Measurement.TOTAL_PATH_LENGTH, track.getTotalPathLength(false));
                measurement.setSource(this);
                inputTrackObject.addMeasurement(measurement);

                measurement = new Measurement(Measurement.DURATION, track.getDuration());
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
        objectMeasurementReferences.add(new MeasurementReference(Measurement.DIRECTIONALITY_RATIO));
        objectMeasurementReferences.add(new MeasurementReference(Measurement.EUCLIDEAN_DISTANCE));
        objectMeasurementReferences.add(new MeasurementReference(Measurement.TOTAL_PATH_LENGTH));
        objectMeasurementReferences.add(new MeasurementReference(Measurement.DURATION));

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

        objectMeasurementReferences.updateImageObjectName(Measurement.DIRECTIONALITY_RATIO,inputTrackObjects);
        objectMeasurementReferences.updateImageObjectName(Measurement.EUCLIDEAN_DISTANCE,inputTrackObjects);
        objectMeasurementReferences.updateImageObjectName(Measurement.TOTAL_PATH_LENGTH,inputTrackObjects);
        objectMeasurementReferences.updateImageObjectName(Measurement.DURATION,inputTrackObjects);

        return objectMeasurementReferences;

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
