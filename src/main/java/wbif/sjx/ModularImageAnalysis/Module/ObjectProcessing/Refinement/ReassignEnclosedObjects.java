//package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Refinement;
//
//import ij.ImagePlus;
//import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
//import wbif.sjx.ModularImageAnalysis.Module.Module;
//import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
//import wbif.sjx.ModularImageAnalysis.Object.*;
//import wbif.sjx.common.Object.Point;
//
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.TreeSet;
//
//public class ReassignEnclosedObjects extends Module {
//    public static final String INPUT_OBJECTS = "Input objects";
//    public static final String TEMPLATE_IMAGE = "Template image";
//
//    public static Obj getEnclosingObject(Obj object1, ObjCollection objects2, Image templateImage) {
//        String colourMode = ObjCollection.ColourModes.ID;
//        HashMap<Integer, Float> hues = objects2.getHues(colourMode, "", false);
//        Image labelledImage = objects2.convertObjectsToImage("Labelled",templateImage,colourMode,hues);
//
//        // Testing if all points on the surface are within the same object (same ID)
//        TreeSet<Point<Integer>> points = object1.getSurface();
//        HashSet<Integer> IDs = new HashSet<>();
//
//        // Iterating over all points, seeing if all are present in the test object.  If any point isn't present, then cancel.
//        ImagePlus labelledIpl = labelledImage.getImagePlus();
//        for (Point<Integer> point:points) {
//            labelledIpl.setPosition(1,point.getZ()+1,object1.getT()+1);
//            IDs.add(labelledIpl.getProcessor().getPixel(point.getX(),point.getY()));
//
//            // If the size of the ID HashMap exceeds 1 the object isn't inside a single object
//            if (IDs.size() > 1) return null;
//
//        }
//
//        return objects2.get(IDs.iterator().next());
//
//    }
//
//    @Override
//    public String getTitle() {
//        return "Reassign enclosed objects";
//    }
//
//    @Override
//    public String getPackageName() {
//        return PackageNames.OBJECT_PROCESSING_REFINEMENT;
//    }
//
//    @Override
//    public String getHelp() {
//        return "Objects entirely enclosed by another are reassigned as being the enclosing objects";
//    }
//
//    @Override
//    protected void run(Workspace workspace) throws GenericMIAException {
//        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
//        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);
//
//        String templateImageName = parameters.getValue(TEMPLATE_IMAGE);
//        Image templateImage = workspace.getImage(templateImageName);
//
//        int count = 0;
//        int total = inputObjects.size();
//
//        Iterator<Obj> iterator = inputObjects.values().iterator();
//        while (iterator.hasNext()) {
//            System.err.println(count++);
//            Obj inputObject = iterator.next();
//
//            // Test for enclosing
//            Obj enclosingObj = getEnclosingObject(inputObject, inputObjects, templateImage);
//
//            // If enclosing, add all points from enclosed object to test object, remove
//            if (enclosingObj != null) {
//                for (Point<Integer> point : inputObject.getPoints()) {
//                    enclosingObj.addCoord(point.getX(), point.getY(), point.getZ());
//                }
//
//                // Removing the input object
//                inputObject.removeRelationships();
//                iterator.remove();
//
//                // Don't bother testing against any others, as a match has been found
//                break;
//
//            }
//        }
//    }
//
//    @Override
//    protected void initialiseParameters() {
//        parameters.add(new Parameter(INPUT_OBJECTS,Parameter.INPUT_OBJECTS,null));
//        parameters.add(new Parameter(TEMPLATE_IMAGE,Parameter.INPUT_IMAGE,null));
//    }
//
//    @Override
//    public ParameterCollection updateAndGetParameters() {
//        return parameters;
//    }
//
//    @Override
//    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
//        return null;
//    }
//
//    @Override
//    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
//        return null;
//    }
//
//    @Override
//    public MetadataReferenceCollection updateAndGetMetadataReferences() {
//        return null;
//    }
//
//    @Override
//    public void addRelationships(RelationshipCollection relationships) {
//
//    }
//}
