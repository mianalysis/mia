//package wbif.sjx.MIA.Module.ObjectProcessing.Relationships;
//
//import fiji.plugin.trackmate.tracking.oldlap.hungarian.JonkerVolgenantAlgorithm;
//import fiji.plugin.trackmate.tracking.sparselap.linker.LAPJV;
//import fiji.plugin.trackmate.tracking.sparselap.linker.SparseCostMatrix;
//
//import java.lang.reflect.Array;
//import java.util.Arrays;
//import java.util.Random;
//
//public class RelateOneToOne {
//
//    private int seed;
//
//    private int pseudoRandom() {
//        return seed = 3170425 * seed + 132102;
//    }
//
//    private double pseudoRandom( final double min, final double max ) {
//        final int random = pseudoRandom() & 0x7fffffff;
//        return min + random * ( ( max - min ) / Integer.MAX_VALUE );
//    }
//
//    private double[][] generateMatrix( final int n, final int m) {
//        final double[][] ma = new double[ n ][ m ];
//        for ( int j = 0; j < n; j++ )
//        {
//            for ( int i = 0; i < m; i++ )
//            {
//                ma[ j ][ i ] = Math.floor( pseudoRandom( 1, 100 ) );
//            }
//        }
//        return ma;
//    }
//
//    private SparseCostMatrix generateSparseMatrix( final double[][] weights ) {
//        final int n = weights.length;
//        final int m = weights[0].length;
//        final int[] number = new int[ n ];
//        final int[] kk = new int[ n * m ];
//        final double[] cc = new double[ n * m ];
//
//        int index = 0;
//        for ( int i = 0; i < n; i++ )
//        {
//            number[ i ] = m;
//            for ( int j = 0; j < m; j++ )
//            {
//                kk[ index ] = j;
//                cc[ index ] = weights[ i ][ j ];
//                index++;
//            }
//        }
//        return new SparseCostMatrix( cc, kk, number, m );
//    }
//
//    public static void main(String[] args) {
//        new RelateOneToOne().testSparseIsNonSparse();
//
//    }
//
//    public final void testSparseIsNonSparse() {
//        final int n = 4;
//        final int m = 6;
//        seed = new Random().nextInt();
//        final double[][] weights = generateMatrix( n , m );
//
//        System.out.println("Full");
//        System.out.println(Arrays.deepToString(weights).replace("],","]\n"));
//
//        final SparseCostMatrix CM = generateSparseMatrix( weights );
//
//        System.out.println("Sparse");
//        System.out.println(Arrays.deepToString(CM.toFullMatrix()).replace("],","]\n"));
//
//        // Sparse with non-sparse entries
//        System.out.println("Initialising");
//        final LAPJV jvs = new LAPJV(CM);
//        System.out.println("Processing");
//        jvs.process();
//        System.out.println("Processing complete");
//        final int[] jvSparseResult = jvs.getResult();
//        System.out.println(Arrays.toString(jvSparseResult));
//
//        double jvsSparse = 0, jonkerVolgenantCost = 0;
//        for ( int i = 0; i < jvSparseResult.length; i++ ) {
//            jvsSparse += weights[ i ][ jvSparseResult[ i ] ];
//        }
//    }
//}

package wbif.sjx.MIA.Module.ObjectProcessing.Relationships;

import java.util.HashMap;
import java.util.LinkedHashMap;

import ij.ImagePlus;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ObjectMeasurementP;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.ParameterGroup;
import wbif.sjx.MIA.Object.Parameters.Objects.OutputClusterObjectsP;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.References.ObjMeasurementRef;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.MIA.Process.ColourFactory;
import wbif.sjx.common.Object.LUTs;
import wbif.sjx.common.Object.Volume.SpatCal;

public class RelateManyToMany extends Module {
    public static final String INPUT_SEPARATOR = "Objects input/output";
    public static final String OBJECT_SOURCE_MODE = "Object source mode";
    public final static String INPUT_OBJECTS_1 = "Input objects 1";
    public final static String INPUT_OBJECTS_2 = "Input objects 2";
    public static final String CREATE_CLUSTER_OBJECTS = "Create cluster objects";
    public static final String OUTPUT_OBJECTS_NAME = "Output cluster objects";

