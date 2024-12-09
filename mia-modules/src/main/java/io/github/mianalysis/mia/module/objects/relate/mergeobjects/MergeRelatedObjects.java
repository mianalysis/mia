package io.github.mianalysis.mia.module.objects.relate.mergeobjects;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.Module;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.parameters.ChildObjectsP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
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
 * Combine coordinates from related objects into a single object. This module
 * can either add coordinates from all child objects into the associated parent
 * or create entirely new merged objects. New merged objects can either contain
 * just coordinates from child objects, or from the parent and its children. Any
 * duplicate coordinates arising from overlapping child objects will only be
 * stored once.<br>
 * <br>
 * Note: If updating the parent objects, any previously-measured object
 * properties may be invalid (i.e. they are not updated). To update such
 * measurements it's necessary to re-run the relevant measurement modules.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class MergeRelatedObjects extends Module {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Object input";

    /**
     * Input parent objects for merging. If "Output mode" is set to "Merge children
     * into parent" all the coordinates from child objects will be added to this
     * object. However, if operating in "Create new object" mode and "Merge mode" is
     * set to "Merge parents and children", coordinates from parent objects will be
     * added to the new merged objects.
     */
    public static final String PARENT_OBJECTS = "Parent objects";

    /**
     * Child objects of the input parent. If "Output mode" is set to "Merge children
     * into parent" all the coordinates from these objects will be added to their
     * respective parent. However, if operating in "Create new object" coordinates
     * from these objects will be added to the new merged objects.
     */
    public static final String CHILD_OBJECTS = "Child objects";

    /**
    * 
    */
    public static final String OUTPUT_SEPARATOR = "Object output";

    /**
     * Controls where the merged object coordinates are output to:<br>
     * <ul>
     * <li>"Create new object" For each input parent, a new merged object will be
     * created. These merged objects are themselves children of the parent
     * object.</li>
     * <li>"Merge children into parent" Combined coordinates (original coordinates
     * from parent and coordinates of children) are added to this parent object.
     * Note: In this mode the coordinates of the parent object are being updated, so
     * any previously-measured object properties may be invalid (i.e. they are not
     * updated). To update such measurements it's necessary to re-run the relevant
     * measurement modules.</li>
     * </ul>
     */
    public static final String OUTPUT_MODE = "Output mode";

    /**
     * If outputting new merged objects (as opposed to updating the parent), objects
     * will be stored with this reference name.
     */
    public static final String OUTPUT_MERGED_OBJECTS = "Output overlapping objects";

    /**
     * When in "Create new object" mode, this parameter controls what coordinates
     * are added to the new merged objects:
     * <ul>
     * <li>"Merge children only" Only coordinates from child objects are added to
     * the merged object. In this mode, coordinates for the parent are ignored.</li>
     * <li>"Merge parents and children" Coordinates from both the parent and child
     * objects are added to the new merged object.</li>
     * </ul>
     */
    public static final String MERGE_MODE = "Merge mode";

    public interface OutputModes {
        String CREATE_NEW_OBJECT = "Create new object";
        String UPDATE_PARENT = "Merge children into parent";

        String[] ALL = new String[] { CREATE_NEW_OBJECT, UPDATE_PARENT };

    }

    public interface MergeModes {
        String MERGE_CHILDREN_ONLY = "Merge children only";
        String MERGE_PARENTS_AND_CHILDREN = "Merge parents and children";

        String[] ALL = new String[] { MERGE_CHILDREN_ONLY, MERGE_PARENTS_AND_CHILDREN };

    }

    public MergeRelatedObjects(Modules modules) {
        super("Merge related objects", modules);
    }

    public static Objs mergeRelatedObjectsCreateNew(Objs parentObjects, String childObjectsName,
            String relatedObjectsName, String mergeMode) {
        Objs relatedObjects = new Objs(relatedObjectsName, parentObjects);

        if (parentObjects == null)
            return relatedObjects;

        for (Obj parentObj : parentObjects.values()) {
            // Collecting all children for this parent. If none are present, skip to the
            // next parent
            Objs currChildObjects = parentObj.getChildren(childObjectsName);
            if (currChildObjects.size() == 0)
                continue;

            // Creating a new Obj and assigning pixels from the parent and all children
            Obj relatedObject = relatedObjects.createAndAddNewObject(parentObj.getVolumeType());
            relatedObject.setT(parentObj.getT());
            relatedObjects.add(relatedObject);
            parentObj.addChild(relatedObject);
            relatedObject.addParent(parentObj);

            // Transferring points from the child object to the new object
            for (Obj childObject : currChildObjects.values())
                relatedObject.getCoordinateSet().addAll(childObject.getCoordinateSet());

            switch (mergeMode) {
                case MergeModes.MERGE_PARENTS_AND_CHILDREN:
                    relatedObject.getCoordinateSet().addAll(parentObj.getCoordinateSet());
                    break;
            }

        }

        return relatedObjects;

    }

    public static void mergeRelatedObjectsUpdateParent(Objs parentObjects, String childObjectsName,
            String mergeMode) {
        if (parentObjects == null)
            return;

        for (Obj parentObj : parentObjects.values()) {
            // Collecting all children for this parent. If none are present, skip to the
            // next parent
            Objs currChildObjects = parentObj.getChildren(childObjectsName);
            if (currChildObjects.size() == 0)
                continue;

            // Transferring points from the child object to the parent object
            for (Obj childObject : currChildObjects.values())
                parentObj.getCoordinateSet().addAll(childObject.getCoordinateSet());

            // Removing any surfaces/centroids that have been previously calculated
            parentObj.clearCentroid();
            parentObj.clearProjected();
            parentObj.clearSurface();
            parentObj.clearROIs();

        }
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_RELATE_MERGE;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Combine coordinates from related objects into a single object.  This module can either add coordinates from all child objects into the associated parent or create entirely new merged objects.  New merged objects can either contain just coordinates from child objects, or from the parent and its children.  Any duplicate coordinates arising from overlapping child objects will only be stored once.<br><br>Note: If updating the parent objects, any previously-measured object properties may be invalid (i.e. they are not updated).  To update such measurements it's necessary to re-run the relevant measurement modules.";

    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting input objects
        String parentObjectName = parameters.getValue(PARENT_OBJECTS, workspace);
        Objs parentObjects = workspace.getObjects(parentObjectName);

        String childObjectsName = parameters.getValue(CHILD_OBJECTS, workspace);
        String outputMode = parameters.getValue(OUTPUT_MODE, workspace);
        String relatedObjectsName = parameters.getValue(OUTPUT_MERGED_OBJECTS, workspace);
        String mergeMode = parameters.getValue(MERGE_MODE, workspace);

        switch (outputMode) {
            case OutputModes.CREATE_NEW_OBJECT:
                Objs relatedObjects = mergeRelatedObjectsCreateNew(parentObjects, childObjectsName,
                        relatedObjectsName, mergeMode);
                if (relatedObjects == null)
                    return Status.PASS;

                workspace.addObjects(relatedObjects);
                if (showOutput)
                    relatedObjects.convertToImageIDColours().show(false);

                break;

            case OutputModes.UPDATE_PARENT:
                mergeRelatedObjectsUpdateParent(parentObjects, childObjectsName, mergeMode);
                if (showOutput && parentObjects != null)
                    parentObjects.convertToImageIDColours().show(false);
                
                break;
        }
        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(PARENT_OBJECTS, this));
        parameters.add(new ChildObjectsP(CHILD_OBJECTS, this));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new ChoiceP(OUTPUT_MODE, this, OutputModes.CREATE_NEW_OBJECT, OutputModes.ALL));
        parameters.add(new OutputObjectsP(OUTPUT_MERGED_OBJECTS, this));
        parameters.add(new ChoiceP(MERGE_MODE, this, MergeModes.MERGE_CHILDREN_ONLY, MergeModes.ALL));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.get(INPUT_SEPARATOR));
        returnedParameters.add(parameters.get(PARENT_OBJECTS));
        returnedParameters.add(parameters.get(CHILD_OBJECTS));
        ChildObjectsP childObjectsP = parameters.getParameter(CHILD_OBJECTS);
        childObjectsP.setParentObjectsName(parameters.getValue(PARENT_OBJECTS, workspace));

        returnedParameters.add(parameters.get(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.get(OUTPUT_MODE));
        switch ((String) parameters.getValue(OUTPUT_MODE, workspace)) {
            case OutputModes.CREATE_NEW_OBJECT:
                returnedParameters.add(parameters.get(OUTPUT_MERGED_OBJECTS));
                returnedParameters.add(parameters.get(MERGE_MODE));
                break;
        }

        return returnedParameters;

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
        WorkspaceI workspace = null;
        ParentChildRefs returnedRelationships = new ParentChildRefs();

        switch ((String) parameters.getValue(OUTPUT_MODE, workspace)) {
            case OutputModes.CREATE_NEW_OBJECT:
                returnedRelationships.add(parentChildRefs.getOrPut(parameters.getValue(PARENT_OBJECTS, workspace),
                        parameters.getValue(OUTPUT_MERGED_OBJECTS, workspace)));
                break;
        }

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

    void addParameterDescriptions() {
        parameters.get(PARENT_OBJECTS).setDescription("Input parent objects for merging.  If \"" + OUTPUT_MODE
                + "\" is set to \"" + OutputModes.UPDATE_PARENT
                + "\" all the coordinates from child objects will be added to this object.  However, if operating in \""
                + OutputModes.CREATE_NEW_OBJECT + "\" mode and \"" + MERGE_MODE + "\" is set to \""
                + MergeModes.MERGE_PARENTS_AND_CHILDREN
                + "\", coordinates from parent objects will be added to the new merged objects.");

        parameters.get(CHILD_OBJECTS).setDescription("Child objects of the input parent.  If \"" + OUTPUT_MODE
                + "\" is set to \"" + OutputModes.UPDATE_PARENT
                + "\" all the coordinates from these objects will be added to their respective parent.  However, if operating in \""
                + OutputModes.CREATE_NEW_OBJECT
                + "\" coordinates from these objects will be added to the new merged objects.");

        parameters.get(OUTPUT_MODE).setDescription("Controls where the merged object coordinates are output to:<br><ul>"

                + "<li>\"" + OutputModes.CREATE_NEW_OBJECT
                + "\" For each input parent, a new merged object will be created.  These merged objects are themselves children of the parent object.</li>"

                + "<li>\"" + OutputModes.UPDATE_PARENT
                + "\" Combined coordinates (original coordinates from parent and coordinates of children) are added to this parent object.  Note: In this mode the coordinates of the parent object are being updated, so any previously-measured object properties may be invalid (i.e. they are not updated).  To update such measurements it's necessary to re-run the relevant measurement modules.</li></ul>");

        parameters.get(OUTPUT_MERGED_OBJECTS).setDescription(
                "If outputting new merged objects (as opposed to updating the parent), objects will be stored with this reference name.");

        parameters.get(MERGE_MODE).setDescription("When in \"" + OutputModes.CREATE_NEW_OBJECT
                + "\" mode, this parameter controls what coordinates are added to the new merged objects:<ul>"

                + "<li>\"" + MergeModes.MERGE_CHILDREN_ONLY
                + "\" Only coordinates from child objects are added to the merged object.  In this mode, coordinates for the parent are ignored.</li>"

                + "<li>\"" + MergeModes.MERGE_PARENTS_AND_CHILDREN
                + "\" Coordinates from both the parent and child objects are added to the new merged object.</li></ul>");

    }
}
