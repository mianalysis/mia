package io.github.mianalysis.mia.module.testmodules;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModulesI;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.ParentObjectsP;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

public class ExtractObjectEdges extends Module {
    public static final String INPUT_SEPARATOR = "Object input/output";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String CREATE_EDGE_OBJECTS = "Create edge objects";
    public static final String OUTPUT_EDGE_OBJECTS = "Output edge objects";
    public static final String CREATE_INTERIOR_OBJECTS = "Create interior objects";
    public static final String OUTPUT_INTERIOR_OBJECTS = "Output interior objects";
    public static final String DISTANCE_SEPARATOR = "Distance controls";
    public static final String EDGE_MODE = "Edge determination";
    public static final String EDGE_DISTANCE = "Distance";
    public static final String MEASUREMENT_NAME = "Measurement name";
    public static final String PARENT_OBJECTS = "Parent objects";
    public static final String PARENT_MEASUREMENT_NAME = "Parent measurement name";
    public static final String CALIBRATED_DISTANCES = "Calibrated distances";
    public static final String EDGE_PERCENTAGE = "Percentage";

    public enum Mode {
        INTERIOR, EDGE
    }

    public ExtractObjectEdges(ModulesI modules) {
        super("Extract object edges", modules);
    }

    public interface EdgeModes {
        String DISTANCE_FROM_EDGE = "Distance to edge";
        String OBJECT_MEASUREMENT = "Object measurement";
        String PARENT_OBJECT_MEASUREMENT = "Parent object measurement";
        String PERCENTAGE_FROM_EDGE = "Percentage of maximum distance to edge";

        String[] ALL = new String[] { DISTANCE_FROM_EDGE, OBJECT_MEASUREMENT, PARENT_OBJECT_MEASUREMENT,
                PERCENTAGE_FROM_EDGE };

    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_PROCESS;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Status process(WorkspaceI workspace) {
        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new BooleanP(CREATE_EDGE_OBJECTS, this, true));
        parameters.add(new OutputObjectsP(OUTPUT_EDGE_OBJECTS, this));
        parameters.add(new BooleanP(CREATE_INTERIOR_OBJECTS, this, true));
        parameters.add(new OutputObjectsP(OUTPUT_INTERIOR_OBJECTS, this));

        parameters.add(new SeparatorP(DISTANCE_SEPARATOR, this));
        parameters.add(new ChoiceP(EDGE_MODE, this, EdgeModes.DISTANCE_FROM_EDGE, EdgeModes.ALL));
        parameters.add(new DoubleP(EDGE_DISTANCE, this, 1.0));
        parameters.add(new ObjectMeasurementP(MEASUREMENT_NAME, this));
        parameters.add(new ParentObjectsP(PARENT_OBJECTS, this));
        parameters.add(new ObjectMeasurementP(PARENT_MEASUREMENT_NAME, this));
        parameters.add(new BooleanP(CALIBRATED_DISTANCES, this, false));
        parameters.add(new DoubleP(EDGE_PERCENTAGE, this, 1.0));

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();
        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(CREATE_EDGE_OBJECTS));
        if ((boolean) parameters.getValue(CREATE_EDGE_OBJECTS, workspace))
            returnedParameters.add(parameters.getParameter(OUTPUT_EDGE_OBJECTS));

        returnedParameters.add(parameters.getParameter(CREATE_INTERIOR_OBJECTS));
        if ((boolean) parameters.getValue(CREATE_INTERIOR_OBJECTS, workspace))
            returnedParameters.add(parameters.getParameter(OUTPUT_INTERIOR_OBJECTS));

        returnedParameters.add(parameters.getParameter(DISTANCE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(EDGE_MODE));

        switch ((String) parameters.getValue(EDGE_MODE, workspace)) {
            case EdgeModes.DISTANCE_FROM_EDGE:
                returnedParameters.add(parameters.getParameter(EDGE_DISTANCE));
                returnedParameters.add(parameters.getParameter(CALIBRATED_DISTANCES));
                break;
            case EdgeModes.OBJECT_MEASUREMENT:
                returnedParameters.add(parameters.getParameter(MEASUREMENT_NAME));
                ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT_NAME))
                        .setObjectName(parameters.getValue(INPUT_OBJECTS, workspace));
                returnedParameters.add(parameters.getParameter(CALIBRATED_DISTANCES));
                break;
            case EdgeModes.PARENT_OBJECT_MEASUREMENT:
                returnedParameters.add(parameters.getParameter(PARENT_OBJECTS));
                ((ParentObjectsP) parameters.getParameter(PARENT_OBJECTS))
                        .setChildObjectsName(parameters.getValue(INPUT_OBJECTS, workspace));
                returnedParameters.add(parameters.getParameter(PARENT_MEASUREMENT_NAME));
                ((ObjectMeasurementP) parameters.getParameter(PARENT_MEASUREMENT_NAME))
                        .setObjectName(parameters.getValue(PARENT_OBJECTS, workspace));
                returnedParameters.add(parameters.getParameter(CALIBRATED_DISTANCES));
                break;
            case EdgeModes.PERCENTAGE_FROM_EDGE:
                returnedParameters.add(parameters.getParameter(EDGE_PERCENTAGE));
                break;
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {  
	return null; 
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        WorkspaceI workspace = null;
        ParentChildRefs returnedRelationships = new ParentChildRefs();

        String inputObjects = parameters.getValue(INPUT_OBJECTS, workspace);

        if ((boolean) parameters.getValue(CREATE_EDGE_OBJECTS, workspace)) {
            String outputEdgeObjects = parameters.getValue(OUTPUT_EDGE_OBJECTS, workspace);
            returnedRelationships.add(parentChildRefs.getOrPut(inputObjects, outputEdgeObjects));
        }

        if ((boolean) parameters.getValue(CREATE_INTERIOR_OBJECTS, workspace)) {
            String outputInteriorObjects = parameters.getValue(OUTPUT_INTERIOR_OBJECTS, workspace);
            returnedRelationships.add(parentChildRefs.getOrPut(inputObjects, outputInteriorObjects));
        }

        return returnedRelationships;

    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}