    public static final String SPATIAL_LINKING_SEPARATOR = "Spatial linking settings";
    public static final String SPATIAL_SEPARATION_MODE = "Spatial separation mode";
    public static final String MAXIMUM_SEPARATION = "Maximum separation";
    public static final String CALIBRATED_UNITS = "Calibrated units";
    public static final String MINIMUM_OVERLAP_PC_1 = "Minimum overlap of object 1 (%)";
    public static final String MINIMUM_OVERLAP_PC_2 = "Minimum overlap of object 2 (%)";

    public static final String ADDITIONAL_MEASUREMENTS_SEPARATOR = "Additional measurement settings";
    public static final String ADD_MEASUREMENT = "Add measurement";
    public static final String MEASUREMENT = "Measurement";
    public static final String CALCULATION = "Calculation";
    public static final String MEASUREMENT_LIMIT = "Measurement limit";

    public static final String MISCELLANEOUS_SEPARATOR = "Miscellaneous settings";
    public static final String LINK_IN_SAME_FRAME = "Only link objects in same frame";

    public static final String OUTPUT_SEPARATOR = "Data output";
    public static final String EXPORT_ALL_DISTANCES = "Export all distances";
    public static final String INCLUDE_TIMEPOINTS = "Include timepoints";
    public static final String INCLUDE_INPUT_PARENT = "Include input object parent";
    public static final String INPUT_PARENT = "Input object parent";
    public static final String INCLUDE_NEIGHBOUR_PARENT = "Include neighbour object parent";
    public static final String NEIGHBOUR_PARENT = "Neighbour object parent";
    public static final String SAVE_NAME_MODE = "Save name mode";
    public static final String SAVE_FILE_NAME = "File name";
    public static final String APPEND_SERIES_MODE = "Append series mode";
    public static final String APPEND_DATETIME_MODE = "Append date/time mode";
    public static final String SAVE_SUFFIX = "Add filename suffix";

    public interface ObjectSourceModes {
        String DIFFERENT_CLASSES = "Different classes";
        String SAME_CLASS = "Same class";

        String[] ALL = new String[] { DIFFERENT_CLASSES, SAME_CLASS };

    }

    public interface SpatialSeparationModes {
        String NONE = "None";
        String CENTROID_SEPARATION = "Centroid separation";
        String SPATIAL_OVERLAP = "Spatial overlap";
        String SURFACE_SEPARATION = "Surface separation";

        String[] ALL = new String[] { NONE, CENTROID_SEPARATION, SPATIAL_OVERLAP, SURFACE_SEPARATION };

    }

    public interface Calculations {
        String DIFFERENCE = "Difference";
        String SUM = "Sum";

        String[] ALL = new String[] { DIFFERENCE, SUM };

    }

    public interface Measurements {
        String WAS_LINKED = "WAS_LINKED";

    }

    public static String getFullName(String objectName, String measurement) {
        return "RELATE_ONE_TO_ONE // " + measurement.substring(0, measurement.length() - 1) + "_" + objectName;

    }

    static boolean testCentroidSeparation(Obj object1, Obj object2, double maxSeparation) {
        // If comparing objects in the same class they will eventually test themselves
        if (object1 == object2)
            return false;

        // Calculating the separation between the two objects
        double overlap = object1.getCentroidSeparation(object2, true);

        // Test if this point is within linking distance
        return overlap <= maxSeparation;

    }

    static boolean testSpatialOverlap(Obj object1, Obj object2, double minOverlap1, double minOverlap2) {
        // If comparing objects in the same class they will eventually test themselves
        if (object1 == object2)
            return false;

        // Calculate the overlap between the two objects
        double overlap = object1.getOverlap(object2);

        // We want large overlaps to be large when they're bad, so taking the inverse
        if (overlap == 0)
            return false;

        double overlapPercentage1 = 100 * overlap / object1.size();
        double overlapPercentage2 = 100 * overlap / object2.size();

        // Checking the minimum overlaps have been met
        return overlapPercentage1 > minOverlap1 && overlapPercentage2 > minOverlap2;

    }

