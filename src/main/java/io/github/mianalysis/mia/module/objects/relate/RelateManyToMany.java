//package io.github.mianalysis.MIA.Module.ObjectProcessing.Relationships;
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

package io.github.mianalysis.mia.module.objects.relate;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.ParameterGroup.ParameterUpdaterAndGetter;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputClusterObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.ColourFactory;
import io.github.sjcross.sjcommon.imagej.LUTs;
import io.github.sjcross.sjcommon.object.volume.VolumeType;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
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
    public static final String ACCEPT_ALL_INSIDE = "Accept all fully enclosed objects";
    public static final String THRESHOLD_MODE = "Threshold mode";
    public static final String MINIMUM_OVERLAP_PC_1 = "Minimum overlap of object 1 (%)";
    public static final String MINIMUM_OVERLAP_PC_2 = "Minimum overlap of object 2 (%)";
    public static final String HIGHER_OVERLAP_PC = "Higher overlap threshold (%)";
    public static final String LOWER_OVERLAP_PC = "Lower overlap threshold (%)";

    public static final String ADDITIONAL_MEASUREMENTS_SEPARATOR = "Additional measurement settings";
    public static final String ADD_MEASUREMENT = "Add measurement";
    public static final String MEASUREMENT_1 = "Measurement 1";
    public static final String MEASUREMENT_2 = "Measurement 2";
    public static final String CALCULATION = "Calculation";
    public static final String MEASUREMENT_LIMIT = "Measurement limit";

    public static final String MISCELLANEOUS_SEPARATOR = "Miscellaneous settings";
    public static final String LINK_IN_SAME_FRAME = "Only link objects in same frame";

    public interface ObjectSourceModes {
        String DIFFERENT_CLASSES = "Different classes";
        String SAME_CLASS = "Same class";

        String[] ALL = new String[] { DIFFERENT_CLASSES, SAME_CLASS };

    }

    public interface SpatialSeparationModes {
        String CENTROID_SEPARATION = "Centroid separation";
        String SPATIAL_OVERLAP = "Spatial overlap";
        String SURFACE_SEPARATION = "Surface separation";

        String[] ALL = new String[] { CENTROID_SEPARATION, SPATIAL_OVERLAP, SURFACE_SEPARATION };

    }

    public interface ThresholdModes {
        String FIXED_THRESHOLDS = "Fixed thresholds";
        String FLEXIBLE_THRESHOLDS = "Flexible thresholds";

        String[] ALL = new String[] { FIXED_THRESHOLDS, FLEXIBLE_THRESHOLDS };

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
        return "RELATE_ONE_TO_ONE // " + measurement.substring(0, measurement.length()) + "_" + objectName;

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

    static boolean testSpatialOverlap(Obj object1, Obj object2, String thresholdMode, double minOverlap1,
            double minOverlap2) {
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
        switch (thresholdMode) {
            case ThresholdModes.FIXED_THRESHOLDS:
            default:
                return overlapPercentage1 >= minOverlap1 && overlapPercentage2 >= minOverlap2;
            case ThresholdModes.FLEXIBLE_THRESHOLDS:
                return Math.max(overlapPercentage1, overlapPercentage2) >= minOverlap1
                        && Math.min(overlapPercentage1, overlapPercentage2) >= minOverlap2;
        }
    }

    static boolean testSurfaceSeparation(Obj object1, Obj object2, double maxSeparation, boolean acceptAllInside) {
        // If comparing objects in the same class they will eventually test themselves
        if (object1 == object2)
            return false;

        // Calculating the separation between the two objects
        double overlap = object1.getSurfaceSeparation(object2, true);

        // If accepting all inside, any negative distances are automatically accepted
        if (acceptAllInside & overlap < 0)
            return true;

        // If not accepting all inside, test against the magnitude of the distance
        if (!acceptAllInside)
            overlap = Math.abs(overlap);

        // Test if this point is within linking distance
        return overlap <= maxSeparation;

    }

    static boolean testGeneric(Obj object1, Obj object2, String measurement1, String measurement2, String calculation,
            double measurementLimit) {
        // If comparing objects in the same class they will eventually test themselves
        if (object1 == object2)
            return false;

        // Getting measurements
        if (object1.getMeasurement(measurement1) == null)
            return false;
        double measurementValue1 = object1.getMeasurement(measurement1).getValue();
        if (Double.isNaN(measurementValue1))
            return false;

        if (object2.getMeasurement(measurement2) == null)
            return false;
        double measurementValue2 = object2.getMeasurement(measurement2).getValue();
        if (Double.isNaN(measurementValue2))
            return false;

        // Perform calculation
        double value;
        switch (calculation) {
            default:
                return false;
            case Calculations.DIFFERENCE:
                value = Math.abs(measurementValue1 - measurementValue2);
                break;
            case Calculations.SUM:
                value = measurementValue1 + measurementValue2;
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

    static Objs createClusters(Objs outputObjects, HashMap<Obj, Integer> assignments) {
        for (Obj object : assignments.keySet()) {
            int groupID = assignments.get(object);

            // Getting cluster object
            if (!outputObjects.containsKey(groupID)) {
                Obj outputObject = outputObjects.createAndAddNewObject(VolumeType.POINTLIST, groupID);
                outputObject.setT(object.getT());
            }
            Obj outputObject = outputObjects.get(groupID);

            // Adding relationships
            outputObject.addChild(object);
            object.addParent(outputObject);

        }

        return outputObjects;

    }

    static void applyMeasurements(Objs objCollection, HashMap<Obj, Integer> assignments,
            String linkedObjectName) {
        for (Obj object : objCollection.values())
            if (object.getPartners(linkedObjectName) != null && object.getPartners(linkedObjectName).size() > 0)
                object.addMeasurement(new Measurement(getFullName(linkedObjectName, Measurements.WAS_LINKED), 1));
            else
                object.addMeasurement(new Measurement(getFullName(linkedObjectName, Measurements.WAS_LINKED), 0));
    }

    public RelateManyToMany(Modules modules) {
        super("Relate many-to-many", modules);
    }

    @Override
    public String getDescription() {
        return "Relate objects of two classes based on spatial proximity or overlap.  With this module, each object from a collection can be linked to an unlimited number of other objects (see \""
                + new RelateManyToOne(null).getName() + "\" and \"" + new RelateOneToOne(null).getName()
                + "\" modules for alternatives).  As such, the assigned relationships can form a network of relationships, with each object connected to multiple others.  Related objects are assigned partner relationships and can optionally also be related by a common cluster (parent) object.  Measurements associated with these relationship (e.g. a record of whether each object was linked) are stored as measurements of the relevant object.";
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_RELATE;
    }

    @Override
    protected Status process(Workspace workspace) {
        String objectSourceMode = parameters.getValue(OBJECT_SOURCE_MODE, workspace);

        // Getting input objects
        String inputObjects1Name = parameters.getValue(INPUT_OBJECTS_1, workspace);
        Objs inputObjects1 = workspace.getObjects().get(inputObjects1Name);

        String inputObjects2Name = parameters.getValue(INPUT_OBJECTS_2, workspace);
        Objs inputObjects2;

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
        boolean createClusterObjects = parameters.getValue(CREATE_CLUSTER_OBJECTS, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS_NAME, workspace);
        String spatialSeparationMode = parameters.getValue(SPATIAL_SEPARATION_MODE, workspace);
        double maximumSeparation = parameters.getValue(MAXIMUM_SEPARATION, workspace);
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS, workspace);
        boolean acceptAllInside = parameters.getValue(ACCEPT_ALL_INSIDE, workspace);
        String thresholdMode = parameters.getValue(THRESHOLD_MODE, workspace);
        double minOverlap1 = parameters.getValue(MINIMUM_OVERLAP_PC_1, workspace);
        double minOverlap2 = parameters.getValue(MINIMUM_OVERLAP_PC_2, workspace);
        double higherThresh = parameters.getValue(HIGHER_OVERLAP_PC, workspace);
        double lowerThresh = parameters.getValue(LOWER_OVERLAP_PC, workspace);
        ParameterGroup parameterGroup = parameters.getParameter(ADD_MEASUREMENT);
        LinkedHashMap<Integer, Parameters> parameterCollections = parameterGroup.getCollections(false);
        boolean linkInSameFrame = parameters.getValue(LINK_IN_SAME_FRAME, workspace);

        // Skipping the module if no objects are present in one collection
        if (inputObjects1.size() == 0 || inputObjects2.size() == 0) {
            workspace.addObjects(new Objs(outputObjectsName, inputObjects1));
            return Status.PASS;
        }

        if (spatialSeparationMode.equals(SpatialSeparationModes.SPATIAL_OVERLAP)
                && thresholdMode.equals(ThresholdModes.FLEXIBLE_THRESHOLDS)) {
            minOverlap1 = higherThresh;
            minOverlap2 = lowerThresh;
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
                        if (!testSpatialOverlap(object1, object2, thresholdMode, minOverlap1, minOverlap2))
                            linkable = false;
                        break;
                    case SpatialSeparationModes.SURFACE_SEPARATION:
                        if (!testSurfaceSeparation(object1, object2, maximumSeparation, acceptAllInside))
                            linkable = false;
                        break;
                }

                // Testing additional measurements
                for (Parameters collection : parameterCollections.values()) {
                    String measurement1 = collection.getValue(MEASUREMENT_1, workspace);
                    String measurement2 = objectSourceMode.equals(ObjectSourceModes.SAME_CLASS) ? measurement1
                            : collection.getValue(MEASUREMENT_2, workspace);

                    String calculation = collection.getValue(CALCULATION, workspace);
                    double measurementLimit = collection.getValue(MEASUREMENT_LIMIT, workspace);
                    if (!testGeneric(object1, object2, measurement1, measurement2, calculation, measurementLimit))
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

            Objs outputObjects = new Objs(outputObjectsName, inputObjects1);
            createClusters(outputObjects, assignments);
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

        if (showOutput) {
            inputObjects1.showMeasurements(this, modules);
            inputObjects2.showMeasurements(this, modules);
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
        parameters.add(new BooleanP(ACCEPT_ALL_INSIDE, this, true));
        parameters.add(new ChoiceP(THRESHOLD_MODE, this, ThresholdModes.FIXED_THRESHOLDS, ThresholdModes.ALL));
        parameters.add(new DoubleP(MINIMUM_OVERLAP_PC_1, this, 50.0));
        parameters.add(new DoubleP(MINIMUM_OVERLAP_PC_2, this, 50.0));
        parameters.add(new DoubleP(HIGHER_OVERLAP_PC, this, 50.0));
        parameters.add(new DoubleP(LOWER_OVERLAP_PC, this, 0.0));

        parameters.add(new SeparatorP(ADDITIONAL_MEASUREMENTS_SEPARATOR, this));

        Parameters collection = new Parameters();
        collection.add(new ObjectMeasurementP(MEASUREMENT_1, this));
        collection.add(new ObjectMeasurementP(MEASUREMENT_2, this));
        collection.add(new ChoiceP(CALCULATION, this, Calculations.DIFFERENCE, Calculations.ALL));
        collection.add(new DoubleP(MEASUREMENT_LIMIT, this, 1));
        parameters.add(new ParameterGroup(ADD_MEASUREMENT, this, collection, 0, getUpdaterAndGetter()));

        parameters.add(new SeparatorP(MISCELLANEOUS_SEPARATOR, this));
        parameters.add(new BooleanP(LINK_IN_SAME_FRAME, this, true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(OBJECT_SOURCE_MODE));
        switch ((String) parameters.getValue(OBJECT_SOURCE_MODE, workspace)) {
            case ObjectSourceModes.DIFFERENT_CLASSES:
                returnedParameters.add(parameters.getParameter(INPUT_OBJECTS_1));
                returnedParameters.add(parameters.getParameter(INPUT_OBJECTS_2));
                break;
            case ObjectSourceModes.SAME_CLASS:
                returnedParameters.add(parameters.getParameter(INPUT_OBJECTS_1));
                break;
        }

        returnedParameters.add(parameters.getParameter(CREATE_CLUSTER_OBJECTS));
        if ((boolean) parameters.getValue(CREATE_CLUSTER_OBJECTS, workspace)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS_NAME));
        }

        returnedParameters.add(parameters.getParameter(SPATIAL_LINKING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SPATIAL_SEPARATION_MODE));
        switch ((String) parameters.getValue(SPATIAL_SEPARATION_MODE, workspace)) {
            case SpatialSeparationModes.CENTROID_SEPARATION:
            case SpatialSeparationModes.SURFACE_SEPARATION:
                returnedParameters.add(parameters.getParameter(MAXIMUM_SEPARATION));
                returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));
                returnedParameters.add(parameters.getParameter(ACCEPT_ALL_INSIDE));
                break;
            case SpatialSeparationModes.SPATIAL_OVERLAP:
                returnedParameters.add(parameters.getParameter(THRESHOLD_MODE));
                switch ((String) parameters.getValue(THRESHOLD_MODE, workspace)) {
                    case ThresholdModes.FIXED_THRESHOLDS:
                        returnedParameters.add(parameters.getParameter(MINIMUM_OVERLAP_PC_1));
                        returnedParameters.add(parameters.getParameter(MINIMUM_OVERLAP_PC_2));
                        break;
                    case ThresholdModes.FLEXIBLE_THRESHOLDS:
                        returnedParameters.add(parameters.getParameter(HIGHER_OVERLAP_PC));
                        returnedParameters.add(parameters.getParameter(LOWER_OVERLAP_PC));
                        break;
                }
                break;
        }

        returnedParameters.add(parameters.getParameter(ADDITIONAL_MEASUREMENTS_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ADD_MEASUREMENT));

        returnedParameters.add(parameters.getParameter(MISCELLANEOUS_SEPARATOR));
        returnedParameters.add(parameters.getParameter(LINK_IN_SAME_FRAME));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        Workspace workspace = null;
        String inputObjectsName1 = parameters.getValue(INPUT_OBJECTS_1, workspace);
        String inputObjectsName2 = parameters.getValue(INPUT_OBJECTS_2, workspace);

        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        String name = getFullName(inputObjectsName2, Measurements.WAS_LINKED);
        ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputObjectsName1);
        returnedRefs.add(reference);
        reference.setDescription("Was this \"" + inputObjectsName1 + "\" object linked with a \"" + inputObjectsName2
                + "\" object.  Linked objects have a value of \"1\" and unlinked objects have a value of \"0\".");

        name = getFullName(inputObjectsName1, Measurements.WAS_LINKED);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputObjectsName2);
        returnedRefs.add(reference);
        reference.setDescription("Was this \"" + inputObjectsName2 + "\" object linked with a \"" + inputObjectsName1
                + "\" object.  Linked objects have a value of \"1\" and unlinked objects have a value of \"0\".");

        return returnedRefs;

    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        Workspace workspace = null;
        ParentChildRefs returnedRefs = new ParentChildRefs();

        // Getting input objects
        String inputObjects1Name = parameters.getValue(INPUT_OBJECTS_1, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS_NAME, workspace);

        returnedRefs.add(parentChildRefs.getOrPut(outputObjectsName, inputObjects1Name));

        String objectSourceMode = parameters.getValue(OBJECT_SOURCE_MODE, workspace);
        if (objectSourceMode.equals(ObjectSourceModes.DIFFERENT_CLASSES)) {
            String inputObjects2Name = parameters.getValue(INPUT_OBJECTS_2, workspace);
            returnedRefs.add(parentChildRefs.getOrPut(outputObjectsName, inputObjects2Name));
        }

        return returnedRefs;

    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        Workspace workspace = null;
        PartnerRefs returnedRefs = new PartnerRefs();

        String inputObjects1Name = parameters.getValue(INPUT_OBJECTS_1, workspace);
        String inputObjects2Name = parameters.getValue(INPUT_OBJECTS_2, workspace);

        switch ((String) parameters.getValue(OBJECT_SOURCE_MODE, workspace)) {
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

    void addParameterDescriptions() {
        parameters.get(OBJECT_SOURCE_MODE).setDescription(
                "Controls whether the objects from the same class should be related to each other, or whether objects from two different classes should be related.");

        parameters.get(INPUT_OBJECTS_1)
                .setDescription("First objection collection from the workspace to relate objects for.  If \""
                        + OBJECT_SOURCE_MODE + "\" is set to \"" + ObjectSourceModes.DIFFERENT_CLASSES
                        + "\", these objects will be related to the objects from the collection specified by \""
                        + INPUT_OBJECTS_2 + "\"; however, if set to \"" + ObjectSourceModes.SAME_CLASS
                        + "\", the objects from this collection will be related to each other.  Related objects will be given partner relationships.");

        parameters.get(INPUT_OBJECTS_2).setDescription(
                "Second object collection from the workspace to relate objects for.  This object collection will only be used if \""
                        + OBJECT_SOURCE_MODE + "\" is set to \"" + ObjectSourceModes.DIFFERENT_CLASSES
                        + "\", in which case these objects will be related to those from the collection specified by \""
                        + INPUT_OBJECTS_1 + "\".  Related objects will be given partner relationships.");

        parameters.get(CREATE_CLUSTER_OBJECTS).setDescription(
                "When selected, new \"cluster\" objects will be created and added to the workspace.  These objects contain no spatial information, but act as links between all objects that were related.  All objects identified as relating to each other are stored as children of the same cluster object.");

        parameters.get(OUTPUT_OBJECTS_NAME)
                .setDescription("If storing cluster objects (when \"" + CREATE_CLUSTER_OBJECTS
                        + "\" is selected), the output cluster objects will be added to the workspace with this name.");

        parameters.get(SPATIAL_SEPARATION_MODE).setDescription(
                "Controls the type of calculation used when determining which objects are related:<br><ul>"

                        + "<li>\"" + SpatialSeparationModes.CENTROID_SEPARATION
                        + "\" Distances are calculated from object centroid to object centroid.  These distances are always positive; increasing as the distance between centroids increases.</li>"

                        + "<li>\"" + SpatialSeparationModes.SPATIAL_OVERLAP
                        + "\" The percentage of each object, which overlaps with another object is calculated.</li>"

                        + "<li>\"" + SpatialSeparationModes.SURFACE_SEPARATION
                        + "\" Distances are calculated between the closest points on the object surfaces.  These distances increase in magnitude the greater the minimum object-object surface distance is; however, they are assigned a negative value if the one of the closest surface points is inside the other object (this should only occur if one object is entirely enclosed by the other) or a positive value otherwise (i.e. if the objects are separate).  Note: Any instances where the object surfaces overlap will be recorded as \"0px\" distance.</li></ul>");

        parameters.get(MAXIMUM_SEPARATION).setDescription("If \"" + SPATIAL_SEPARATION_MODE + "\" is set to \""
                + SpatialSeparationModes.CENTROID_SEPARATION + "\" or \"" + SpatialSeparationModes.SURFACE_SEPARATION
                + "\", this is the maximum separation two objects can have and still be related.");

        parameters.get(CALIBRATED_UNITS).setDescription(
                "When selected, spatial values are assumed to be specified in calibrated units (as defined by the \""
                        + new InputControl(null).getName() + "\" parameter \"" + InputControl.SPATIAL_UNIT
                        + "\").  Otherwise, pixel units are assumed.");

        parameters.get(ACCEPT_ALL_INSIDE).setDescription("When selected and \"" + SPATIAL_SEPARATION_MODE
                + "\" is set to \"" + SpatialSeparationModes.SURFACE_SEPARATION
                + "\", any instances of objects fully enclosed within another are accepted as being related.  Otherwise, the absolute distance between object surfaces will be used.");

        parameters.get(MINIMUM_OVERLAP_PC_1).setDescription("If \"" + SPATIAL_SEPARATION_MODE + "\" is set to \""
                + SpatialSeparationModes.SPATIAL_OVERLAP
                + "\", this is the minimum percentage overlap the first object must have with the other object for the two objects to be related.");

        parameters.get(MINIMUM_OVERLAP_PC_2).setDescription("If \"" + SPATIAL_SEPARATION_MODE + "\" is set to \""
                + SpatialSeparationModes.SPATIAL_OVERLAP
                + "\", this is the minimum percentage overlap the second object must have with the other object for the two objects to be related.");

        parameters.get(ADD_MEASUREMENT).setDescription(
                "Add additional measurement criteria the two objects must satisfy in order to be related.");

        ParameterGroup group = (ParameterGroup) parameters.get(ADD_MEASUREMENT);
        Parameters collection = group.getTemplateParameters();

        collection.get(MEASUREMENT_1).setDescription(
                "Measurement associated with objects from the first collection that will be used for this test.");

        collection.get(MEASUREMENT_2).setDescription(
                "Measurement associated with objects from the second collection that will be used for this test.");

        collection.get(CALCULATION).setDescription(
                "Controls the calculation used to compare the measurements values for the two objects being tested.  The two measurements can either be summed together or the difference between them taken.");

        collection.get(MEASUREMENT_LIMIT).setDescription("The combined measurement (summed or difference, based on \""
                + CALCULATION + "\" parameter) must be smaller than this value for the two objects to be linked.");

        parameters.get(LINK_IN_SAME_FRAME).setDescription(
                "When selected, child and parent objects must be in the same time frame for them to be linked.");

    }

    private ParameterUpdaterAndGetter getUpdaterAndGetter() {
        return new ParameterUpdaterAndGetter() {

            @Override
            public Parameters updateAndGet(Parameters params) {
                Parameters returnedParameters = new Parameters();

                String objectName1 = parameters.getValue(INPUT_OBJECTS_1, null);
                String objectName2 = parameters.getValue(INPUT_OBJECTS_2, null);

                returnedParameters.add(params.getParameter(MEASUREMENT_1));
                ((ObjectMeasurementP) params.getParameter(MEASUREMENT_1)).setObjectName(objectName1);

                switch ((String) parameters.getValue(OBJECT_SOURCE_MODE, null)) {
                    case ObjectSourceModes.DIFFERENT_CLASSES:
                        returnedParameters.add(params.getParameter(MEASUREMENT_2));
                        ((ObjectMeasurementP) params.getParameter(MEASUREMENT_2)).setObjectName(objectName2);
                        break;
                }

                returnedParameters.add(params.getParameter(CALCULATION));
                returnedParameters.add(params.getParameter(MEASUREMENT_LIMIT));

                return returnedParameters;

            }
        };
    }
}
