package wbif.sjx.MIA.Module.ObjectProcessing.Refinement;

import ij.ImagePlus;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ObjectMeasurements.Spatial.RelateObjects;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Process.ColourFactory;
import wbif.sjx.common.Object.LUTs;

import java.util.HashMap;
import java.util.Iterator;

public class MergeRelatedObjects extends Module {
    public final static String INPUT_SEPARATOR = "Object input";
    public final static String PARENT_OBJECTS = "Parent objects";
    public final static String CHILD_OBJECTS = "Child objects";

    public final static String OUTPUT_SEPARATOR = "Object output";
    public static final String RELATED_OBJECTS = "Output overlapping objects";


    public MergeRelatedObjects(ModuleCollection modules) {
        super("Merge related objects", modules);
    }


    public ObjCollection mergeRelatedObjects(ObjCollection parentObjects, ObjCollection childObjects, String relatedObjectsName) {
        Obj exampleParent = parentObjects.getFirst();
        ObjCollection relatedObjects = new ObjCollection(relatedObjectsName);

        if (exampleParent == null) return relatedObjects;

        double dppXY = exampleParent.getDistPerPxXY();
        double dppZ = exampleParent.getDistPerPxZ();
        String calibratedUnits = exampleParent.getCalibratedUnits();
        boolean twoD = exampleParent.is2D();

        Iterator<Obj> parentIterator = parentObjects.values().iterator();
        while (parentIterator.hasNext()) {
            Obj parentObj = parentIterator.next();

            // Collecting all children for this parent.  If none are present, skip to the next parent
            ObjCollection currChildObjects = parentObj.getChildren(childObjects.getName());
            if (currChildObjects.size() == 0) continue;

            // Creating a new Obj and assigning pixels from the parent and all children
            Obj relatedObject = new Obj(relatedObjectsName,relatedObjects.getAndIncrementID(),dppXY,dppZ,calibratedUnits,twoD);
            relatedObject.setT(parentObj.getT());
            relatedObjects.add(relatedObject);

            for (Obj childObject:currChildObjects.values()) {
                // Transferring points from the child object to the new object
                relatedObject.getPoints().addAll(childObject.getPoints());

                // Removing the child object from its original collection
                childObjects.values().remove(childObject);

            }

            // Transferring points from the parent object to the new object
            relatedObject.getPoints().addAll(parentObj.getPoints());

            // Removing the parent object from its original collection
            parentIterator.remove();

        }

        return relatedObjects;

    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_REFINEMENT;
    }

    @Override
    protected boolean process(Workspace workspace) {
        // Getting input objects
        String parentObjectName = parameters.getValue(PARENT_OBJECTS);
        ObjCollection parentObjects = workspace.getObjects().get(parentObjectName);

        String childObjectName = parameters.getValue(CHILD_OBJECTS);
        ObjCollection childObjects = workspace.getObjects().get(childObjectName);

        String relatedObjectsName = parameters.getValue(RELATED_OBJECTS);

        ObjCollection relatedObjects = mergeRelatedObjects(parentObjects,childObjects,relatedObjectsName);
        if (relatedObjects != null) {
            workspace.addObjects(relatedObjects);
            // Showing objects
            if (showOutput) {
                HashMap<Integer,Float> hues = ColourFactory.getRandomHues(relatedObjects);
                ImagePlus dispIpl = relatedObjects.convertObjectsToImage("Objects",null,hues,8,false).getImagePlus();
                dispIpl.setLut(LUTs.Random(true));
                dispIpl.setPosition(1,1,1);
                dispIpl.updateChannelAndDraw();
                dispIpl.show();
            }
        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputObjectsP(PARENT_OBJECTS, this));
        parameters.add(new InputObjectsP(CHILD_OBJECTS, this));

        parameters.add(new ParamSeparatorP(OUTPUT_SEPARATOR,this));
        parameters.add(new OutputObjectsP(RELATED_OBJECTS,this));

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

    @Override
    public String getDescription() {
        return "";
    }
}
