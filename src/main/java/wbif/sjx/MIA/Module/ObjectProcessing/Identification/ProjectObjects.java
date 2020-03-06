package wbif.sjx.MIA.Module.ObjectProcessing.Identification;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.OutputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.Object.Volume.SpatCal;
import wbif.sjx.common.Object.Volume.Volume;

/**
 * Projects xy coordinates into a single plane.  Duplicates of xy coordinates at different heights are removed.
 */
public class ProjectObjects extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_OBJECTS = "Output objects";

    public ProjectObjects(ModuleCollection modules) {
        super("Project objects",modules);
    }

    public static Obj process(Obj inputObject, String outputObjectsName, boolean addRelationship) throws IntegerOverflowException {
        Volume projected = inputObject.getProjected();

        Obj outputObject = new Obj(outputObjectsName,inputObject.getID(),inputObject);
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
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_IDENTIFICATION;
    }

    @Override
    public String getDescription() {
        return "Create projections of objects along the z-axis into an xy-plane representation.  The output projected " +
                "objects capture the maximum xy profile of the object, thus acting like a silhouette.  These objects " +
                "are stored separately in the workspace from the input objects, but are related in a parent-child " +
                "relationship (input-output, respectively).";
    }

    @Override
    public boolean process(Workspace workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);
        SpatCal calIn = inputObjects.getSpatialCalibration();
        SpatCal calOut = new SpatCal(calIn.getDppXY(),calIn.getDppZ(),calIn.getUnits(),calIn.getWidth(),calIn.getHeight(),1);
        ObjCollection outputObjects = new ObjCollection(outputObjectsName,calOut,inputObjects.getNFrames());

        for (Obj inputObject:inputObjects.values()) {
            Obj outputObject = null;
            try {
                outputObject = process(inputObject,outputObjectsName, true);
            } catch (IntegerOverflowException e) {
                return false;
            }
            outputObjects.put(outputObject.getID(),outputObject);
        }

        workspace.addObjects(outputObjects);

        // Showing objects
        if (showOutput) outputObjects.convertToImageRandomColours().showImage();

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this, "", "Objects to be projected into the xy-plane."));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this, "", "Output projected objects to be stored in the workspace."));

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
    public RelationshipRefCollection updateAndGetRelationships() {
        RelationshipRefCollection returnedRelationships = new RelationshipRefCollection();

        returnedRelationships.add(relationshipRefs.getOrPut(parameters.getValue(INPUT_OBJECTS),parameters.getValue(OUTPUT_OBJECTS)));

        return returnedRelationships;

    }

    @Override
    public boolean verify() {
        return true;
    }
}
