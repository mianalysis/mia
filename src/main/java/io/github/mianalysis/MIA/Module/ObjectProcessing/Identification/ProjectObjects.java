package io.github.mianalysis.MIA.Module.ObjectProcessing.Identification;

import ome.units.quantity.Time;
import ome.units.unit.Unit;
import io.github.mianalysis.MIA.Module.Categories;
import io.github.mianalysis.MIA.Module.Category;
import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Module.ModuleCollection;
import io.github.mianalysis.MIA.Object.Obj;
import io.github.mianalysis.MIA.Object.ObjCollection;
import io.github.mianalysis.MIA.Object.Status;
import io.github.mianalysis.MIA.Object.Workspace;
import io.github.mianalysis.MIA.Object.Parameters.InputObjectsP;
import io.github.mianalysis.MIA.Object.Parameters.ParameterCollection;
import io.github.mianalysis.MIA.Object.Parameters.SeparatorP;
import io.github.mianalysis.MIA.Object.Parameters.Objects.OutputObjectsP;
import io.github.mianalysis.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.MetadataRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.ParentChildRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.PartnerRefCollection;
import io.github.sjcross.common.Exceptions.IntegerOverflowException;
import io.github.sjcross.common.Object.Volume.SpatCal;
import io.github.sjcross.common.Object.Volume.Volume;

/**
 * Projects xy coordinates into a single plane.  Duplicates of xy coordinates at different heights are removed.
 */
public class ProjectObjects extends Module {
    public static final String INPUT_SEPARATOR = "Object input/output";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_OBJECTS = "Output objects";

    public ProjectObjects(ModuleCollection modules) {
        super("Project objects",modules);
    }

    public static Obj process(Obj inputObject, ObjCollection outputObjects, boolean addRelationship) throws IntegerOverflowException {
        Volume projected = inputObject.getProjected();

        Obj outputObject = outputObjects.createAndAddNewObject(inputObject.getVolumeType(), inputObject.getID());
        outputObject.setCoordinateSet(projected.getCoordinateSet());
        outputObject.setT(inputObject.getT());

        // If adding relationship
        if (addRelationship) {
            outputObject.addParent(inputObject);
            inputObject.addChild(outputObject);
        }

        return outputObject;

    }



    @Override
    public Category getCategory() {
        return Categories.OBJECT_PROCESSING_IDENTIFICATION;
    }

    @Override
    public String getDescription() {
        return "Create projections of objects along the z-axis into an xy-plane representation.  The output projected " +
                "objects capture the maximum xy profile of the object, thus acting like a silhouette.  These objects " +
                "are stored separately in the workspace from the input objects, but are related in a parent-child " +
                "relationship (input-output, respectively).";
    }

    @Override
    public Status process(Workspace workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);
        SpatCal calIn = inputObjects.getSpatialCalibration();
        SpatCal calOut = new SpatCal(calIn.getDppXY(), calIn.getDppZ(), calIn.getUnits(), calIn.getWidth(),
                calIn.getHeight(), 1);
        double frameInterval = inputObjects.getFrameInterval();
        Unit<Time> temporalUnit = inputObjects.getTemporalUnit();
        ObjCollection outputObjects = new ObjCollection(outputObjectsName, calOut, inputObjects.getNFrames(),
                frameInterval, temporalUnit);

        for (Obj inputObject:inputObjects.values()) {
            try {
                process(inputObject,outputObjects, true);
            } catch (IntegerOverflowException e) {
                return Status.FAIL;
            }
        }

        workspace.addObjects(outputObjects);

        // Showing objects
        if (showOutput) outputObjects.convertToImageRandomColours().showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        ParentChildRefCollection returnedRelationships = new ParentChildRefCollection();

        returnedRelationships.add(parentChildRefs.getOrPut(parameters.getValue(INPUT_OBJECTS),parameters.getValue(OUTPUT_OBJECTS)));

        return returnedRelationships;

    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_OBJECTS).setDescription("Objects to be projected into the xy-plane.  Tese are related as a parent of their respective projected object.");

        parameters.get(OUTPUT_OBJECTS).setDescription("Output projected objects to be stored in the workspace.  These are related as children of the respective input object.");

    }
}