    static boolean testSurfaceSeparation(Obj object1, Obj object2, double maxSeparation) {
        // If comparing objects in the same class they will eventually test themselves
        if (object1 == object2)
            return false;

        // Calculating the separation between the two objects
        double overlap = object1.getSurfaceSeparation(object2, true);

        // Test if this point is within linking distance
        return overlap <= maxSeparation;

    }

    static boolean testGeneric(Obj object1, Obj object2, String measurement, String calculation,
            double measurementLimit) {
        // If comparing objects in the same class they will eventually test themselves
        if (object1 == object2)
            return false;

        // Getting measurements
        if (object1.getMeasurement(measurement) == null)
            return false;
        double measurement1 = object1.getMeasurement(measurement).getValue();
        if (Double.isNaN(measurement1))
            return false;

        if (object2.getMeasurement(measurement) == null)
            return false;
        double measurement2 = object2.getMeasurement(measurement).getValue();
        if (Double.isNaN(measurement2))
            return false;

        // Perform calculation
        double value;
        switch (calculation) {
            default:
                return false;
            case Calculations.DIFFERENCE:
                value = Math.abs(measurement1 - measurement2);
                break;
            case Calculations.SUM:
                value = measurement1 + measurement2;
                break;
        }

        // Testing measurement
        return value < measurementLimit;

    }

    static int updateAssignments(Obj object1, Obj object2, HashMap<Obj, Integer> assignments, int maxGroupID) {
        // Any pairs that got this far can be linked. First, checking if they are
        // already part of a group
        if (assignments.containsKey(object1) & !assignments.containsKey(object2)) {
            // Object1 is present, but object2 isn't. Object 2 inherits assignment from
            // object 1.
            int groupID = assignments.get(object1);
            assignments.put(object2, groupID);

        } else if (!assignments.containsKey(object1) && assignments.containsKey(object2)) {
            // Object2 is present, but object1 isn't. Object 1 inherits assignment from
            // object 2.
            int groupID = assignments.get(object2);
            assignments.put(object1, groupID);

        } else if (assignments.containsKey(object1) && assignments.containsKey(object2)) {
            // Both object 1 and 2 are present. Object 2 inherits assignment from object 1,
            // as do all other
            // objects previous in the same group as object 2.
            int groupID1 = assignments.get(object1);
            int groupID2 = assignments.get(object2);
            for (Obj object : assignments.keySet()) {
                if (assignments.get(object) == groupID2)
                    assignments.put(object, groupID1);
            }

        } else {
            // Neither object 1 nor object 2 are present. Both are assigned the next
            // available ID.
            int groupID = ++maxGroupID;
            assignments.put(object1, groupID);
            assignments.put(object2, groupID);

        }

        // Adding partnership between the two objects
        object1.addPartner(object2);
        object2.addPartner(object1);

        return maxGroupID;

    }

    static ObjCollection createClusters(String outputObjectsName, HashMap<Obj, Integer> assignments,
            SpatCal calibration, int nFrames) {
        ObjCollection outputObjects = new ObjCollection(outputObjectsName, calibration, nFrames);
        for (Obj object : assignments.keySet()) {
            int groupID = assignments.get(object);

            // Getting cluster object
            if (!outputObjects.containsKey(groupID)) {
                Obj outputObject = new Obj(outputObjectsName, groupID, object);
                outputObject.setT(object.getT());
                outputObjects.put(groupID, outputObject);
            }
            Obj outputObject = outputObjects.get(groupID);

            // Adding relationships
            outputObject.addChild(object);
            object.addParent(outputObject);

        }

        return outputObjects;

    }

    static void applyMeasurements(ObjCollection objCollection, HashMap<Obj, Integer> assignments,
            String linkedObjectName) {
        for (Obj object : objCollection.values()) {
            if (assignments.containsKey(object)) {
                object.addMeasurement(new Measurement(getFullName(linkedObjectName, Measurements.WAS_LINKED), 1));
            } else {
                object.addMeasurement(new Measurement(getFullName(linkedObjectName, Measurements.WAS_LINKED), 0));
            }
        }
    }

    static void createUnlinkedClusters() {

    }

