// TODO: For unit test: reverse coordinates for ExpectedObjects 3D

package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Refinement;

import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Object.Spot;

import java.util.HashMap;
import java.util.Iterator;

public class ResolveObjectOverlap extends Module {
    public final static String INPUT_OBJECTS_1 = "Input objects 1";
    public final static String INPUT_OBJECTS_2 = "Input objects 2";
    public static final String OUTPUT_OBJECTS_NAME = "Output objects name";
    public static final String OVERLAP_MODE = "Overlap mode";
    public static final String MAXIMUM_SEPARATION = "Maximum separation";
    public static final String CALIBRATED_UNITS = "Calibrated units";
    public static final String MINIMUM_OVERLAP_PC = "Minimum overlap (%)";
    public static final String OVERLAP_REQUIREMENT = "Overlap requirement";

    public interface OverlapModes {
        String CENTROID_SEPARATION = "Centroid separation";
        String SPATIAL_OVERLAP = "Spatial overlap";

        String[] ALL = new String[]{CENTROID_SEPARATION,SPATIAL_OVERLAP};

    }

    public interface OverlapRequirements {
        String MUTUALLY_OPTIMAL = "Mutually optimal";
        String OPTIMAL_FOR_OBJECTS1 = "Optimal for objects 1";
        String OPTIMAL_FOR_OBJECTS2 = "Optimal for objects 2";

        String[] ALL = new String[]{MUTUALLY_OPTIMAL, OPTIMAL_FOR_OBJECTS1, OPTIMAL_FOR_OBJECTS2};

    }

    private ObjCollection calculateSpatialOverlap(ObjCollection inputObjects1, ObjCollection inputObjects2,
                                         String outputObjectsName, double minOverlap, String overlapRequirement) {
        ObjCollection outputObjects = new ObjCollection(outputObjectsName);

        // Initialising the storage, which has ID number (key) and maximum overlap[0] and object ID[1] (value)
        HashMap<Integer,Double[]> overlaps1 = initialiseOverlapStore(inputObjects1,-Double.MAX_VALUE);
        HashMap<Integer,Double[]> overlaps2 = initialiseOverlapStore(inputObjects2,-Double.MAX_VALUE);

        // Calculating the overlaps
        for (Obj object1:inputObjects1.values()) {
            Double[] overlap1 = overlaps1.get(object1.getID());
            for (Obj object2:inputObjects2.values()) {
                Double[] overlap2 = overlaps2.get(object2.getID());

                // Calculate the overlap between the two objects
                double overlap = object1.getOverlap(object2);

                // Comparing the overlap to previously-maximum overlaps
                if (overlap>overlap1[0]) {
                    overlap1[0] = overlap;
                    overlap1[1] = (double) object2.getID();
                }

                // Comparing the overlap to previously-maximum overlaps
                if (overlap>overlap2[0]) {
                    overlap2[0] = overlap;
                    overlap2[1] = (double) object1.getID();
                }
            }
        }

        // Converting overlaps to percentages of the object's size
        for (int key:overlaps1.keySet()) {
            Obj obj = inputObjects1.get(key);
            Double[] overlap1 = overlaps1.get(key);
            overlap1[0] = 100*overlap1[0]/obj.getNVoxels();
        }

        for (int key:overlaps2.keySet()) {
            Obj obj = inputObjects2.get(key);
            Double[] overlap2 = overlaps2.get(key);
            overlap2[0] = 100*overlap2[0]/obj.getNVoxels();
        }

        switch (overlapRequirement) {
            case OverlapRequirements.MUTUALLY_OPTIMAL:
                reassignObjects(inputObjects1,inputObjects2,outputObjects,overlaps1,overlaps2,minOverlap,Double.MAX_VALUE,true);
                break;

            case OverlapRequirements.OPTIMAL_FOR_OBJECTS1:
                reassignObjects(inputObjects1,inputObjects2,outputObjects,overlaps1,overlaps2,minOverlap,Double.MAX_VALUE,false);
                break;

            case OverlapRequirements.OPTIMAL_FOR_OBJECTS2:
                reassignObjects(inputObjects2,inputObjects1,outputObjects,overlaps2,overlaps1,minOverlap,Double.MAX_VALUE,true);
                break;
        }

        return outputObjects;

    }

