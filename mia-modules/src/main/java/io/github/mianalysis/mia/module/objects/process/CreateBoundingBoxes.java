package io.github.mianalysis.mia.module.objects.process;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.coordinates.volume.Volume;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;
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

/**
 * Creates a solid bounding box for each input object.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class CreateBoundingBoxes extends Module {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Image input, object output";

    /**
     * Input objects to extract bounding box from.
     */
    public static final String INPUT_OBJECTS = "Input objects";

    /**
     * Output bounding box objects to be stored in the workspace.
     */
    public static final String OUTPUT_OBJECTS = "Output objects";

    public static Obj getBoundingBox(Obj inputObject, Objs outputObjects, boolean assignRelationships) {
        double[][] extents = inputObject.getExtents(true, false);

        Obj outputObject = outputObjects.createAndAddNewObject(VolumeType.QUADTREE);
        int xMin = (int) Math.floor(extents[0][0]);
        int xMax = (int) Math.ceil(extents[0][1]);
        int yMin = (int) Math.floor(extents[1][0]);
        int yMax = (int) Math.ceil(extents[1][1]);
        int zMin = (int) Math.floor(extents[2][0]);
        int zMax = (int) Math.ceil(extents[2][1]);

        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                for (int z = zMin; z <= zMax; z++) {
                    try {
                        outputObject.add(x, y, z);
                    } catch (PointOutOfRangeException e) {
                    }
                }
            }
        }

        outputObject.setT(inputObject.getT());

        if (assignRelationships) {
            outputObject.addParent(inputObject);
            inputObject.addChild(outputObject);
        }

        return outputObject;

    }

    public CreateBoundingBoxes(Modules modules) {
        super("Create bounding boxes", modules);
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
        return "Creates a solid bounding box for each input object.";
    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting parameters
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);

        Objs inputObjects = workspace.getObjects(inputObjectsName);
        Objs outputObjects = new Objs(outputObjectsName, inputObjects);

        for (Obj inputObject : inputObjects.values())
            getBoundingBox(inputObject, outputObjects, true);

        workspace.addObjects(outputObjects);

        // Showing objects
        if (showOutput)
            outputObjects.convertToImageIDColours().show();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this, "", "Input objects to extract bounding box from."));
        parameters.add(
                new OutputObjectsP(OUTPUT_OBJECTS, this, "",
                        "Output bounding box objects to be stored in the workspace."));

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

        returnedRelationships
                .add(parentChildRefs.getOrPut(parameters.getValue(INPUT_OBJECTS, workspace),
                        parameters.getValue(OUTPUT_OBJECTS, workspace)));

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
