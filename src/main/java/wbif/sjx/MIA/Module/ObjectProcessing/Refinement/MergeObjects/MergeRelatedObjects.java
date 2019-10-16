package wbif.sjx.MIA.Module.ObjectProcessing.Refinement.MergeObjects;

import ij.ImagePlus;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
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
import wbif.sjx.common.Object.Point;

import java.util.HashMap;
import java.util.Iterator;

public class MergeRelatedObjects extends Module {
    public final static String INPUT_SEPARATOR = "Object input";
    public final static String PARENT_OBJECTS = "Parent objects";
    public final static String CHILD_OBJECTS = "Child objects";

    public final static String OUTPUT_SEPARATOR = "Object output";
    public static final String RELATED_OBJECTS = "Output overlapping objects";

    public static final String MERGE_SEPARATOR = "Merge controls";
    public static final String MERGE_MODE = "Merge mode";

    public interface MergeModes {
        String MERGE_CHILDREN_ONLY = "Merge children only";
        String MERGE_PARENTS_AND_CHILDREN = "Merge parents and children";

        String[] ALL = new String[]{MERGE_CHILDREN_ONLY,MERGE_PARENTS_AND_CHILDREN};

    }

    public MergeRelatedObjects(ModuleCollection modules) {
        super("Merge related objects", modules);
    }


    public ObjCollection mergeRelatedObjects(ObjCollection parentObjects, ObjCollection childObjects, String relatedObjectsName, String mergeMode) {
        Obj exampleParent = parentObjects.getFirst();
        ObjCollection relatedObjects = new ObjCollection(relatedObjectsName);

        if (exampleParent == null) return relatedObjects;

        for (Obj parentObj:parentObjects.values()) {
            // Collecting all children for this parent.  If none are present, skip to the next parent
            ObjCollection currChildObjects = parentObj.getChildren(childObjects.getName());
            if (currChildObjects.size() == 0) continue;

            // Creating a new Obj and assigning pixels from the parent and all children
            Obj relatedObject = new Obj(relatedObjectsName,relatedObjects.getAndIncrementID(),exampleParent);
            relatedObject.setT(parentObj.getT());
            relatedObjects.add(relatedObject);

            for (Obj childObject:currChildObjects.values()) {
                // Transferring points from the child object to the new object
                relatedObject.getCoordinateSet().addAll(childObject.getCoordinateSet());

            }

            switch (mergeMode) {
                case MergeModes.MERGE_PARENTS_AND_CHILDREN:
                    relatedObject.getCoordinateSet().addAll(parentObj.getCoordinateSet());
                    break;
            }
        }

        return relatedObjects;

    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_REFINEMENT_MERGE_OBJECTS;
    }

    @Override
    protected boolean process(Workspace workspace) {
        // Getting input objects
        String parentObjectName = parameters.getValue(PARENT_OBJECTS);
        ObjCollection parentObjects = workspace.getObjects().get(parentObjectName);

        String childObjectName = parameters.getValue(CHILD_OBJECTS);
        ObjCollection childObjects = workspace.getObjects().get(childObjectName);

        String relatedObjectsName = parameters.getValue(RELATED_OBJECTS);
        String mergeMode = parameters.getValue(MERGE_MODE);

        ObjCollection relatedObjects = mergeRelatedObjects(parentObjects,childObjects,relatedObjectsName,mergeMode);
        if (relatedObjects != null) {
            workspace.addObjects(relatedObjects);

            // Showing objects
            if (showOutput) relatedObjects.convertToImageRandomColours().showImage();

        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputObjectsP(PARENT_OBJECTS, this));
        parameters.add(new ChildObjectsP(CHILD_OBJECTS, this));

        parameters.add(new ParamSeparatorP(OUTPUT_SEPARATOR,this));
        parameters.add(new OutputObjectsP(RELATED_OBJECTS,this));

        parameters.add(new ParamSeparatorP(MERGE_SEPARATOR,this));
        parameters.add(new ChoiceP(MERGE_MODE,this,MergeModes.MERGE_CHILDREN_ONLY,MergeModes.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ChildObjectsP childObjectsP = parameters.getParameter(CHILD_OBJECTS);
        childObjectsP.setParentObjectsName(parameters.getValue(PARENT_OBJECTS));

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
