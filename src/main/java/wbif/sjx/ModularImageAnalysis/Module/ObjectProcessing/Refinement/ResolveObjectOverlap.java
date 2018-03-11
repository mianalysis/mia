// TODO: For unit test: reverse coordinates for ExpectedObjects 3D

package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Refinement;

import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.util.HashMap;
import java.util.Iterator;

public class ResolveObjectOverlap extends Module {
    public final static String INPUT_OBJECTS_1 = "Input objects 1";
    public final static String INPUT_OBJECTS_2 = "Input objects 2";
    public static final String OUTPUT_OBJECTS_NAME = "Output objects name";
    public static final String MINIMUM_OVERLAP_PC = "Minimum overlap (%)";
    public static final String OVERLAP_REQUIREMENT = "Overlap requirement";

    public interface OverlapRequirements {
        String MUTUAL_MAX_OVERLAP = "Mutual maximum overlap";
        String MAX_OVERLAP_FOR_OBJECTS1 = "Maximum overlap for objects 1";
        String MAX_OVERLAP_FOR_OBJECTS2 = "Maximum overlap for objects 2";

        String[] ALL = new String[]{MUTUAL_MAX_OVERLAP,MAX_OVERLAP_FOR_OBJECTS1,MAX_OVERLAP_FOR_OBJECTS2};

    }


    private void reassignObjects(ObjCollection objects1, ObjCollection objects2, ObjCollection outputObjects,
                                 HashMap<Integer,Double[]> overlaps1, HashMap<Integer,Double[]> overlaps2,
                                 double minOverlap, boolean requireMutual) {

        Iterator<Obj> iterator = objects1.values().iterator();
        while (iterator.hasNext()) {
            Obj object1 = iterator.next();
            Double[] overlap1 = overlaps1.get(object1.getID());

            // The associated ID will be zero if no overlap was detected
            if (overlap1[1] == 0) continue;

            Obj object2 = objects2.get(overlap1[1].intValue());
            Double[] overlap2 = overlaps2.get(object2.getID());

            double overlapPC1 = 100*overlap1[0]/object1.getPoints().size();
            double overlapPC2 = 100*overlap2[0]/object2.getPoints().size();

            if (overlapPC1 < minOverlap) continue;
            if (requireMutual && overlap2[1] != object1.getID()) continue;
            if (requireMutual && overlapPC2 < minOverlap) continue;

            // Merge objects and adding to output objects
            Obj outputObject = new Obj(outputObjects.getName(),outputObjects.getNextID(),
                    object1.getDistPerPxXY(),object1.getDistPerPxZ(),object1.getCalibratedUnits());
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
    protected void run(Workspace workspace, boolean verbose) throws GenericMIAException {
        // Getting input objects
        String inputObjects1Name = parameters.getValue(INPUT_OBJECTS_1);
        ObjCollection inputObjects1 = workspace.getObjects().get(inputObjects1Name);

        String inputObjects2Name = parameters.getValue(INPUT_OBJECTS_2);
        ObjCollection inputObjects2 = workspace.getObjects().get(inputObjects2Name);

        // Getting parameters
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS_NAME);
        double minOverlap = parameters.getValue(MINIMUM_OVERLAP_PC);
        String overlapRequirement = parameters.getValue(OVERLAP_REQUIREMENT);

        // Initialising the storage, which has ID number (key) and maximum overlap[0] and object ID[1] (value)
        HashMap<Integer,Double[]> overlaps1 = new HashMap<>();
        HashMap<Integer,Double[]> overlaps2 = new HashMap<>();
        for (Obj object1:inputObjects1.values()) {
            overlaps1.put(object1.getID(),new Double[]{Double.MIN_VALUE,0d});
        }
        for (Obj object2:inputObjects2.values()) {
            overlaps2.put(object2.getID(),new Double[]{Double.MIN_VALUE,0d});
        }

        // Calculating the overlaps
        for (Obj object1:inputObjects1.values()) {
            Double[] overlap1 = overlaps1.get(object1.getID());
            for (Obj object2:inputObjects2.values()) {
                Double[] overlap2 = overlaps2.get(object2.getID());

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

        ObjCollection outputObjects = new ObjCollection(outputObjectsName);
        switch (overlapRequirement) {
            case OverlapRequirements.MUTUAL_MAX_OVERLAP:
                reassignObjects(inputObjects1,inputObjects2,outputObjects,overlaps1,overlaps2,minOverlap,true);
                break;

            case OverlapRequirements.MAX_OVERLAP_FOR_OBJECTS1:
                reassignObjects(inputObjects1,inputObjects2,outputObjects,overlaps1,overlaps2,minOverlap,false);
                break;

            case OverlapRequirements.MAX_OVERLAP_FOR_OBJECTS2:
                reassignObjects(inputObjects2,inputObjects1,outputObjects,overlaps2,overlaps1,minOverlap,true);
                break;
        }

        workspace.addObjects(outputObjects);
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS_1,Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(INPUT_OBJECTS_2,Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(OUTPUT_OBJECTS_NAME,Parameter.OUTPUT_OBJECTS,null));
        parameters.add(new Parameter(MINIMUM_OVERLAP_PC,Parameter.DOUBLE,50.0));
        parameters.add(new Parameter(OVERLAP_REQUIREMENT,Parameter.CHOICE_ARRAY,OverlapRequirements.MUTUAL_MAX_OVERLAP,OverlapRequirements.ALL));

    }

    @Override
    protected void initialiseMeasurementReferences() {

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
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
