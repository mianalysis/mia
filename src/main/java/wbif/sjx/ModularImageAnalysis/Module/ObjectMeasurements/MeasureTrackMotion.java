//package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements;
//
//import wbif.sjx.ModularImageAnalysis.Module.HCModule;
//import wbif.sjx.ModularImageAnalysis.Object.*;
//import wbif.sjx.common.Object.Track;
//
///**
// * Created by steph on 24/05/2017.
// */
//public class MeasureTrackMotion extends HCModule {
//    public static final String INPUT_TRACK_OBJECTS = "Input track objects";
//    public static final String INPUT_SPOT_OBJECTS = "Input spot objects";
//
//    private ImageObjReference inputTrackObjects;
//
//
//    @Override
//    public String getTitle() {
//        return "Measure track motion";
//    }
//
//    @Override
//    public String getHelp() {
//        return null;
//    }
//
//    @Override
//    public void run(Workspace workspace, boolean verbose) {
//        // Getting input track objects
//        String inputTrackObjectsName = parameters.getValue(INPUT_TRACK_OBJECTS);
//        ObjCollection inputTrackObjects = workspace.getObjects().get(inputTrackObjectsName);
//
//        // Getting input spot objects
//        String inputSpotObjectsName = parameters.getValue(INPUT_SPOT_OBJECTS);
//
//        // Converting objects to Track class object
//        for (Obj inputTrackObject:inputTrackObjects.values()) {
//
//            // Initialising stores for coordinates
//            double[] x = new double[inputTrackObject.getChildren(inputSpotObjectsName).size()];
//            double[] y = new double[inputTrackObject.getChildren(inputSpotObjectsName).size()];
//            double[] z = new double[inputTrackObject.getChildren(inputSpotObjectsName).size()];
//            int[] f = new int[inputTrackObject.getChildren(inputSpotObjectsName).size()];
//
//            // Getting the corresponding spots for this track
//            int iter = 0;
//            for (Obj spotObject : inputTrackObject.getChildren(inputSpotObjectsName).values()) {
//                x[iter] = spotObject.getXMean(true);
//                y[iter] = spotObject.getYMean(true);
//                z[iter] = spotObject.getZMean(true,true);
//                f[iter] = spotObject.getT();
//                iter++;
//
//            }
//
//            // Create track object
//            Track track = new Track(x, y, z, f);
//
//            if (x.length == 0) {
//                // Adding measurements to track objects
//                Measurement measurement = new Measurement(Measurement.DIRECTIONALITY_RATIO, Double.NaN);
//                measurement.setSource(this);
//                inputTrackObject.addMeasurement(measurement);
//
//                measurement = new Measurement(Measurement.EUCLIDEAN_DISTANCE, Double.NaN);
//                measurement.setSource(this);
//                inputTrackObject.addMeasurement(measurement);
//
//                measurement = new Measurement(Measurement.TOTAL_PATH_LENGTH, Double.NaN);
//                measurement.setSource(this);
//                inputTrackObject.addMeasurement(measurement);
//
//                measurement = new Measurement(Measurement.DURATION, Double.NaN);
//                measurement.setSource(this);
//                inputTrackObject.addMeasurement(measurement);
//
//            } else {
//                // Adding measurements to track objects
//                Measurement measurement = new Measurement(Measurement.DIRECTIONALITY_RATIO, track.getDirectionalityRatio(false));
//                measurement.setSource(this);
//                inputTrackObject.addMeasurement(measurement);
//
//                measurement = new Measurement(Measurement.EUCLIDEAN_DISTANCE, track.getEuclideanDistance(false));
//                measurement.setSource(this);
//                inputTrackObject.addMeasurement(measurement);
//
//                measurement = new Measurement(Measurement.TOTAL_PATH_LENGTH, track.getTotalPathLength(false));
//                measurement.setSource(this);
//                inputTrackObject.addMeasurement(measurement);
//
//                measurement = new Measurement(Measurement.DURATION, track.getDuration());
//                measurement.setSource(this);
//                inputTrackObject.addMeasurement(measurement);
//
//            }
//
//        }
//    }
//
//    @Override
//    public void initialiseParameters() {
//        parameters.addParameter(new Parameter( INPUT_TRACK_OBJECTS, Parameter.INPUT_OBJECTS,null));
//        parameters.addParameter(new Parameter( INPUT_SPOT_OBJECTS, Parameter.CHILD_OBJECTS,null));
//
//    }
//
//    @Override
//    public ParameterCollection updateAndGetParameters() {
//        ParameterCollection returnedParameters = new ParameterCollection();
//        returnedParameters.addParameter(parameters.getParameter(INPUT_TRACK_OBJECTS));
//        returnedParameters.addParameter(parameters.getParameter(INPUT_SPOT_OBJECTS));
//
//        // Updating measurements with measurement choices from currently-selected object
//        String objectName = parameters.getValue(INPUT_TRACK_OBJECTS);
//        if (objectName != null) {
//            parameters.updateValueSource(INPUT_SPOT_OBJECTS, objectName);
//
//        } else {
//            parameters.updateValueSource(INPUT_SPOT_OBJECTS, null);
//
//        }
//
//        return returnedParameters;
//    }
//
//    @Override
//    public void initialiseImageReferences() {
//        inputTrackObjects = new ImageObjReference();
//        objectReferences.add(inputTrackObjects);
//
//        inputTrackObjects.addMeasurementReference(new MeasurementReference(Measurement.DIRECTIONALITY_RATIO));
//        inputTrackObjects.addMeasurementReference(new MeasurementReference(Measurement.EUCLIDEAN_DISTANCE));
//        inputTrackObjects.addMeasurementReference(new MeasurementReference(Measurement.TOTAL_PATH_LENGTH));
//        inputTrackObjects.addMeasurementReference(new MeasurementReference(Measurement.DURATION));
//
//    }
//
//    @Override
//    public ReferenceCollection updateAndGetImageReferences() {
//        return null;
//    }
//
//    @Override
//    public ReferenceCollection updateAndGetObjectReferences() {
//        inputTrackObjects.setName(parameters.getValue(INPUT_TRACK_OBJECTS));
//
//        return objectReferences;
//
//    }
//
//    @Override
//    public void addRelationships(RelationshipCollection relationships) {
//
//    }
//}
