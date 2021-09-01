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

package io.github.mianalysis.MIA.Module.ObjectProcessing.Relationships;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.sisu.Nullable;

import fiji.plugin.trackmate.tracking.sparselap.costmatrix.DefaultCostMatrixCreator;
import fiji.plugin.trackmate.tracking.sparselap.linker.JaqamanLinker;
import io.github.mianalysis.MIA.Module.Categories;
import io.github.mianalysis.MIA.Module.Category;
import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Module.ModuleCollection;
import io.github.mianalysis.MIA.Module.Core.InputControl;
import io.github.mianalysis.MIA.Object.Measurement;
import io.github.mianalysis.MIA.Object.Obj;
import io.github.mianalysis.MIA.Object.ObjCollection;
import io.github.mianalysis.MIA.Object.Status;
import io.github.mianalysis.MIA.Object.Workspace;
import io.github.mianalysis.MIA.Object.Parameters.BooleanP;
import io.github.mianalysis.MIA.Object.Parameters.ChoiceP;
import io.github.mianalysis.MIA.Object.Parameters.InputObjectsP;
import io.github.mianalysis.MIA.Object.Parameters.ParameterCollection;
import io.github.mianalysis.MIA.Object.Parameters.SeparatorP;
import io.github.mianalysis.MIA.Object.Parameters.Objects.OutputClusterObjectsP;
import io.github.mianalysis.MIA.Object.Parameters.Text.DoubleP;
import io.github.mianalysis.MIA.Object.References.ObjMeasurementRef;
import io.github.mianalysis.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.MetadataRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.ParentChildRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.PartnerRefCollection;

public class RelateOneToOne extends Module {
    public static final String INPUT_SEPARATOR = "Objects input/output";
    public final static String INPUT_OBJECTS_1 = "Input objects 1";
    public final static String INPUT_OBJECTS_2 = "Input objects 2";
    public static final String CREATE_CLUSTER_OBJECTS = "Create cluster objects";
    public static final String OUTPUT_OBJECTS_NAME = "Output cluster objects";

    public static final String RELATIONSHIP_SEPARATOR = "Relationship settings";
    public static final String RELATIONSHIP_MODE = "Relationship mode";
    public static final String MAXIMUM_SEPARATION = "Maximum separation";
    public static final String CALIBRATED_UNITS = "Calibrated units";
    public static final String MINIMUM_OVERLAP_PC_1 = "Minimum overlap of object 1 (%)";
    public static final String MINIMUM_OVERLAP_PC_2 = "Minimum overlap of object 2 (%)";

    
    public interface RelationshipModes {
        String CENTROID_SEPARATION = "Centroid separation";
        String SPATIAL_OVERLAP = "Spatial overlap";

        String[] ALL = new String[] { CENTROID_SEPARATION, SPATIAL_OVERLAP };

    }

    public interface Measurements {
        String FRACTION_1 = "FRACTION1";
        String N_VOXELS1 = "N_VOXELS1";
        String FRACTION_2 = "FRACTION2";
        String N_VOXELS2 = "N_VOXELS2";
        String WAS_LINKED1 = "WAS_LINKED1";

    }

    public static String getFullName(String objectName, String measurement) {
        return "RELATE_ONE_TO_ONE // " + measurement.substring(0, measurement.length() - 1) + "_" + objectName;

    }

    static ArrayList<Linkable> getCentroidSeparationLinkables(ObjCollection inputObjects1, ObjCollection inputObjects2,
            double maxSeparation) {
        ArrayList<Linkable> linkables = new ArrayList<>();

        // Getting linkable objects
        for (Obj object1 : inputObjects1.values()) {
            for (Obj object2 : inputObjects2.values()) {
                // Calculating the separation between the two objects
                double overlap = object1.getCentroidSeparation(object2, true);

                // Only add if within the linking limit
                if (overlap <= maxSeparation)
                    linkables.add(new Linkable(overlap, object1.getID(), object2.getID()));

            }
        }

        return linkables;

    }

