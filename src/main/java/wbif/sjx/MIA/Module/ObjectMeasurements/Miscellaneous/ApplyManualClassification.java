//package wbif.sjx.MIA.Module.ObjectMeasurements.Miscellaneous;
//
//import wbif.sjx.MIA.Module.Module;
//import wbif.sjx.MIA.Module.PackageNames;
//import wbif.sjx.MIA.Object.*;
//import wbif.sjx.MIA.Object.Parameters.*;
//
//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.LinkedHashSet;
//
///**
// * Applies manual object classifications from a .csv file at the specified location.  Each row of the file must
// * correspond to a different object and have the format [ID],[Classification]
// */
//public class ApplyManualClassification extends Module {
//    public static final String TRACK_OBJECTS = "Input objects";
//    public static final String CLASSIFICATION_FILE = "Classification file";
//    public static final String ADD_MEASUREMENT = "Add measurement";
//    public static final String MEASUREMENT = "Measurement";
//    public static final String REMOVE_MISSING = "Remove objects without classification";
//
//
//    public interface Measurements {
//        String CLASS = "CLASSIFIER // CLASS";
//
//    }
//
//    @Override
//    public String getTitle() {
//        return "Apply manual classification";
//
//    }
//
//    @Override
//    public String getPackageName() {
//        return PackageNames.OBJECT_MEASUREMENTS_MISCELLANEOUS;
//    }
//
//    @Override
//    public String getDescription() {
//        return "";
//    }
//
//    @Override
//    public Status process(Workspace workspace) {
//        // Getting input objects
//        String inputObjectsName = parameters.getValue(TRACK_OBJECTS);
//        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);
//
//        // Getting classification file and storing classifications as HashMap that can be easily read later on
//        String classificationFilePath = parameters.getValue(CLASSIFICATION_FILE);
//        try {
//            BufferedReader bufferedReader = new BufferedReader(new FileReader(classificationFilePath));
//            String line;
//            while((line=bufferedReader.readLine())!=null){
//                // Getting current object value
//                String vals[] = line.split(",");
//                double clazz = Double.valueOf(vals[0]);
//                int x = Integer.valueOf(vals[0]);
//                int y = Integer.valueOf(vals[1]);
//                int f = Integer.valueOf(vals[2]);
//                int currClass = Integer.valueOf(vals[3]);
//
//                for (Obj object:inputObjects.values()) {
//                    double xCent = object.getXMean(true);
//                    double yCent = object.getYMean(true);
//                    int timepoint = object.getT();
//
//                    if (xCent==x & yCent==y & timepoint == f) {
//                        Measurement objClass = new Measurement(Measurements.CLASS,currClass);
//                        objClass.setSource(this);
//                        object.addMeasurement(objClass);
//
//                        break;
//                    }
//                }
//            }
//
//            bufferedReader.close();
//
//            // Removing objects that don't have an assigned class (first removing the parent-child relationships).
//            // Otherwise, the class measurement is set to Double.NaN
//            if (parameters.getValue(REMOVE_MISSING)) {
//                for (Obj object : inputObjects.values()) {
//                    if (object.getMeasurement(Measurements.CLASS) == null) {
//                        object.removeRelationships();
//                    }
//                }
//                inputObjects.entrySet().removeIf(entry -> entry.getValue().getMeasurement(Measurements.CLASS) == null);
//
//            } else {
//                for (Obj object : inputObjects.values()) {
//                    if (object.getMeasurement(Measurements.CLASS) == null) {
//                        Measurement objClass = new Measurement(Measurements.CLASS,Double.NaN);
//                        objClass.setSource(this);
//                        object.addMeasurement(objClass);
//                    }
//                }
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        if (showOutput) inputObjects.showMeasurements(this,modules);
//
//        return true;
//
//    }
//
//    @Override
//    protected void initialiseParameters() {
//        parameters.addRef(new InputObjectsP(TRACK_OBJECTS,this));
//        parameters.addRef(new FilePathP(CLASSIFICATION_FILE, this));
//
//        ParameterCollection collection = new ParameterCollection();
//        collection.addRef(new ObjectMeasurementP(MEASUREMENT,this));
//        parameters.addRef(new ParameterGroup(ADD_MEASUREMENT,this,collection));
//        parameters.addRef(new BooleanP(REMOVE_MISSING, this,false));
//
//    }
//
//    @Override
//    public ParameterCollection updateAndGetParameters() {
//        String inputObjectsName = parameters.getValue(TRACK_OBJECTS);
//
//        ParameterGroup parameterGroup = parameters.getParameter(ADD_MEASUREMENT);
//        LinkedHashSet<ParameterCollection> collections = parameterGroup.getCollections();
//        for (ParameterCollection collection:collections) {
//            ((ObjectMeasurementP) collection.getParameter(MEASUREMENT)).setObjectName(inputObjectsName);
//        }
//
//        return parameters;
//
//    }
//
//    @Override
//    public ObjMeasurementRefCollection updateAndGetImageMeasurementRefs() {
//        return null;
//    }
//
//    @Override
//    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
//        objectMeasurementRefs.setAllAvailable(false);
//
//        MeasurementRef classMeas = objectMeasurementRefs.getOrPut(Measurements.CLASS);
//        classMeas.setObjectsName(parameters.getValue(TRACK_OBJECTS));
//        classMeas.setAvailable(true);
//
//        return objectMeasurementRefs;
//
//    }
//
//    @Override
//    public MetadataRefCollection updateAndGetMetadataReferences() {
//        return null;
//    }
//
//    @Override
//    public ParentChildRefCollection updateAndGetParentChildRefs() {
//        return null;
//    }
//
//}