    public RelateManyToMany(ModuleCollection modules) {
        super("Relate many-to-many", modules);
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_RELATIONSHIPS;
    }

    @Override
    protected Status process(Workspace workspace) {
        String objectSourceMode = parameters.getValue(OBJECT_SOURCE_MODE);

        // Getting input objects
        String inputObjects1Name = parameters.getValue(INPUT_OBJECTS_1);
        ObjCollection inputObjects1 = workspace.getObjects().get(inputObjects1Name);

        String inputObjects2Name = parameters.getValue(INPUT_OBJECTS_2);
        ObjCollection inputObjects2;
        switch (objectSourceMode) {
            default:
                MIA.log.writeError("Unknown object source mode");
                return Status.FAIL;
            case ObjectSourceModes.DIFFERENT_CLASSES:
                inputObjects2 = workspace.getObjects().get(inputObjects2Name);
                inputObjects1.removePartners(inputObjects2Name);
                inputObjects2.removePartners(inputObjects1Name);
                break;
            case ObjectSourceModes.SAME_CLASS:
                inputObjects2 = inputObjects1;
                inputObjects1.removePartners(inputObjects1Name);
                break;
        }

        // Getting parameters
        boolean createClusterObjects = parameters.getValue(CREATE_CLUSTER_OBJECTS);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS_NAME);
        String spatialSeparationMode = parameters.getValue(SPATIAL_SEPARATION_MODE);
        double maximumSeparation = parameters.getValue(MAXIMUM_SEPARATION);
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS);
        double minOverlap1 = parameters.getValue(MINIMUM_OVERLAP_PC_1);
        double minOverlap2 = parameters.getValue(MINIMUM_OVERLAP_PC_2);
        ParameterGroup parameterGroup = parameters.getParameter(ADD_MEASUREMENT);
        LinkedHashMap<Integer,ParameterCollection> parameterCollections = parameterGroup.getCollections(false);
        boolean linkInSameFrame = parameters.getValue(LINK_IN_SAME_FRAME);

        // Skipping the module if no objects are present in one collection
        if (inputObjects1.size() == 0 || inputObjects2.size() == 0) {
            workspace.addObjects(new ObjCollection(outputObjectsName, inputObjects1.getSpatialCalibration(),
                    inputObjects1.getNFrames()));
            return Status.PASS;
        }

        Obj firstObj = inputObjects1.getFirst();
        if (calibratedUnits)
            maximumSeparation = maximumSeparation / firstObj.getDppXY();

        // Creating a HashMap to store the group ID that each object was assigned to
        HashMap<Obj, Integer> assignments = new HashMap<>();
        int maxGroupID = 0;

        // Iterating over all object pairs
        for (Obj object1 : inputObjects1.values()) {
            for (Obj object2 : inputObjects2.values()) {
                if (linkInSameFrame && object1.getT() != object2.getT())
                    continue;

                boolean linkable = true;

                // Testing spatial separation
                switch (spatialSeparationMode) {
                    case SpatialSeparationModes.CENTROID_SEPARATION:
                        if (!testCentroidSeparation(object1, object2, maximumSeparation))
                            linkable = false;
                        break;
                    case SpatialSeparationModes.SPATIAL_OVERLAP:
                        if (!testSpatialOverlap(object1, object2, minOverlap1, minOverlap2))
                            linkable = false;
                        break;
                    case SpatialSeparationModes.SURFACE_SEPARATION:
                        if (!testSurfaceSeparation(object1, object2, maximumSeparation))
                            linkable = false;
                        break;
                }

                // Testing additional measurements
                for (ParameterCollection collection : parameterCollections.values()) {
                    String measurement = collection.getValue(MEASUREMENT);
                    String calculation = collection.getValue(CALCULATION);
                    double measurementLimit = collection.getValue(MEASUREMENT_LIMIT);
                    if (!testGeneric(object1, object2, measurement, calculation, measurementLimit))
                        linkable = false;
                }

                if (linkable) {
                    // Assigning the same groupID to these two objects
                    maxGroupID = updateAssignments(object1, object2, assignments, maxGroupID);
                } else {
                    // If these objects haven't been previously assigned a group, giving them the
                    // next available IDs
                    assignments.putIfAbsent(object1, ++maxGroupID);
                    assignments.putIfAbsent(object2, ++maxGroupID);
                }
            }
        }

        applyMeasurements(inputObjects1, assignments, inputObjects2Name);
        applyMeasurements(inputObjects2, assignments, inputObjects1Name);

        if (createClusterObjects) {
            // Remove any previously-assigned relationships (from previous runs)
            if (workspace.getObjectSet(outputObjectsName) != null) {
                inputObjects1.removeParents(outputObjectsName);
                inputObjects2.removeParents(outputObjectsName);
                workspace.removeObjects(outputObjectsName, false);
            }

            ObjCollection outputObjects = createClusters(outputObjectsName, assignments,
                    inputObjects1.getSpatialCalibration(), inputObjects1.getNFrames());
            workspace.addObjects(outputObjects);

            if (showOutput) {
                // Generating colours
                HashMap<Integer, Float> hues = ColourFactory.getParentIDHues(inputObjects1, outputObjectsName, true);
                ImagePlus dispIpl = inputObjects1.convertToImage(outputObjectsName, hues, 8, true).getImagePlus();
                dispIpl.setLut(LUTs.Random(true));
                dispIpl.setPosition(1, 1, 1);
                dispIpl.updateChannelAndDraw();
                dispIpl.show();

                if (objectSourceMode.equals(ObjectSourceModes.DIFFERENT_CLASSES)) {
                    // Generating colours
                    hues = ColourFactory.getParentIDHues(inputObjects2, outputObjectsName, true);
                    dispIpl = inputObjects2.convertToImage(outputObjectsName, hues, 8, true).getImagePlus();
                    dispIpl.setLut(LUTs.Random(true));
                    dispIpl.setPosition(1, 1, 1);
                    dispIpl.updateChannelAndDraw();
                    dispIpl.show();
                }
            }
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters
                .add(new ChoiceP(OBJECT_SOURCE_MODE, this, ObjectSourceModes.DIFFERENT_CLASSES, ObjectSourceModes.ALL));
        parameters.add(new InputObjectsP(INPUT_OBJECTS_1, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS_2, this));
        parameters.add(new BooleanP(CREATE_CLUSTER_OBJECTS, this, true));
        parameters.add(new OutputClusterObjectsP(OUTPUT_OBJECTS_NAME, this));

        parameters.add(new SeparatorP(SPATIAL_LINKING_SEPARATOR, this));
        parameters.add(new ChoiceP(SPATIAL_SEPARATION_MODE, this, SpatialSeparationModes.SPATIAL_OVERLAP,
                SpatialSeparationModes.ALL));
        parameters.add(new DoubleP(MAXIMUM_SEPARATION, this, 1.0));
        parameters.add(new BooleanP(CALIBRATED_UNITS, this, false));
        parameters.add(new DoubleP(MINIMUM_OVERLAP_PC_1, this, 50.0));
        parameters.add(new DoubleP(MINIMUM_OVERLAP_PC_2, this, 50.0));

        parameters.add(new SeparatorP(ADDITIONAL_MEASUREMENTS_SEPARATOR, this));
        ParameterCollection collection = new ParameterCollection();
        collection.add(new ObjectMeasurementP(MEASUREMENT, this));
        collection.add(new ChoiceP(CALCULATION, this, Calculations.DIFFERENCE, Calculations.ALL));
        collection.add(new DoubleP(MEASUREMENT_LIMIT, this, 1));
        parameters.add(new ParameterGroup(ADD_MEASUREMENT, this, collection, 0));

        parameters.add(new SeparatorP(MISCELLANEOUS_SEPARATOR, this));
        parameters.add(new BooleanP(LINK_IN_SAME_FRAME, this, true));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(OBJECT_SOURCE_MODE));
        switch ((String) parameters.getValue(OBJECT_SOURCE_MODE)) {
            case ObjectSourceModes.DIFFERENT_CLASSES:
                returnedParameters.add(parameters.getParameter(INPUT_OBJECTS_1));
                returnedParameters.add(parameters.getParameter(INPUT_OBJECTS_2));
                break;
            case ObjectSourceModes.SAME_CLASS:
                returnedParameters.add(parameters.getParameter(INPUT_OBJECTS_1));
                break;
        }

        returnedParameters.add(parameters.getParameter(CREATE_CLUSTER_OBJECTS));
        if ((boolean) parameters.getValue(CREATE_CLUSTER_OBJECTS)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS_NAME));
        }

        returnedParameters.add(parameters.getParameter(SPATIAL_LINKING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SPATIAL_SEPARATION_MODE));
        switch ((String) parameters.getValue(SPATIAL_SEPARATION_MODE)) {
            case SpatialSeparationModes.CENTROID_SEPARATION:
            case SpatialSeparationModes.SURFACE_SEPARATION:
                returnedParameters.add(parameters.getParameter(MAXIMUM_SEPARATION));
                returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));
                break;
            case SpatialSeparationModes.SPATIAL_OVERLAP:
                returnedParameters.add(parameters.getParameter(MINIMUM_OVERLAP_PC_1));
                returnedParameters.add(parameters.getParameter(MINIMUM_OVERLAP_PC_2));
                break;
        }

        returnedParameters.add(parameters.getParameter(ADDITIONAL_MEASUREMENTS_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ADD_MEASUREMENT));

        // Updating measurement sources
        String objectName = parameters.getValue(INPUT_OBJECTS_1);
        ParameterGroup parameterGroup = parameters.getParameter(ADD_MEASUREMENT);
        for (ParameterCollection collection : parameterGroup.getCollections(false).values()) {
            ((ObjectMeasurementP) collection.getParameter(MEASUREMENT)).setObjectName(objectName);
        }

        returnedParameters.add(parameters.getParameter(MISCELLANEOUS_SEPARATOR));
        returnedParameters.add(parameters.getParameter(LINK_IN_SAME_FRAME));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        String inputObjectsName1 = parameters.getValue(INPUT_OBJECTS_1);
        String inputObjectsName2 = parameters.getValue(INPUT_OBJECTS_2);

        ObjMeasurementRefCollection returnedRefs = new ObjMeasurementRefCollection();

        String name = getFullName(inputObjectsName2, Measurements.WAS_LINKED);
        ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputObjectsName1);
        returnedRefs.add(reference);
        reference.setDescription("Was this \"" + inputObjectsName1 + "\" object linked with a \"" + inputObjectsName2
                + "\" object.  Linked objects have a value of \"1\" and unlinked objects have a value of \"0\".");

        return returnedRefs;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        ParentChildRefCollection returnedRefs = new ParentChildRefCollection();

        // Getting input objects
        String inputObjects1Name = parameters.getValue(INPUT_OBJECTS_1);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS_NAME);

        returnedRefs.add(parentChildRefs.getOrPut(outputObjectsName, inputObjects1Name));

        String objectSourceMode = parameters.getValue(OBJECT_SOURCE_MODE);
        if (objectSourceMode.equals(ObjectSourceModes.DIFFERENT_CLASSES)) {
            String inputObjects2Name = parameters.getValue(INPUT_OBJECTS_2);
            returnedRefs.add(parentChildRefs.getOrPut(outputObjectsName, inputObjects2Name));
        }

        return returnedRefs;

    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        PartnerRefCollection returnedRefs = new PartnerRefCollection();

        String inputObjects1Name = parameters.getValue(INPUT_OBJECTS_1);
        String inputObjects2Name = parameters.getValue(INPUT_OBJECTS_2);

        switch ((String) parameters.getValue(OBJECT_SOURCE_MODE)) {
            case ObjectSourceModes.DIFFERENT_CLASSES:
                returnedRefs.add(partnerRefs.getOrPut(inputObjects1Name, inputObjects2Name));
                break;
            case ObjectSourceModes.SAME_CLASS:
                returnedRefs.add(partnerRefs.getOrPut(inputObjects1Name, inputObjects1Name));
                break;
        }

        return returnedRefs;

    }

    @Override
    public boolean verify() {
        return true;
    }

    @Override
    public String getDescription() {
        return "";
    }
}
