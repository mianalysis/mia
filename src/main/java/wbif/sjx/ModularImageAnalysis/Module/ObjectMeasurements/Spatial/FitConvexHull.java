//package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial;
//
//import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
//import wbif.sjx.ModularImageAnalysis.Module.Module;
//import wbif.sjx.ModularImageAnalysis.Object.*;
//import wbif.sjx.common.Analysis.ConvexHullCalculator;
//import wbif.sjx.common.Object.Volume;
//
///**
// * Created by sc13967 on 18/06/2018.
// */
//public class FitConvexHull extends Module {
//    public static final String INPUT_TRACK_OBJECTS = "Input objects";
//    public static final String OBJECT_OUTPUT_MODE = "Object output mode";
//    public static final String OUTPUT_OBJECTS = "Output objects";
//    public static final String FITTING_MODE = "Fitting mode";
//
//
//    public interface OutputModes {
//        String DO_NOT_STORE = "Do not store";
//        String CREATE_NEW_OBJECT = "Create new objects";
//        String UPDATE_INPUT = "Update input objects";
//
//        String[] ALL = new String[]{DO_NOT_STORE,CREATE_NEW_OBJECT,UPDATE_INPUT};
//
//    }
//
//    public interface FittingModes {
//        String CENTROIDS = "Pixel centroids";
//        String CORNERS = "Pixel corners";
//
//        String[] ALL = new String[]{CENTROIDS,CORNERS};
//
//    }
//
//    public interface Measurements {
//        String HULL_VOLUME_PX = "CONVEX_HULL // VOLUME_(PX^3)";
//        String HULL_VOLUME_CAL = "CONVEX_HULL // VOLUME_(${CAL}^3)";
//        String HULL_SURFACE_AREA_PX = "CONVEX_HULL // SURFACE_AREA_(PX^2)";
//        String HULL_SURFACE_AREA_CAL = "CONVEX_HULL // SURFACE_AREA_(${CAL}^2)";
//        String SPHERICITY = "CONVEX_HULL // SPHERICITY";
//        String SOLIDITY = "CONVEX_HULL // SOLIDITY";
//
//    }
//
//    public void processObject(Obj inputObject, int mode, ObjCollection outputObjects, String objectOutputMode) {
//        ConvexHullCalculator calculator = new ConvexHullCalculator(inputObject, mode,1E-8);
//
//        // Adding measurements
//        addMeasurements(inputObject,calculator);
//
//        // If the hull can't be fit, terminating the current iteration
//        if (!calculator.canFitHull()) return;
//
//        Volume hull = !objectOutputMode.equals(OutputModes.DO_NOT_STORE) ? calculator.getContainedPoints() : null;
//
//        switch (objectOutputMode) {
//            case OutputModes.CREATE_NEW_OBJECT:
//                Obj hullObject = createNewObject(inputObject,hull,outputObjects);
//                if (hullObject != null) outputObjects.add(hullObject);
//                break;
//            case OutputModes.UPDATE_INPUT:
//                updateInputObject(inputObject,hull);
//                break;
//        }
//    }
//
//    public int getFitMode(String fittingMode) {
//        // Getting the mode with which to generateModuleList the ConvexHullCalculator
//        switch (fittingMode) {
//            case FittingModes.CENTROIDS:
//                return ConvexHullCalculator.CENTROID;
//            case FittingModes.CORNERS:
//                return ConvexHullCalculator.CORNER;
//        }
//
//        return 0;
//
//    }
//
//    public void addMeasurements(Obj inputObject, ConvexHullCalculator calculator) {
//        // If the convex hull can't be fit add blank measurements
//        if (!calculator.canFitHull()) {
//            inputObject.addMeasurement(new Measurement(Measurements.HULL_VOLUME_PX, Double.NaN));
//            inputObject.addMeasurement(new Measurement(Units.replace(Measurements.HULL_VOLUME_CAL), Double.NaN));
//            inputObject.addMeasurement(new Measurement(Measurements.HULL_SURFACE_AREA_PX, Double.NaN));
//            inputObject.addMeasurement(new Measurement(Units.replace(Measurements.HULL_SURFACE_AREA_CAL), Double.NaN));
//            inputObject.addMeasurement(new Measurement(Measurements.SPHERICITY, Double.NaN));
//            inputObject.addMeasurement(new Measurement(Measurements.SOLIDITY, Double.NaN));
//            return;
//        }
//
//        // Hull volume was calculated using
//        double hullVolumePx = calculator.getHullVolume(true);
//        double hullVolumeCal = calculator.getHullVolume(false);
//        double hullSurfaceAreaPx = calculator.getHullSurfaceArea(true);
//        double hullSurfaceAreaCal = calculator.getHullSurfaceArea(false);
//        double sphericity = calculator.getSphericity();
//        double solidity = calculator.getSolidity();
//
//        inputObject.addMeasurement(new Measurement(Measurements.HULL_VOLUME_PX, hullVolumePx));
//        inputObject.addMeasurement(new Measurement(Units.replace(Measurements.HULL_VOLUME_CAL), hullVolumeCal));
//        inputObject.addMeasurement(new Measurement(Measurements.HULL_SURFACE_AREA_PX, hullSurfaceAreaPx));
//        inputObject.addMeasurement(new Measurement(Units.replace(Measurements.HULL_SURFACE_AREA_CAL), hullSurfaceAreaCal));
//        inputObject.addMeasurement(new Measurement(Measurements.SPHERICITY, sphericity));
//        inputObject.addMeasurement(new Measurement(Measurements.SOLIDITY, solidity));
//    }
//
//    public Obj createNewObject (Obj inputObject, Volume hull, ObjCollection outputObjects) {
//        if (hull == null) return null;
//
//        double dppXY = inputObject.getDistPerPxXY();
//        double dppZ = inputObject.getDistPerPxZ();
//        String units = inputObject.getCalibratedUnits();
//        boolean is2D = inputObject.is2D();
//
//        Obj hullObject = new Obj(outputObjects.getName(),outputObjects.getNextID(),dppXY,dppZ,units,is2D);
//        hullObject.setPoints(hull.getPoints());
//
//        hullObject.addParent(inputObject);
//        inputObject.addChild(hullObject);
//        outputObjects.add(hullObject);
//
//        return hullObject;
//
//    }
//
//    public void updateInputObject(Obj inputObject, Volume hull) {
//        inputObject.setPoints(hull.getPoints());
//    }
//
//
//    @Override
//    public String getTitle() {
//        return "Fit convex hull";
//    }
//
//    @Override
//    public String getHelp() {
//        return "Uses QuickHull3D to fit a convex hull." +
//                "\nhttps://github.com/Quickhull3d/quickhull3d";
//    }
//
//    @Override
//    protected void generateModuleList(Workspace workspace) throws GenericMIAException {
//        // Getting input objects
//        String inputObjectsName = parameters.getValue(INPUT_TRACK_OBJECTS);
//        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);
//
//        // Getting parameters
//        String objectOutputMode = parameters.getValue(OBJECT_OUTPUT_MODE);
//        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
//        String fittingMode = parameters.getValue(FITTING_MODE);
//        int mode = getFitMode(fittingMode);
//
//        // If necessary, creating a new ObjCollection and adding it to the Workspace
//        ObjCollection outputObjects = null;
//        if (objectOutputMode.equals(OutputModes.CREATE_NEW_OBJECT)) {
//            outputObjects = new ObjCollection(outputObjectsName);
//            workspace.addObjects(outputObjects);
//        }
//
//        // Running through each object, taking measurements and adding new object to the workspace where necessary
//        int count = 0;
//        int nTotal = inputObjects.size();
//        for (Obj inputObject:inputObjects.values()) {
//            processObject(inputObject,mode,outputObjects,objectOutputMode);
//            writeMessage("Processed object "+(++count)+" of "+nTotal);
//        }
//    }
//
//    @Override
//    protected void initialiseParameters() {
//        parameters.add(new Parameter(INPUT_TRACK_OBJECTS,this,null));
//        parameters.add(new Parameter(OBJECT_OUTPUT_MODE,this,OutputModes.DO_NOT_STORE,OutputModes.ALL));
//        parameters.add(new Parameter(OUTPUT_OBJECTS,this,""));
//        parameters.add(new Parameter(FITTING_MODE,this,FittingModes.CENTROIDS,FittingModes.ALL));
//
//    }
//
//    @Override
//    public ParameterCollection updateAndGetParameters() {
//        ParameterCollection returnedParameters = new ParameterCollection();
//
//        returnedParameters.add(parameters.getParameter(INPUT_TRACK_OBJECTS));
//
//        returnedParameters.add(parameters.getParameter(OBJECT_OUTPUT_MODE));
//        switch ((String) parameters.getValue(OBJECT_OUTPUT_MODE)) {
//            case OutputModes.CREATE_NEW_OBJECT:
//                returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));
//                break;
//        }
//
//        returnedParameters.add(parameters.getParameter(FITTING_MODE));
//
//        return returnedParameters;
//
//    }
//
//    @Override
//    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
//        return null;
//    }
//
//    @Override
//    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
//        objectMeasurementRefs.setAllCalculated(false);
//
//        String inputObjectsName = parameters.getValue(INPUT_TRACK_OBJECTS);
//
//        MeasurementRef reference = objectMeasurementRefs.getOrPut(Measurements.HULL_VOLUME_PX);
//        reference.setCalculated(true);
//        reference.setImageObjName(inputObjectsName);
//
//        reference = objectMeasurementRefs.getOrPut(Units.replace(Measurements.HULL_VOLUME_CAL));
//        reference.setCalculated(true);
//        reference.setImageObjName(inputObjectsName);
//
//        reference = objectMeasurementRefs.getOrPut(Measurements.HULL_SURFACE_AREA_PX);
//        reference.setCalculated(true);
//        reference.setImageObjName(inputObjectsName);
//
//        reference = objectMeasurementRefs.getOrPut(Units.replace(Measurements.HULL_SURFACE_AREA_CAL));
//        reference.setCalculated(true);
//        reference.setImageObjName(inputObjectsName);
//
//        reference = objectMeasurementRefs.getOrPut(Measurements.SPHERICITY);
//        reference.setCalculated(true);
//        reference.setImageObjName(inputObjectsName);
//
//        reference = objectMeasurementRefs.getOrPut(Measurements.SOLIDITY);
//        reference.setCalculated(true);
//        reference.setImageObjName(inputObjectsName);
//
//        return objectMeasurementRefs;
//
//    }
//
//    @Override
//    public void addRelationships(RelationshipCollection relationships) {
//        switch ((String) parameters.getValue(OBJECT_OUTPUT_MODE)) {
//            case OutputModes.CREATE_NEW_OBJECT:
//                String inputObjectsName = parameters.getValue(INPUT_TRACK_OBJECTS);
//                String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
//                relationships.addRelationship(inputObjectsName,outputObjectsName);
//
//                break;
//        }
//    }
//}