    static ArrayList<Linkable> getSpatialOverlapLinkables(ObjCollection inputObjects1, ObjCollection inputObjects2,
            double minOverlap1, double minOverlap2) {
        ArrayList<Linkable> linkables = new ArrayList<>();

        // Calculating the overlaps
        for (Obj object1 : inputObjects1.values()) {
            for (Obj object2 : inputObjects2.values()) {
                // Calculate the overlap between the two objects
                double overlap = object1.getOverlap(object2);

                // We want large overlaps to be large when they're bad, so taking the inverse
                if (overlap >= 0) {
                    double overlapPercentage1 = 100 * overlap / object1.size();
                    double overlapPercentage2 = 100 * overlap / object2.size();

                    // Checking the minimum overlaps have been met
                    if (overlapPercentage1 > minOverlap1 && overlapPercentage2 > minOverlap2) {
                        // Calculated using the raw pixel overlap to prevent small objects being
                        // weighted too highly
                        linkables.add(new Linkable(1 / overlap, object1.getID(), object2.getID()));
                    }
                }
            }
        }

        return linkables;

    }

    static DefaultCostMatrixCreator<Integer, Integer> getCostMatrixCreator(ArrayList<Linkable> linkables) {
        return getCostMatrixCreator(linkables, 1.05, 1);
    }

    static DefaultCostMatrixCreator<Integer, Integer> getCostMatrixCreator(ArrayList<Linkable> linkables,
            double alternativeCostFactor, double percentile) {
        List<Integer> IDs1 = linkables.stream().mapToInt(Linkable::getID1).boxed()
                .collect(Collectors.toCollection(ArrayList::new));
        List<Integer> IDs2 = linkables.stream().mapToInt(Linkable::getID2).boxed()
                .collect(Collectors.toCollection(ArrayList::new));
        double[] costs = linkables.stream().mapToDouble(Linkable::getCost).toArray();

        // Determining links using TrackMate implementation of Jonker-Volgenant
        // algorithm for linear assignment problems
        DefaultCostMatrixCreator<Integer, Integer> creator = new DefaultCostMatrixCreator<>(IDs1, IDs2, costs,
                alternativeCostFactor, percentile);

        if (!creator.checkInput() || !creator.process())
            return null;

        return creator;

    }

    static ObjCollection assignLinks(ObjCollection inputObjects1, ObjCollection inputObjects2,
            DefaultCostMatrixCreator<Integer, Integer> creator, @Nullable String outputObjectsName) {
        ObjCollection outputObjects = null;
        if (outputObjectsName != null)
            outputObjects = new ObjCollection(outputObjectsName, inputObjects1);

        JaqamanLinker<Integer, Integer> linker = new JaqamanLinker<>(creator);
        if (!linker.checkInput() || !linker.process())
            return null;
        Map<Integer, Integer> assignment = linker.getResult();

        for (Integer ID1 : assignment.keySet()) {
            int ID2 = assignment.get(ID1);
            Obj object1 = inputObjects1.get(ID1);
            Obj object2 = inputObjects2.get(ID2);

            // Adding measurements
            object1.addMeasurement(new Measurement(getFullName(object2.getName(), Measurements.WAS_LINKED1), 1));
            object2.addMeasurement(new Measurement(getFullName(object1.getName(), Measurements.WAS_LINKED1), 1));

            // Adding partnerships
            object1.addPartner(object2);
            object2.addPartner(object1);

            // Creating new object
            if (outputObjectsName != null)
                createClusterObject(object1, object2, outputObjects);
            
        }

        return outputObjects;

    }

    static void addMissingLinks(ObjCollection inputObjects1, ObjCollection inputObjects2) {
        // Ensuring input objects have "WAS_LINKED" measurements
        String name = getFullName(inputObjects2.getName(), Measurements.WAS_LINKED1);
        for (Obj object1 : inputObjects1.values()) {
            if (object1.getMeasurement(name) == null)
                object1.addMeasurement(new Measurement(name, 0));
        }

        name = getFullName(inputObjects1.getName(), Measurements.WAS_LINKED1);
        for (Obj object2 : inputObjects2.values()) {
            if (object2.getMeasurement(name) == null)
                object2.addMeasurement(new Measurement(name, 0));
        }
    }