    private ObjCollection calculateCentroidSeparation(ObjCollection inputObjects1, ObjCollection inputObjects2,
                                                  String outputObjectsName, double maxSeparation, String overlapRequirement) {
        ObjCollection outputObjects = new ObjCollection(outputObjectsName);

        // Initialising the storage, which has ID number (key) and maximum overlap[0] and object ID[1] (value)
        HashMap<Integer,Double[]> overlaps1 = initialiseOverlapStore(inputObjects1,Double.MAX_VALUE);
        HashMap<Integer,Double[]> overlaps2 = initialiseOverlapStore(inputObjects2,Double.MAX_VALUE);

        // Calculating the separations
        for (Obj object1:inputObjects1.values()) {
            Double[] overlap1 = overlaps1.get(object1.getID());
            for (Obj object2:inputObjects2.values()) {
                Double[] overlap2 = overlaps2.get(object2.getID());

                // Calculating the separation between the two objects
                double overlap = object1.getCentroidSeparation(object2,true);

                // Comparing the overlap to previously-minimum overlaps
                if (overlap<overlap1[0]) {
                    overlap1[0] = overlap;
                    overlap1[1] = (double) object2.getID();
                }

                // Comparing the overlap to previously-minimum overlaps
                if (overlap<overlap2[0]) {
                    overlap2[0] = overlap;
                    overlap2[1] = (double) object1.getID();
                }
            }
        }

        switch (overlapRequirement) {
            case OverlapRequirements.MUTUALLY_OPTIMAL:
                reassignObjects(inputObjects1,inputObjects2,outputObjects,overlaps1,overlaps2,-Double.MAX_VALUE,maxSeparation,true);
                break;

            case OverlapRequirements.OPTIMAL_FOR_OBJECTS1:
                reassignObjects(inputObjects1,inputObjects2,outputObjects,overlaps1,overlaps2,-Double.MAX_VALUE,maxSeparation,false);
                break;

            case OverlapRequirements.OPTIMAL_FOR_OBJECTS2:
                reassignObjects(inputObjects2,inputObjects1,outputObjects,overlaps2,overlaps1,-Double.MAX_VALUE,maxSeparation,true);
                break;
        }

        return outputObjects;

    }

    private HashMap<Integer,Double[]> initialiseOverlapStore(ObjCollection inputObjects, double defaultScore) {
        HashMap<Integer,Double[]> overlaps = new HashMap<>();
        for (Obj object:inputObjects.values()) {
            overlaps.put(object.getID(),new Double[]{defaultScore,Double.NaN});
        }

        return overlaps;

    }

    private void reassignObjects(ObjCollection objects1, ObjCollection objects2, ObjCollection outputObjects,
                                 HashMap<Integer,Double[]> overlaps1, HashMap<Integer,Double[]> overlaps2,
                                 double minOverlap, double maxOverlap, boolean requireMutual) {

        Iterator<Obj> iterator = objects1.values().iterator();
        while (iterator.hasNext()) {
            Obj object1 = iterator.next();
            Double[] overlap1 = overlaps1.get(object1.getID());

            // The associated ID will be NaN if no overlap was detected
            if (Double.isNaN(overlap1[1])) continue;
            Obj object2 = objects2.get(overlap1[1].intValue());

            // There is a possibility the other object has been removed already
            if (object2 == null) continue;
            Double[] overlap2 = overlaps2.get(object2.getID());

            // Checking overlaps against the limits
            if (overlap1[0] <= minOverlap || overlap1[0] >= maxOverlap) continue;
            if (requireMutual && (overlap2[0] <= minOverlap || overlap2[0] >= maxOverlap)) continue;

            // If mutual, checking both objects identified the other as the optimal link
            if (requireMutual && overlap2[1] != object1.getID()) continue;

            // Merge objects and adding to output objects
            Obj outputObject = new Obj(outputObjects.getName(),outputObjects.getNextID(),
                    object1.getDistPerPxXY(),object1.getDistPerPxZ(),object1.getCalibratedUnits(),object1.is2D());
            outputObject.getPoints().addAll(object1.getPoints());
            outputObject.getPoints().addAll(object2.getPoints());
            outputObjects.add(outputObject);

            // Removing merged objects from input
            iterator.remove();
            objects2.remove(object2.getID());

        }
    }

