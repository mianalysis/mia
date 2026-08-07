package io.github.mianalysis.mia.module.objects.transform;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.coordinates.volume.SpatCal;
import io.github.mianalysis.mia.object.coordinates.volume.Volume;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.exceptions.IntegerOverflowException;
import ome.units.quantity.Time;
import ome.units.unit.Unit;

/**
* 
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ProjectObjects extends Module {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Object input/output";

    /**
     * Objects to be projected into the xy-plane. These are related as a parent of
     * their respective projected object.
     */
    public static final String INPUT_OBJECTS = "Input objects";

    /**
     * Output projected objects to be stored in the workspace. These are related as
     * children of the respective input object.
     */
    public static final String OUTPUT_OBJECTS = "Output objects";

    public static final String PROJECTION_SEPARATOR = "Projection controls";

    public static final String DUPLICATE_RELATIONSHIPS = "Duplicate relationships";

    public ProjectObjects(Modules modules) {
        super("Project objects", modules);
    }

    public static Obj process(Obj inputObject, Objs outputObjects, boolean addRelationship)
            throws IntegerOverflowException {
        Volume projected = inputObject.getProjected();

        Obj outputObject = outputObjects.createAndAddNewObject(inputObject.getVolumeType(), inputObject.getID());
        outputObject.setCoordinateSet(projected.getCoordinateSet());
        outputObject.setT(inputObject.getT());

        if (addRelationship) {
            outputObject.addParent(inputObject);
            inputObject.addChild(outputObject);
        }

        return outputObject;

    }

    public static void addRelationships(Objs outputObjects, String inputObjectsName) {
        for (Obj outputObject : outputObjects.values()) {
            Obj inputObject = outputObject.getParent(inputObjectsName);

            for (Obj parent : inputObject.getParents(true).values()) {
                outputObject.addParent(parent);
                parent.addChild(inputObject);
            }

            for (Objs currChildren : inputObject.getChildren().values())
                for (Obj child : currChildren.values()) {
                    outputObject.addChild(child);
                    child.addParent(outputObject);
                }

            for (Objs currPartners : inputObject.getPartners().values())
                for (Obj partner : currPartners.values()) {
                    outputObject.addPartner(partner);
                    partner.addPartner(outputObject);

                    if (partner.getName().equals(inputObjectsName)) {
                        Obj extraPartner = inputObject.getChildren(outputObject.getName()).getFirst();
                        outputObject.addPartner(extraPartner);
                        extraPartner.addPartner(outputObject);
                    }
                }
        }
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_TRANSFORM;
    }

    @Override
    public Status process(Workspace workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
        boolean duplicateRelationships = parameters.getValue(DUPLICATE_RELATIONSHIPS, workspace);

        Objs inputObjects = workspace.getObjects(inputObjectsName);
        SpatCal calIn = inputObjects.getSpatialCalibration();
        SpatCal calOut = new SpatCal(calIn.getDppXY(), calIn.getDppZ(), calIn.getUnits(), calIn.getWidth(),
                calIn.getHeight(), 1);
        double frameInterval = inputObjects.getFrameInterval();
        Unit<Time> temporalUnit = inputObjects.getTemporalUnit();
        Objs outputObjects = new Objs(outputObjectsName, calOut, inputObjects.getNFrames(),
                frameInterval, temporalUnit);

        for (Obj inputObject : inputObjects.values()) {
            try {
                process(inputObject, outputObjects, true);

                if (duplicateRelationships)
                    addRelationships(outputObjects, inputObjectsName);

            } catch (IntegerOverflowException e) {
                return Status.FAIL;
            }
        }

        workspace.addObjects(outputObjects);

        // Showing objects
        if (showOutput)
            outputObjects.convertToImageIDColours().show(false);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new SeparatorP(PROJECTION_SEPARATOR, this));
        parameters.add(new BooleanP(DUPLICATE_RELATIONSHIPS, this, false));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        return parameters;
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
        Workspace workspace = null;
        ParentChildRefs returnedRelationships = new ParentChildRefs();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);

        // Standard parent-child relationship
        returnedRelationships.add(parentChildRefs.getOrPut(inputObjectsName, outputObjectsName));

        if ((boolean) parameters.getValue(DUPLICATE_RELATIONSHIPS, workspace)) {
            // Getting references up to this location
            ParentChildRefs currentRefs = modules.getParentChildRefs(this);

            // Adding relationships where the input object is the parent
            String[] childNames = currentRefs.getChildNames(inputObjectsName, true);
            for (String childName : childNames)
                returnedRelationships.add(parentChildRefs.getOrPut(outputObjectsName, childName));

            // Adding relationships where the input object is the child
            String[] parentNames = currentRefs.getParentNames(inputObjectsName, true);
            for (String parentName : parentNames)
                returnedRelationships.add(parentChildRefs.getOrPut(parentName, outputObjectsName));

        }

        return returnedRelationships;

    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        Workspace workspace = null;

        PartnerRefs returnedRefs = new PartnerRefs();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);

        if ((boolean) parameters.getValue(DUPLICATE_RELATIONSHIPS, workspace)) {
            // Getting references up to this location
            PartnerRefs currentRefs = modules.getPartnerRefs(this);

            String[] partnerNames = currentRefs.getPartnerNamesArray(inputObjectsName);
            for (String partnerName : partnerNames) {
                returnedRefs.add(partnerRefs.getOrPut(outputObjectsName, partnerName));

                // Adding special case where objects partner their own collection
                if (partnerName.equals(inputObjectsName))
                    returnedRefs.add(partnerRefs.getOrPut(outputObjectsName, outputObjectsName));
            }

        }

        return returnedRefs;

    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_OBJECTS).setDescription(
                "Objects to be projected into the xy-plane.  These are related as a parent of their respective projected object.");

        parameters.get(OUTPUT_OBJECTS).setDescription(
                "Output projected objects to be stored in the workspace.  These are related as children of the respective input object.");

    }
}