    static Obj createClusterObject(Obj object1, Obj object2, ObjCollection outputObjects) {
        Obj outputObject = outputObjects.createAndAddNewObject(object1.getVolumeType());
        outputObject.setT(object1.getT());

        // Adding relationships
        outputObject.addChild(object1);
        outputObject.addChild(object2);
        object1.addParent(outputObject);
        object2.addParent(outputObject);

        // Adding measurements
        double nPoints1 = (double) object1.size();
        double nPoints2 = (double) object2.size();
        double nTotalPoints = nPoints1 + nPoints2;
        double fraction1 = nPoints1 / nTotalPoints;
        double fraction2 = nPoints2 / nTotalPoints;

        String name = getFullName(object1.getName(), Measurements.FRACTION_1);
        outputObject.addMeasurement(new Measurement(name, fraction1));
        name = getFullName(object1.getName(), Measurements.N_VOXELS1);
        outputObject.addMeasurement(new Measurement(name, nPoints1));
        name = getFullName(object1.getName(), Measurements.FRACTION_2);
        outputObject.addMeasurement(new Measurement(name, fraction2));
        name = getFullName(object2.getName(), Measurements.N_VOXELS2);
        outputObject.addMeasurement(new Measurement(name, nPoints2));

        return outputObject;

    }

    public RelateOneToOne(ModuleCollection modules) {
        super("Relate one-to-one", modules);
    }

    @Override
    public String getDescription() {
        return "Relate objects of two classes based on spatial proximity or overlap.  With this module, each object from a collection can only be linked to one other object (see \""
                + new RelateManyToMany(null).getName() + "\" and \"" + new RelateManyToOne(null).getName()
                + "\" modules for alternatives).  The assignments are chosen to give the optimal overall relationship connectivity.  As such, an object may not be linked to its own best match if that best match is itself closer still to another object.  Related objects are assigned partner relationships and can optionally also be related by a common cluster (parent) object.  Measurements associated with this relationship (e.g. distance to the related object) are stored as measurements of the relevant object.";

    }