    @Override
    public String getTitle() {
        return "Resolve object overlap";
    }

    @Override
    public String getHelp() {
        return "Identifies overlapping objects and moves them to a new object collection";
    }

    @Override
    protected void run(Workspace workspace) throws GenericMIAException {
        // Getting input objects
        String inputObjects1Name = parameters.getValue(INPUT_OBJECTS_1);
        ObjCollection inputObjects1 = workspace.getObjects().get(inputObjects1Name);

        String inputObjects2Name = parameters.getValue(INPUT_OBJECTS_2);
        ObjCollection inputObjects2 = workspace.getObjects().get(inputObjects2Name);

        // Getting parameters
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS_NAME);
        String overlapMode = parameters.getValue(OVERLAP_MODE);
        double maximumSeparation = parameters.getValue(MAXIMUM_SEPARATION);
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS);
        double minOverlap = parameters.getValue(MINIMUM_OVERLAP_PC);
        String overlapRequirement = parameters.getValue(OVERLAP_REQUIREMENT);

        // Skipping the module if no objects are present in one collection
        if (inputObjects1.size() == 0 || inputObjects2.size() == 0) {
            workspace.addObjects(new ObjCollection(outputObjectsName));
            return;
        }

        if (calibratedUnits) maximumSeparation = maximumSeparation*inputObjects1.values().iterator().next().getDistPerPxXY();

        ObjCollection outputObjects = null;
        switch (overlapMode) {
            case OverlapModes.CENTROID_SEPARATION:
                outputObjects = calculateCentroidSeparation(inputObjects1, inputObjects2, outputObjectsName, maximumSeparation, overlapRequirement);
                break;

            case OverlapModes.SPATIAL_OVERLAP:
                outputObjects = calculateSpatialOverlap(inputObjects1, inputObjects2, outputObjectsName, minOverlap, overlapRequirement);
                break;
        }

        workspace.addObjects(outputObjects);

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS_1,Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(INPUT_OBJECTS_2,Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(OUTPUT_OBJECTS_NAME,Parameter.OUTPUT_OBJECTS,null));
        parameters.add(new Parameter(OVERLAP_MODE,Parameter.CHOICE_ARRAY,OverlapModes.SPATIAL_OVERLAP,OverlapModes.ALL));
        parameters.add(new Parameter(MAXIMUM_SEPARATION,Parameter.DOUBLE,1.0));
        parameters.add(new Parameter(CALIBRATED_UNITS,Parameter.BOOLEAN,false));
        parameters.add(new Parameter(MINIMUM_OVERLAP_PC,Parameter.DOUBLE,50.0));
        parameters.add(new Parameter(OVERLAP_REQUIREMENT,Parameter.CHOICE_ARRAY,OverlapRequirements.MUTUALLY_OPTIMAL,OverlapRequirements.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS_1));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS_2));
        returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS_NAME));
        returnedParameters.add(parameters.getParameter(OVERLAP_MODE));

        switch ((String) parameters.getValue(OVERLAP_MODE)){
            case OverlapModes.CENTROID_SEPARATION:
                returnedParameters.add(parameters.getParameter(MAXIMUM_SEPARATION));
                returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));
                break;
            case OverlapModes.SPATIAL_OVERLAP:
                returnedParameters.add(parameters.getParameter(MINIMUM_OVERLAP_PC));
                break;
        }

        returnedParameters.add(parameters.getParameter(OVERLAP_REQUIREMENT));

        return returnedParameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
