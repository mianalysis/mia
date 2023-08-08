package io.github.mianalysis.mia.module.objects.transform;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.InvertIntensity;
import io.github.mianalysis.mia.module.images.process.binary.BinaryOperations2D;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.exceptions.IntegerOverflowException;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;


/**
* Any objects entirely enclosed by another object in the same collection are reassigned as being part of the enclosing object.  This operation removes the enclosed object as a separate entity.<br><br>Note: MIA objects do not permit duplication of the same coordinate within a single object, so any duplicate coordinates will be ignored (i.e. only one copy will be stored).
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ReassignEnclosedObjects extends Module {

	/**
	* Object collection to process for enclosed objects.  Objects in this collection will be updated as a result of this module (enclosed objects will be removed as separate entities and their coordinates will be added to the enclosing object).
	*/
    public static final String INPUT_OBJECTS = "Input objects";

    public ReassignEnclosedObjects(Modules modules) {
        super("Reassign enclosed objects", modules);
    }

    public static void testEnclosed(Obj object, Objs objects) throws IntegerOverflowException {
        // If this object has already been removed (i.e. ID = -1), skip it
        if (object.getID() == -1)
            return;

        // Creating a binary image of the input object
        Image binaryImage = object.getAsImage("Binary",false);
        ImagePlus binaryIpl = binaryImage.getImagePlus();

        // Filling holes in the binary image
        InvertIntensity.process(binaryIpl);
        BinaryOperations2D.process(binaryIpl,BinaryOperations2D.OperationModes.FILL_HOLES,1,1,
        false);

        // Iterating over each object in the collection, testing if the centroid is
        // present in the filled object
        for (Obj testObject : objects.values()) {
            // We don't want to test against the same object
            if (object.getID() == testObject.getID())
                continue;

            // We only want to test against objects in the same frame
            if (object.getT() != testObject.getT())
                continue;

            // Getting the object centroid
            int xCent = (int) Math.round(testObject.getXMean(true));
            int yCent = (int) Math.round(testObject.getYMean(true));
            int zCent = (int) Math.round(testObject.getZMean(true, false));

            // Checking if this centroid is within the input object
            binaryIpl.setPosition(1, zCent + 1, testObject.getT() + 1);
            int val = binaryIpl.getProcessor().get(xCent, yCent);
            if (val == 255) continue;


            // Expanding the test object by 1 px to fill the gap
            // Obj expanded = ExpandShrinkObjects.processObject(testObject, ExpandShrinkObjects.Methods.EXPAND_2D, 1);

            // if (expanded == null)
            //     expanded = testObject;

            for (Point<Integer> point : testObject.getCoordinateSet()) {
                try {
                    object.add(point.getX(), point.getY(), point.getZ());
                } catch (PointOutOfRangeException e) {
                }
            }

            // Removing the test object
            testObject.removeRelationships();
            testObject.setID(-1);

        }
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_TRANSFORM;
    }

    @Override
    public String getDescription() {
        return "Any objects entirely enclosed by another object in the same collection are reassigned as being part of the enclosing object.  This operation removes the enclosed object as a separate entity.<br><br>Note: MIA objects do not permit duplication of the same coordinate within a single object, so any duplicate coordinates will be ignored (i.e. only one copy will be stored).";
    }

    @Override
    public Status process(Workspace workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        Objs inputObjects = workspace.getObjects(inputObjectsName);

        int count = 0;
        try {
            for (Obj object : inputObjects.values()) {
                testEnclosed(object, inputObjects);
                writeProgressStatus(++count, inputObjects.size(), "objects");
            }

            inputObjects.values().removeIf(object -> object.getID() == -1);

        } catch (IntegerOverflowException e) {
            return Status.FAIL;
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
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
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_OBJECTS).setDescription(
                "Object collection to process for enclosed objects.  Objects in this collection will be updated as a result of this module (enclosed objects will be removed as separate entities and their coordinates will be added to the enclosing object).");
    }
}
