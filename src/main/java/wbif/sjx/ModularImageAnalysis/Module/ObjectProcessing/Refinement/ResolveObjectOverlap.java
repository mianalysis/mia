//// TODO: For unit test: reverse coordinates for ExpectedObjects 3D
//
//package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Refinement;
//
//import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
//import wbif.sjx.ModularImageAnalysis.Module.Module;
//import wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Intensity.MeasureObjectColocalisation;
//import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
//import wbif.sjx.ModularImageAnalysis.Object.*;
//
//import java.util.HashMap;
//import java.util.Iterator;
//
//public class ResolveObjectOverlap extends Module {
//    public final static String PARENT_OBJECTS = "Input objects 1";
//    public final static String INPUT_OBJECTS_2 = "Input objects 2";
//    public static final String OUTPUT_OBJECTS_NAME = "Output objects name";
//    public static final String OVERLAP_MODE = "Overlap mode";
//    public static final String MAXIMUM_SEPARATION = "Maximum separation";
//    public static final String CALIBRATED_UNITS = "Calibrated units";
//    public static final String MINIMUM_OVERLAP_PC_1 = "Minimum overlap of object 1 (%)";
//    public static final String MINIMUM_OVERLAP_PC_2 = "Minimum overlap of object 2 (%)";
//
//
//    public interface OverlapModes {
//        String CENTROID_SEPARATION = "Centroid separation";
//        String SPATIAL_OVERLAP = "Spatial overlap";
//
//        String[] ALL = new String[]{CENTROID_SEPARATION,SPATIAL_OVERLAP};
//
//    }
//
//    public interface Measurements {
//        String FRACTION_1 = "FRACTION1";
//        String N_VOXELS1 = "N_VOXELS1";
//        String FRACTION_2 = "FRACTION2";
//        String N_VOXELS2 = "N_VOXELS2";
//
//        String[] ALL = new String[]{FRACTION_1,N_VOXELS1,FRACTION_2,N_VOXELS2};
//
//    }
//
//
//    public static String getFullName(String objectName, String measurement) {
//        return "OBJECT_OVERLAP // "+measurement.substring(0,measurement.length()-1)+"_"+objectName;
//
//    }
//
//    private ObjCollection calculateSpatialOverlap(ObjCollection inputObjects1, ObjCollection inputObjects2,
//                                                  String outputObjectsName, double minOverlap1, double minOverlap2) {
//        ObjCollection outputObjects = new ObjCollection(outputObjectsName);
//
//        // Initialising the storage, which has ID number (key) and maximum overlap[0] and object ID[1] (value)
//        HashMap<Integer,Double[]> overlaps1 = initialiseOverlapStore(inputObjects1,-Double.MAX_VALUE);
//        HashMap<Integer,Double[]> overlaps2 = initialiseOverlapStore(inputObjects2,-Double.MAX_VALUE);
//
//        // Calculating the overlaps
//        int totalPairs = inputObjects1.size()*inputObjects2.size();
//        int count = 0;
//        for (Obj object1:inputObjects1.values()) {
//            Double[] overlap1 = overlaps1.get(object1.getID());
//            for (Obj object2:inputObjects2.values()) {
//                count++;
//
//                // Calculate the overlap between the two objects
//                double overlap = object1.getOverlap(object2);
//
//                // Comparing the overlap to previously-maximum overlaps
//                double overlapPercentage1 = 100*overlap/object1.getNVoxels();
//                if (overlapPercentage1>overlap1[0] &&  overlapPercentage1> minOverlap1) {
//                    overlap1[0] = overlapPercentage1;
//                    overlap1[1] = (double) object2.getID();
//                }
//
//                // Comparing the overlap to previously-maximum overlaps
//                Double[] overlap2 = overlaps2.get(object2.getID());
//                double overlapPercentage2 = 100*overlap/object2.getNVoxels();
//                if (overlapPercentage2>overlap2[0] &&  overlapPercentage2> minOverlap2) {
//                    overlap2[0] = overlapPercentage2;
//                    overlap2[1] = (double) object1.getID();
//                }
//            }
//            writeMessage("Compared "+(count)+" pairs of "+totalPairs);
//        }
//
//        reassignObjects(inputObjects1,inputObjects2,outputObjects,overlaps1,overlaps2);
//
//        return outputObjects;
//
//    }
//
//    private ObjCollection calculateCentroidSeparation(ObjCollection inputObjects1, ObjCollection inputObjects2,
//                                                      String outputObjectsName, double maxSeparation) {
//        ObjCollection outputObjects = new ObjCollection(outputObjectsName);
//
//        // Initialising the storage, which has ID number (key) and maximum overlap[0] and object ID[1] (value)
//        HashMap<Integer,Double[]> overlaps1 = initialiseOverlapStore(inputObjects1,Double.MAX_VALUE);
//        HashMap<Integer,Double[]> overlaps2 = initialiseOverlapStore(inputObjects2,Double.MAX_VALUE);
//
//        // Calculating the separations
//        int totalPairs = inputObjects1.size()*inputObjects2.size();
//        int count = 0;
//        for (Obj object1:inputObjects1.values()) {
//            Double[] overlap1 = overlaps1.get(object1.getID());
//            for (Obj object2:inputObjects2.values()) {
//                Double[] overlap2 = overlaps2.get(object2.getID());
//
//                // Calculating the separation between the two objects
//                double overlap = object1.getCentroidSeparation(object2,true);
//
//                // Comparing the overlap to previously-minimum overlaps
//                if (overlap<overlap1[0] && overlap < maxSeparation) {
//                    overlap1[0] = overlap;
//                    overlap1[1] = (double) object2.getID();
//                }
//
//                // Comparing the overlap to previously-minimum overlaps
//                if (overlap<overlap2[0] && overlap < maxSeparation) {
//                    overlap2[0] = overlap;
//                    overlap2[1] = (double) object1.getID();
//                }
//                count++;
//            }
//            writeMessage("Compared "+(count)+" pairs of "+totalPairs);
//        }
//
//        reassignObjects(inputObjects1,inputObjects2,outputObjects,overlaps1,overlaps2);
//
//        return outputObjects;
//
//    }
//
//    private HashMap<Integer,Double[]> initialiseOverlapStore(ObjCollection inputObjects, double defaultScore) {
//        HashMap<Integer,Double[]> overlaps = new HashMap<>();
//        for (Obj object:inputObjects.values()) {
//            overlaps.put(object.getID(),new Double[]{defaultScore,Double.NaN});
//        }
//
//        return overlaps;
//
//    }
//
//    private void reassignObjects(ObjCollection objects1, ObjCollection objects2, ObjCollection outputObjects,
//                                 HashMap<Integer,Double[]> overlaps1, HashMap<Integer,Double[]> overlaps2) {
//
//        // Creating a HashMap, which stores the correspondences between removed objects and the new objects
//        HashMap<Integer,Obj> newObjects = new HashMap<>();
//
//        Iterator<Obj> iterator = objects1.values().iterator();
//        while (iterator.hasNext()) {
//            Obj object1 = iterator.next();
//            Double[] overlap1 = overlaps1.get(object1.getID());
//
//            // The associated ID will be NaN if no overlap was detected
//            if (Double.isNaN(overlap1[1])) continue;
//            Obj object2 = objects2.get(overlap1[1].intValue());
//            Double[] overlap2 = overlaps2.get(object2.getID());
//
//            // Checking both objects identified the other as the optimal link
//            if (overlap2[1] != object1.getID()) continue;
//
//            // Merge objects and adding to output objects
//                Obj outputObject = new Obj(outputObjects.getName(), outputObjects.getNextID(),
//                        object1.getDistPerPxXY(), object1.getDistPerPxZ(), object1.getCalibratedUnits(), object1.is2D());
//
//                // Adding measurements
//                double nPoints1 = (double) object1.getNVoxels();
//                double nPoints2 = (double) object2.getNVoxels();
//                double nTotalPoints = nPoints1 + nPoints2;
//                double fraction1 = nPoints1/nTotalPoints;
//                double fraction2 = nPoints2/nTotalPoints;
//
//                String name = getFullName(object1.getName(),Measurements.FRACTION_1);
//                outputObject.addMeasurement(new Measurement(name,fraction1));
//                name = getFullName(object1.getName(),Measurements.N_VOXELS1);
//                outputObject.addMeasurement(new Measurement(name,nPoints1));
//                name = getFullName(object1.getName(),Measurements.FRACTION_2);
//                outputObject.addMeasurement(new Measurement(name,fraction2));
//                name = getFullName(object2.getName(),Measurements.N_VOXELS2);
//                outputObject.addMeasurement(new Measurement(name,nPoints2));
//
//                // Assigning points to new object
//                outputObject.getPoints().addAll(object1.getPoints());
//                outputObject.getPoints().addAll(object2.getPoints());
//                outputObjects.add(outputObject);
//
//                // Removing merged objects from input
//                objects2.remove(object2.getID());
//
//                // Adding the new object to the correspondences HashMap along with the object2 that created it
//                newObjects.put(overlap1[1].intValue(), outputObject);
//
//            iterator.remove();
//
//        }
//    }
//
//    @Override
//    public String getTitle() {
//        return "Resolve object overlap";
//    }
//
//    @Override
//    public String getPackageName() {
//        return PackageNames.OBJECT_PROCESSING_REFINEMENT;
//    }
//
//    @Override
//    public String getHelp() {
//        return "Identifies overlapping objects and moves them to a new object collection\n"+
//                "Objects are linked on a one-to-one basis.";
//    }
//
//    @Override
//    protected void run(Workspace workspace) throws GenericMIAException {
//        // Getting input objects
//        String inputObjects1Name = parameters.getValue(PARENT_OBJECTS);
//        ObjCollection inputObjects1 = workspace.getObjects().get(inputObjects1Name);
//
//        String inputObjects2Name = parameters.getValue(INPUT_OBJECTS_2);
//        ObjCollection inputObjects2 = workspace.getObjects().get(inputObjects2Name);
//
//        // Getting parameters
//        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS_NAME);
//        String overlapMode = parameters.getValue(OVERLAP_MODE);
//        double maximumSeparation = parameters.getValue(MAXIMUM_SEPARATION);
//        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS);
//        double minOverlap1 = parameters.getValue(MINIMUM_OVERLAP_PC_1);
//        double minOverlap2 = parameters.getValue(MINIMUM_OVERLAP_PC_2);
//
//        // Skipping the module if no objects are present in one collection
//        if (inputObjects1.size() == 0 || inputObjects2.size() == 0) {
//            workspace.addObjects(new ObjCollection(outputObjectsName));
//            return;
//        }
//
//        Obj firstObj = inputObjects1.getFirst();
//        if (calibratedUnits) maximumSeparation = maximumSeparation/firstObj.getDistPerPxXY();
//
//        ObjCollection outputObjects = null;
//        switch (overlapMode) {
//            case OverlapModes.CENTROID_SEPARATION:
//                outputObjects = calculateCentroidSeparation(inputObjects1, inputObjects2, outputObjectsName, maximumSeparation);
//                break;
//
//            case OverlapModes.SPATIAL_OVERLAP:
//                outputObjects = calculateSpatialOverlap(inputObjects1, inputObjects2, outputObjectsName, minOverlap1, minOverlap2);
//                break;
//        }
//
//        workspace.addObjects(outputObjects);
//
//    }
//
//    @Override
//    protected void initialiseParameters() {
//        parameters.add(new Parameter(PARENT_OBJECTS,this,null));
//        parameters.add(new Parameter(INPUT_OBJECTS_2,this,null));
//        parameters.add(new Parameter(OUTPUT_OBJECTS_NAME,this,null));
//        parameters.add(new Parameter(OVERLAP_MODE,this,OverlapModes.SPATIAL_OVERLAP,OverlapModes.ALL));
//        parameters.add(new Parameter(MAXIMUM_SEPARATION,this,1.0));
//        parameters.add(new Parameter(CALIBRATED_UNITS,this,false));
//        parameters.add(new Parameter(MINIMUM_OVERLAP_PC_1,this,50.0));
//        parameters.add(new Parameter(MINIMUM_OVERLAP_PC_2,this,50.0));
//
//    }
//
//    @Override
//    public ParameterCollection updateAndGetParameters() {
//        ParameterCollection returnedParameters = new ParameterCollection();
//
//        returnedParameters.add(parameters.getParameter(PARENT_OBJECTS));
//        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS_2));
//        returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS_NAME));
//        returnedParameters.add(parameters.getParameter(OVERLAP_MODE));
//
//        switch ((String) parameters.getValue(OVERLAP_MODE)){
//            case OverlapModes.CENTROID_SEPARATION:
//                returnedParameters.add(parameters.getParameter(MAXIMUM_SEPARATION));
//                returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));
//                break;
//            case OverlapModes.SPATIAL_OVERLAP:
//                returnedParameters.add(parameters.getParameter(MINIMUM_OVERLAP_PC_1));
//                returnedParameters.add(parameters.getParameter(MINIMUM_OVERLAP_PC_2));
//                break;
//        }
//
//        return returnedParameters;
//
//    }
//
//    @Override
//    public MeasurementRefCollection updateAndGetImageMeasurementReferences() {
//        return null;
//    }
//
//    @Override
//    public MeasurementRefCollection updateAndGetObjectMeasurementReferences() {
//        String inputObjectsName1 = parameters.getValue(PARENT_OBJECTS);
//        String inputObjectsName2 = parameters.getValue(INPUT_OBJECTS_2);
//        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS_NAME);
//
//        objectMeasurementReferences.setAllCalculated(false);
//
//        String name = getFullName(inputObjectsName1,Measurements.FRACTION_1);
//        MeasurementReference reference = objectMeasurementReferences.getOrPut(name);
//        reference.setImageObjName(outputObjectsName);
//        reference.setCalculated(true);
//        reference.setDescription("Fraction of overlap object which is coincident with \""+inputObjectsName1+"\" objects");
//
//        name = getFullName(inputObjectsName1,Measurements.N_VOXELS1);
//        reference = objectMeasurementReferences.getOrPut(name);
//        reference.setImageObjName(outputObjectsName);
//        reference.setCalculated(true);
//        reference.setDescription("Number of voxels in overlap object which are coincident with \""+inputObjectsName1+"\" objects");
//
//        name = getFullName(inputObjectsName2,Measurements.FRACTION_2);
//        reference = objectMeasurementReferences.getOrPut(name);
//        reference.setImageObjName(outputObjectsName);
//        reference.setCalculated(true);
//        reference.setDescription("Fraction of overlap object which is coincident with \""+inputObjectsName2+"\" objects");
//
//        name = getFullName(inputObjectsName2,Measurements.N_VOXELS2);
//        reference = objectMeasurementReferences.getOrPut(name);
//        reference.setImageObjName(outputObjectsName);
//        reference.setCalculated(true);
//        reference.setDescription("Number of voxels in overlap object which are coincident with \""+inputObjectsName2+"\" objects");
//
//        return objectMeasurementReferences;
//
//    }
//
//    @Override
//    public MetadataRefCollection updateAndGetMetadataReferences() {
//        return null;
//    }
//
//    @Override
//    public void addRelationships(RelationshipCollection relationships) {
//
//    }
//}
