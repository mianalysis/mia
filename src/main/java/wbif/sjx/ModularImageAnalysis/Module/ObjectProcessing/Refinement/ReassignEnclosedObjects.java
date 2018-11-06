package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Refinement;

import ij.ImagePlus;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.Binary.BinaryOperations2D;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Object.Point;

public class ReassignEnclosedObjects extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String TEMPLATE_IMAGE = "Template image";

    public void testEncloses(ObjCollection objects, Image templateImage) {
        int count = 0;
        int total = objects.size();

        for (Obj object:objects.values()) {
            // Creating a binary image of the input object
            Image binaryImage = object.convertObjToImage("Binary",templateImage);
            ImagePlus binaryIpl = binaryImage.getImagePlus();

            // Filling holes in the binary image
            InvertIntensity.process(binaryIpl);
            BinaryOperations2D.process(binaryIpl,BinaryOperations2D.OperationModes.FILL_HOLES,1);

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

                for (Point<Integer> point : expanded.getPoints()) {
                    object.addCoord(point.getX(), point.getY(), point.getZ());
                }

                // Removing the test object
                testObject.removeRelationships();
                testObject.setID(-1);

            }
        }

        objects.values().removeIf(object -> object.getID() == -1);

    }

    @Override
    public String getTitle() {
        return "Reassign enclosed objects";
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_REFINEMENT;
    }

    @Override
    public String getHelp() {
        return "Objects entirely enclosed by another are reassigned as being the enclosing objects";
    }

    @Override
    protected void run(Workspace workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        String templateImageName = parameters.getValue(TEMPLATE_IMAGE);
        Image templateImage = workspace.getImage(templateImageName);

        testEncloses(inputObjects, templateImage);

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS,Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(TEMPLATE_IMAGE,Parameter.INPUT_IMAGE,null));
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
    public MetadataReferenceCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
