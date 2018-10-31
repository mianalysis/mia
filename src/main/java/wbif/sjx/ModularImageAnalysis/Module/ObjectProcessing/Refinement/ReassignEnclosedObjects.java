package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Refinement;

import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Object.Point;

import java.util.Iterator;
import java.util.TreeSet;

public class ReassignEnclosedObjects extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String TEMPLATE_IMAGE = "Template image";

    public static boolean testEnclosed(Obj object1, Obj object2, Image templateImage) {
        // Expanding the input object by one pixel (i.e. get the first row of pixels outside the object
        String method = object1.is2D() ? ExpandShrinkObjects.Methods.EXPAND_2D : ExpandShrinkObjects.Methods.EXPAND_3D;
        Obj expandedObj = ExpandShrinkObjects.processObject(object1,templateImage,method,2);
        if (expandedObj == null) return false;

        // Getting surface pixels of expanded object
        TreeSet<Point<Integer>> expandedSurface = expandedObj.getSurface();

        // Iterating over all points, seeing if all are present in the test object.  If any point isn't present, then cancel.
        for (Point<Integer> point:expandedSurface) {
            if (!object2.containsPoint(point)) return false;
        }

        return true;

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
    protected void run(Workspace workspace) throws GenericMIAException {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        String templateImageName = parameters.getValue(TEMPLATE_IMAGE);
        Image templateImage = parameters.getValue(templateImageName);

        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();

            // Comparing to surrounding objects
            for (Obj testObject:inputObjects.values()) {
                // Only process if they are in the same frame
                if (inputObject.getT() != testObject.getT()) continue;

                // Test for enclosing
                boolean enclosed = testEnclosed(inputObject,testObject,templateImage);

                // If enclosing, add all points from enclosed object to test object, remove
                if (enclosed) {
                    for (Point<Integer> point : inputObject.getPoints()) {
                        testObject.addCoord(point.getX(), point.getY(), point.getZ());
                    }

                    // Removing the input object
                    inputObject.removeRelationships();
                    iterator.remove();

                    // Don't bother testing against any others, as a match has been found
                    break;

                }
            }
        }
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
