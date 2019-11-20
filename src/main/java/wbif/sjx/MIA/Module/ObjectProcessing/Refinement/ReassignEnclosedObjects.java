package wbif.sjx.MIA.Module.ObjectProcessing.Refinement;

import ij.ImagePlus;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary.BinaryOperations2D;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.Object.Point;
import wbif.sjx.common.Object.Volume.PointOutOfRangeException;

public class ReassignEnclosedObjects extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String TEMPLATE_IMAGE = "Template image";

    public ReassignEnclosedObjects(ModuleCollection modules) {
        super("Reassign enclosed objects",modules);
    }

    public void testEncloses(ObjCollection objects, Image templateImage) throws IntegerOverflowException {
        int count = 0;
        int total = objects.size();

        for (Obj object:objects.values()) {
            // Creating a binary image of the input object
            Image binaryImage = object.convertObjToImage("Binary",templateImage);
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
                Obj expanded = ExpandShrinkObjects.processObject(testObject,templateImage,ExpandShrinkObjects.Methods.EXPAND_2D,1);

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
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_REFINEMENT;
    }

    @Override
    public String getDescription() {
        return "Objects entirely enclosed by another are reassigned as being the enclosing objects";
    }

    @Override
    public boolean process(Workspace workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        String templateImageName = parameters.getValue(TEMPLATE_IMAGE);
        Image templateImage = workspace.getImage(templateImageName);

        try {
            testEncloses(inputObjects, templateImage);
        } catch (IntegerOverflowException e) {
            return false;
        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this));
        parameters.add(new InputImageP(TEMPLATE_IMAGE,this));
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
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}
