package wbif.sjx.MIA.Module.ObjectProcessing.Refinement;

import ij.ImagePlus;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary.BinaryOperations2D;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.Object.Point;
import wbif.sjx.common.Object.Volume.PointOutOfRangeException;

public class ReassignEnclosedObjects extends Module {
    public static final String INPUT_OBJECTS = "Input objects";

    public ReassignEnclosedObjects(ModuleCollection modules) {
        super("Reassign enclosed objects",modules);
    }

    public static void testEnclosed(ObjCollection objects) throws IntegerOverflowException {
        for (Obj object:objects.values()) {
            // If this object has already been removed (i.e. ID = -1), skip it
            if (object.getID() == -1) continue;

            // Creating a binary image of the input object
            Image binaryImage = object.convertObjToImage("Binary");
            ImagePlus binaryIpl = binaryImage.getImagePlus();

            // Filling holes in the binary image
            InvertIntensity.process(binaryIpl);
            BinaryOperations2D.process(binaryIpl,BinaryOperations2D.OperationModes.FILL_HOLES,1,1);

            // Iterating over each object in the collection, testing if the centroid is present in the filled object
            for (Obj testObject : objects.values()) {
                // We don't want to test against the same object
                if (object.getID() == testObject.getID()) continue;

                // We only want to test against objects in the same frame
                if (object.getT() != testObject.getT()) continue;

                // Getting the object centroid
                int xCent = (int) Math.round(testObject.getXMean(true));
                int yCent = (int) Math.round(testObject.getYMean(true));
                int zCent = (int) Math.round(testObject.getZMean(true, false));

                // Checking if this centroid is within the input object
                binaryIpl.setPosition(1, zCent + 1, testObject.getT() + 1);
                int val = binaryIpl.getProcessor().get(xCent, yCent);

                if (val == 255) continue;

                // Expanding the test object by 1 px to fill the gap
                Obj expanded = ExpandShrinkObjects.processObject(testObject,ExpandShrinkObjects.Methods.EXPAND_2D,1);

                if (expanded == null) expanded = testObject;

                for (Point<Integer> point : expanded.getCoordinateSet()) {
                    try {
                        object.add(point.getX(), point.getY(), point.getZ());
                    } catch (PointOutOfRangeException e) {}
                }

                // Removing the test object
                testObject.removeRelationships();
                testObject.setID(-1);

            }
        }

        objects.values().removeIf(object -> object.getID() == -1);

    }



    @Override
    public Category getCategory() {
        return Categories.OBJECT_PROCESSING_REFINEMENT;
    }

    @Override
    public String getDescription() {
        return "Any objects entirely enclosed by another object in the same collection are reassigned as being part of the enclosing object.  This operation removes the enclosed object as a separate entity.<br><br>Note: MIA objects do not permit duplication of the same coordinate within a single object, so any duplicate coordinates will be ignored (i.e. only one copy will be stored).";
    }

    @Override
    public Status process(Workspace workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        try {
            testEnclosed(inputObjects);
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
        return null;
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
        parameters.get(INPUT_OBJECTS).setDescription("Object collection to process for enclosed objects.  Objects in this collection will be updated as a result of this module (enclosed objects will be removed as separate entities and their coordinates will be added to the enclosing object).");
    }
}
