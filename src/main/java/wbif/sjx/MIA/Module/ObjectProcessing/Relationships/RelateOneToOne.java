package wbif.sjx.MIA.Module.ObjectProcessing.Relationships;

import blogspot.software_and_algorithms.stern_library.optimization.HungarianAlgorithm;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.Deprecated.ResolveCoOccurrence;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.MIA.Object.Workspace;

import javax.annotation.Nullable;
import java.util.ArrayList;

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

        String[] ALL = new String[]{CENTROID_SEPARATION,SPATIAL_OVERLAP};

    }

    public interface Measurements {
        String FRACTION_1 = "FRACTION1";
        String N_VOXELS1 = "N_VOXELS1";
        String FRACTION_2 = "FRACTION2";
        String N_VOXELS2 = "N_VOXELS2";
        String WAS_LINKED1 = "WAS_LINKED1";

        String[] ALL = new String[]{FRACTION_1,N_VOXELS1,FRACTION_2,N_VOXELS2};

    }

    public static String getFullName(String objectName, String measurement) {
        return "RELATE_ONE_TO_ONE // "+measurement.substring(0,measurement.length()-1)+"_"+objectName;

    }

    double[][] calculateCentroidSeparationCosts(ObjCollection inputObjects1, ObjCollection inputObjects2, double maxSeparation) {
        double[][] costs = new double[inputObjects1.size()][inputObjects2.size()];

        // Calculating the separations
        long totalPairs = inputObjects1.size()*inputObjects2.size();
        long count = 0;
        int i = 0;

        for (Obj object1:inputObjects1.values()) {
            int j = 0;
            for (Obj object2:inputObjects2.values()) {
                // Calculating the separation between the two objects
                double overlap = object1.getCentroidSeparation(object2,true);

                // Applying the linking limit
                if (overlap > maxSeparation) overlap = Double.MAX_VALUE;
                costs[i][j] = overlap;

                count++;
                j++;

            }

            i++;

            writeMessage("Calculated cost for "+Math.floorDiv(100*count,totalPairs)+"% of pairs");

        }

        return costs;

    }

    double[][] calculateSpatialOverlapCosts(ObjCollection inputObjects1, ObjCollection inputObjects2, double minOverlap1, double minOverlap2) {
        double[][] costs = new double[inputObjects1.size()][inputObjects2.size()];

        // Calculating the overlaps
        long totalPairs = inputObjects1.size()*inputObjects2.size();
        long count = 0;
        int i = 0;

        for (Obj object1:inputObjects1.values()) {
            int j = 0;
            for (Obj object2:inputObjects2.values()) {
                // Calculate the overlap between the two objects
                double overlap = object1.getOverlap(object2);

                // We want large overlaps to be large when they're bad, so taking the inverse
                if (overlap == 0) {
                    costs[i][j] = Double.MAX_VALUE;
                } else {
                    double overlapPercentage1 = 100*overlap/object1.getNVoxels();
                    double overlapPercentage2 = 100*overlap/object2.getNVoxels();

                    // Checking the minimum overlaps have been met
                    if (overlapPercentage1> minOverlap1 && overlapPercentage2> minOverlap2) {
                        // Cost is calculated using the raw pixel overlap to prevent small objects being weighted too highly
                        costs[i][j] = 1 / overlap;
                    } else {
                        costs[i][j] = Double.MAX_VALUE;
                    }
                }
                j++;
            }

            i++;

            writeMessage("Calculated cost for "+Math.floorDiv(100*(count++),totalPairs)+"% of pairs");

        }

        return costs;

    }

    ObjCollection assignLinks(ObjCollection inputObjects1, ObjCollection inputObjects2, double[][] costs, @Nullable String outputObjectsName) {
        ObjCollection outputObjects = null;
        if (outputObjectsName != null) outputObjects = new ObjCollection(outputObjectsName);

        // Determining links using Munkres (Hungarian) algorithm
        writeMessage("Calculating links");
        HungarianAlgorithm hungarianAlgorithm = new HungarianAlgorithm(costs);
        int[] assignment = hungarianAlgorithm.execute();

        ArrayList<Obj> objects1 = new ArrayList<>(inputObjects1.values());
        ArrayList<Obj> objects2 = new ArrayList<>(inputObjects2.values());

        // Applying the calculated assignments as relationships
        long totalPairs = inputObjects1.size()*inputObjects2.size();
        long count = 0;
        for (int curr = 0; curr < assignment.length; curr++) {
            // Getting the object from the current frame
            Obj object1 = objects1.get(curr);

            // Checking if the two objects can be linked
            if (assignment[curr] == -1 || costs[curr][assignment[curr]] == Double.MAX_VALUE) continue;

            // Getting linked object
            Obj object2 = objects2.get(assignment[curr]);

            // Adding measurements
            object1.addMeasurement(new Measurement(getFullName(object2.getName(),Measurements.WAS_LINKED1),1));
            object2.addMeasurement(new Measurement(getFullName(object1.getName(),Measurements.WAS_LINKED1),1));

            // Creating new object
            if (outputObjectsName != null) {
                int ID = outputObjects.getAndIncrementID();
                outputObjects.add(createClusterObject(object1,object2,outputObjectsName,ID));
            }

            writeMessage("Assigned links for "+Math.floorDiv(100*(count++),totalPairs)+"% of pairs");

        }

        // Ensuring input objects have "WAS_LINKED" measurements
        String name = getFullName(inputObjects2.getName(),Measurements.WAS_LINKED1);
        for (Obj object1:inputObjects1.values()) {
            if (object1.getMeasurement(name) == null) object1.addMeasurement(new Measurement(name,0));
        }

        name = getFullName(inputObjects1.getName(),Measurements.WAS_LINKED1);
        for (Obj object2:inputObjects2.values()) {
            if (object2.getMeasurement(name) == null) object2.addMeasurement(new Measurement(name,0));
        }

        return outputObjects;

    }

    static Obj createClusterObject(Obj object1, Obj object2, String outputObjectsName, int ID) {
        double dppXY = object1.getDistPerPxXY();
        double dppZ = object1.getDistPerPxZ();
        String units = object1.getCalibratedUnits();
        boolean is2D = object1.is2D();

        Obj outputObject = new Obj(outputObjectsName, ID, dppXY, dppZ, units, is2D);

        // Adding relationships
        outputObject.addChild(object1);
        outputObject.addChild(object2);
        object1.addParent(outputObject);
        object2.addParent(outputObject);

        // Adding measurements
        double nPoints1 = (double) object1.getNVoxels();
        double nPoints2 = (double) object2.getNVoxels();
        double nTotalPoints = nPoints1 + nPoints2;
        double fraction1 = nPoints1/nTotalPoints;
        double fraction2 = nPoints2/nTotalPoints;

        String name = getFullName(object1.getName(), Measurements.FRACTION_1);
        outputObject.addMeasurement(new Measurement(name,fraction1));
        name = getFullName(object1.getName(), Measurements.N_VOXELS1);
        outputObject.addMeasurement(new Measurement(name,nPoints1));
        name = getFullName(object1.getName(), Measurements.FRACTION_2);
        outputObject.addMeasurement(new Measurement(name,fraction2));
        name = getFullName(object2.getName(), Measurements.N_VOXELS2);
        outputObject.addMeasurement(new Measurement(name,nPoints2));

        return outputObject;

    }


    public RelateOneToOne(ModuleCollection modules) {
        super("Relate one-to-one", modules);
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_RELATIONSHIPS;
    }

    @Override
    protected boolean process(Workspace workspace) {
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
            workspace.addObjects(new ObjCollection(outputObjectsName));
            return true;
        }

        if (!createClusterObjects) outputObjectsName = null;

        Obj firstObj = inputObjects1.getFirst();
        if (calibratedUnits) maximumSeparation = maximumSeparation/firstObj.getDistPerPxXY();

        // Calculating linking costs
        double[][] costs = null;
        switch (relationshipMode) {
            case ResolveCoOccurrence.OverlapModes.CENTROID_SEPARATION:
                costs = calculateCentroidSeparationCosts(inputObjects1,inputObjects2,maximumSeparation);
                break;

            case ResolveCoOccurrence.OverlapModes.SPATIAL_OVERLAP:
                costs = calculateSpatialOverlapCosts(inputObjects1,inputObjects2,minOverlap1,minOverlap2);
                break;
        }

        // Assigning optimal links
        ObjCollection outputObjects = assignLinks(inputObjects1,inputObjects2,costs,outputObjectsName);

        if (createClusterObjects) workspace.addObjects(outputObjects);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS_1,this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS_2,this));
        parameters.add(new BooleanP(CREATE_CLUSTER_OBJECTS,this,true));
        parameters.add(new OutputClusterObjectsP(OUTPUT_OBJECTS_NAME,this));

        parameters.add(new ParamSeparatorP(RELATIONSHIP_SEPARATOR,this));
        parameters.add(new ChoiceP(RELATIONSHIP_MODE,this, RelationshipModes.SPATIAL_OVERLAP, RelationshipModes.ALL));
        parameters.add(new DoubleP(MAXIMUM_SEPARATION,this,1.0));
        parameters.add(new BooleanP(CALIBRATED_UNITS,this,false));
        parameters.add(new DoubleP(MINIMUM_OVERLAP_PC_1,this,50.0));
        parameters.add(new DoubleP(MINIMUM_OVERLAP_PC_2,this,50.0));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS_1));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS_2));
        returnedParameters.add(parameters.getParameter(CREATE_CLUSTER_OBJECTS));
        if (parameters.getValue(CREATE_CLUSTER_OBJECTS)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS_NAME));
        }

        returnedParameters.add(parameters.getParameter(RELATIONSHIP_SEPARATOR));
        returnedParameters.add(parameters.getParameter(RELATIONSHIP_MODE));
        switch ((String) parameters.getValue(RELATIONSHIP_MODE)){
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
        reference.setDescription("Fraction of overlap object which is coincident with \""+inputObjectsName1+"\" objects");

        name = getFullName(inputObjectsName1, Measurements.N_VOXELS1);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(outputObjectsName);
        returnedRefs.add(reference);
        reference.setDescription("Number of voxels in overlap object which are coincident with \""+inputObjectsName1+"\" objects");

        name = getFullName(inputObjectsName2, Measurements.FRACTION_2);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(outputObjectsName);
        returnedRefs.add(reference);
        reference.setDescription("Fraction of overlap object which is coincident with \""+inputObjectsName2+"\" objects");

        name = getFullName(inputObjectsName2, Measurements.N_VOXELS2);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(outputObjectsName);
        returnedRefs.add(reference);
        reference.setDescription("Number of voxels in overlap object which are coincident with \""+inputObjectsName2+"\" objects");

        name = getFullName(inputObjectsName2, Measurements.WAS_LINKED1);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputObjectsName1);
        returnedRefs.add(reference);
        reference.setDescription("Was this \""+inputObjectsName1+"\" object linked with a \""+inputObjectsName2+"\" object.  Linked objects have a value of \"1\" and unlinked objects have a value of \"0\".");

        name = getFullName(inputObjectsName1, Measurements.WAS_LINKED1);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputObjectsName2);
        returnedRefs.add(reference);
        reference.setDescription("Was this \""+inputObjectsName2+"\" object linked with a \""+inputObjectsName1+"\" object.  Linked objects have a value of \"1\" and unlinked objects have a value of \"0\".");

        return returnedRefs;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipRefCollection updateAndGetRelationships() {
        RelationshipRefCollection returnedRefs = new RelationshipRefCollection();

        if (parameters.getValue(CREATE_CLUSTER_OBJECTS)) {
            // Getting input objects
            String inputObjects1Name = parameters.getValue(INPUT_OBJECTS_1);
            String inputObjects2Name = parameters.getValue(INPUT_OBJECTS_2);
            String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS_NAME);

            returnedRefs.add(relationshipRefs.getOrPut(outputObjectsName, inputObjects1Name));
            returnedRefs.add(relationshipRefs.getOrPut(outputObjectsName, inputObjects2Name));

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
