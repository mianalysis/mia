package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Refinement;

import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;

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

        // If necessary, creating the new ObjCollection
        ObjCollection outputObjects = new ObjCollection(outputObjectsName);

        switch (overlapRequirement) {
            case OverlapRequirements.MUTUAL_MAX_OVERLAP:

                break;

            case OverlapRequirements.MAX_OVERLAP_FOR_OBJECTS1:

                break;

            case OverlapRequirements.MAX_OVERLAP_FOR_OBJECTS2:

                break;
        }
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS_1,Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(INPUT_OBJECTS_2,Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(OUTPUT_OBJECTS_NAME,Parameter.OUTPUT_OBJECTS,null));
        parameters.add(new Parameter(MINIMUM_OVERLAP_PC,Parameter.DOUBLE,0.0));
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