    @Override
    public Category getCategory() {
        return Categories.OBJECT_PROCESSING_RELATIONSHIPS;
    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting input objects
        String inputObjects1Name = parameters.getValue(INPUT_OBJECTS_1);
        ObjCollection inputObjects1 = workspace.getObjects().get(inputObjects1Name);

        String inputObjects2Name = parameters.getValue(INPUT_OBJECTS_2);
        ObjCollection inputObjects2 = workspace.getObjects().get(inputObjects2Name);

        // Getting parameters
        boolean createClusterObjects = parameters.getValue(CREATE_CLUSTER_OBJECTS);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS_NAME);
        String relationshipMode = parameters.getValue(RELATIONSHIP_MODE);
        double maximumSeparation = parameters.getValue(MAXIMUM_SEPARATION);
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS);
        double minOverlap1 = parameters.getValue(MINIMUM_OVERLAP_PC_1);
        double minOverlap2 = parameters.getValue(MINIMUM_OVERLAP_PC_2);

        // Skipping the module if no objects are present in one collection
        if (inputObjects1.size() == 0 || inputObjects2.size() == 0) {
            addMissingLinks(inputObjects1, inputObjects2);
            workspace.addObjects(new ObjCollection(outputObjectsName, inputObjects1));
            return Status.PASS;
        }

        if (!createClusterObjects)
            outputObjectsName = null;

        Obj firstObj = inputObjects1.getFirst();
        if (calibratedUnits)
            maximumSeparation = maximumSeparation / firstObj.getDppXY();

        // Calculating linking costs
        ArrayList<Linkable> linkables = null;
        switch (relationshipMode) {
            case RelationshipModes.CENTROID_SEPARATION:
            default:
                linkables = getCentroidSeparationLinkables(inputObjects1, inputObjects2, maximumSeparation);
                break;

            case RelationshipModes.SPATIAL_OVERLAP:
                linkables = getSpatialOverlapLinkables(inputObjects1, inputObjects2, minOverlap1, minOverlap2);
                break;
        }

        ObjCollection outputObjects = null;
        if (linkables.size() != 0) {
            // Creating cost matrix and checking creator was created
            DefaultCostMatrixCreator<Integer, Integer> creator = getCostMatrixCreator(linkables);
            if (creator != null)
                outputObjects = assignLinks(inputObjects1, inputObjects2, creator, outputObjectsName);
        }

        // Assigning missing links
        addMissingLinks(inputObjects1, inputObjects2);

        if (createClusterObjects)
            workspace.addObjects(outputObjects);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS_1, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS_2, this));
        parameters.add(new BooleanP(CREATE_CLUSTER_OBJECTS, this, true));
        parameters.add(new OutputClusterObjectsP(OUTPUT_OBJECTS_NAME, this));

        parameters.add(new SeparatorP(RELATIONSHIP_SEPARATOR, this));
        parameters.add(new ChoiceP(RELATIONSHIP_MODE, this, RelationshipModes.SPATIAL_OVERLAP, RelationshipModes.ALL));
        parameters.add(new DoubleP(MAXIMUM_SEPARATION, this, 1.0));
        parameters.add(new BooleanP(CALIBRATED_UNITS, this, false));
        parameters.add(new DoubleP(MINIMUM_OVERLAP_PC_1, this, 50.0));
        parameters.add(new DoubleP(MINIMUM_OVERLAP_PC_2, this, 50.0));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS_1));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS_2));
        returnedParameters.add(parameters.getParameter(CREATE_CLUSTER_OBJECTS));
        if ((boolean) parameters.getValue(CREATE_CLUSTER_OBJECTS)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS_NAME));
        }

        returnedParameters.add(parameters.getParameter(RELATIONSHIP_SEPARATOR));
        returnedParameters.add(parameters.getParameter(RELATIONSHIP_MODE));
        switch ((String) parameters.getValue(RELATIONSHIP_MODE)) {
            case RelationshipModes.CENTROID_SEPARATION:
                returnedParameters.add(parameters.getParameter(MAXIMUM_SEPARATION));
                returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));
                break;
            case RelationshipModes.SPATIAL_OVERLAP:
                returnedParameters.add(parameters.getParameter(MINIMUM_OVERLAP_PC_1));
                returnedParameters.add(parameters.getParameter(MINIMUM_OVERLAP_PC_2));
                break;
        }

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
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS_NAME);

        ObjMeasurementRefCollection returnedRefs = new ObjMeasurementRefCollection();

        String name = getFullName(inputObjectsName1, Measurements.FRACTION_1);
        ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(outputObjectsName);
        returnedRefs.add(reference);
        reference.setDescription(
                "Fraction of overlap object which is coincident with \"" + inputObjectsName1 + "\" objects");

        name = getFullName(inputObjectsName1, Measurements.N_VOXELS1);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(outputObjectsName);
        returnedRefs.add(reference);
        reference.setDescription(
                "Number of voxels in overlap object which are coincident with \"" + inputObjectsName1 + "\" objects");

        name = getFullName(inputObjectsName2, Measurements.FRACTION_2);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(outputObjectsName);
        returnedRefs.add(reference);
        reference.setDescription(
                "Fraction of overlap object which is coincident with \"" + inputObjectsName2 + "\" objects");

        name = getFullName(inputObjectsName2, Measurements.N_VOXELS2);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(outputObjectsName);
        returnedRefs.add(reference);
        reference.setDescription(
                "Number of voxels in overlap object which are coincident with \"" + inputObjectsName2 + "\" objects");

        name = getFullName(inputObjectsName2, Measurements.WAS_LINKED1);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputObjectsName1);
        returnedRefs.add(reference);
        reference.setDescription("Was this \"" + inputObjectsName1 + "\" object linked with a \"" + inputObjectsName2
                + "\" object.  Linked objects have a value of \"1\" and unlinked objects have a value of \"0\".");

        name = getFullName(inputObjectsName1, Measurements.WAS_LINKED1);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputObjectsName2);
        returnedRefs.add(reference);
        reference.setDescription("Was this \"" + inputObjectsName2 + "\" object linked with a \"" + inputObjectsName1
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

        if ((boolean) parameters.getValue(CREATE_CLUSTER_OBJECTS)) {
            // Getting input objects
            String inputObjects1Name = parameters.getValue(INPUT_OBJECTS_1);
            String inputObjects2Name = parameters.getValue(INPUT_OBJECTS_2);
            String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS_NAME);

            returnedRefs.add(parentChildRefs.getOrPut(outputObjectsName, inputObjects1Name));
            returnedRefs.add(parentChildRefs.getOrPut(outputObjectsName, inputObjects2Name));

        }

        return returnedRefs;

    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        PartnerRefCollection returnedRefs = new PartnerRefCollection();

        String inputObjects1Name = parameters.getValue(INPUT_OBJECTS_1);
        String inputObjects2Name = parameters.getValue(INPUT_OBJECTS_2);

        returnedRefs.add(partnerRefs.getOrPut(inputObjects1Name, inputObjects2Name));

        return returnedRefs;

    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_OBJECTS_1).setDescription(
                "First objection collection from the workspace to relate objects for.  These objects will be related to the objects from the collection specified by \""
                        + INPUT_OBJECTS_2
                        + "\".  Related objects will be given partner relationships and optionally (depending on the state of \""
                        + CREATE_CLUSTER_OBJECTS + "\") be related by a common parent cluster object.");

        parameters.get(INPUT_OBJECTS_2).setDescription(
                "Second objection collection from the workspace to relate objects for.  These objects will be related to the objects from the collection specified by \""
                        + INPUT_OBJECTS_1
                        + "\".  Related objects will be given partner relationships and optionally (depending on the state of \""
                        + CREATE_CLUSTER_OBJECTS + "\") be related by a common parent cluster object.");

        parameters.get(CREATE_CLUSTER_OBJECTS).setDescription(
                "When selected, new \"cluster\" objects will be created and added to the workspace.  These objects contain no spatial information, but act as links between all objects that were related.  Both objects identified as relating to each other are stored as children of the same cluster object.");

        parameters.get(OUTPUT_OBJECTS_NAME)
                .setDescription("If storing cluster objects (when \"" + CREATE_CLUSTER_OBJECTS
                        + "\" is selected), the output cluster objects will be added to the workspace with this name.");

        parameters.get(RELATIONSHIP_MODE).setDescription(
                "Controls the type of calculation used when determining which objects are related:<br><ul>"

                        + "<li>\"" + RelationshipModes.CENTROID_SEPARATION
                        + "\" Distances are calculated from object centroid to object centroid.  These distances are always positive; increasing as the distance between centroids increases.</li>"

                        + "<li>\"" + RelationshipModes.SPATIAL_OVERLAP
                        + "\" The percentage of each object, which overlaps with another object is calculated.</li>");

        parameters.get(MAXIMUM_SEPARATION)
                .setDescription("If \"" + RELATIONSHIP_MODE + "\" is set to \"" + RelationshipModes.CENTROID_SEPARATION
                        + "\", this is the maximum separation two objects can have and still be related.");

        parameters.get(CALIBRATED_UNITS).setDescription(
                "When selected, spatial values are assumed to be specified in calibrated units (as defined by the \""
                        + new InputControl(null).getName() + "\" parameter \"" + InputControl.SPATIAL_UNIT
                        + "\").  Otherwise, pixel units are assumed.");

        parameters.get(MINIMUM_OVERLAP_PC_1).setDescription("If \"" + RELATIONSHIP_MODE + "\" is set to \""
                + RelationshipModes.SPATIAL_OVERLAP
                + "\", this is the minimum percentage overlap the first object must have with the other object for the two objects to be related.");

        parameters.get(MINIMUM_OVERLAP_PC_2).setDescription("If \"" + RELATIONSHIP_MODE + "\" is set to \""
                + RelationshipModes.SPATIAL_OVERLAP
                + "\", this is the minimum percentage overlap the second object must have with the other object for the two objects to be related.");

    }
}
