package io.github.mianalysis.mia.module.objects.relate.mergeobjects;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.ObjsFactories;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjFactories;
import io.github.mianalysis.mia.object.coordinates.ObjI;
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
 * Combines all objects at a single timepoint from a specific object collection
 * into a single object. Due to the fact objects are stored in 3D, there are
 * still separate objects for each timepoint.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class MergeSingleClass extends Module {

    /**
    * 
    */
    public final static String INPUT_SEPARATOR = "Object input";

    /**
     * Input object collection that will have all objects from each timepoint merged
     * into a single object.
     */
    public final static String INPUT_OBJECTS = "Input objects";

    /**
     * Output merged objects (one per input timepoint). These objects will be stored
     * in the workspace and accessible via this name.
     */
    public static final String OUTPUT_OBJECTS = "Output merged objects";

    public MergeSingleClass(Modules modules) {
        super("Merge single class", modules);
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
        return "Combines all objects at a single timepoint from a specific object collection into a single object.  Due to the fact objects are stored in 3D, there are still separate objects for each timepoint.";
    }

    public static ObjsI mergeSingleClass(ObjsI inputObjects, String outputObjectsName) {
        ObjsI outputObjects = ObjsFactories.getDefaultFactory().createFromExampleObjs(outputObjectsName, inputObjects);

        // Iterating over all input objects, adding their coordinates to the relevant
        // object
        for (ObjI inputObject : inputObjects.values()) {
            // Getting the current timepoint instance
            int t = inputObject.getT();

            // If it doesn't already exist, creating a new object for this timepoint. The ID
            // of this object is the timepoint index (numbering starting at 1).
            if (!outputObjects.containsKey(t + 1)) {
                ObjI outputObject = ObjFactories.getDefaultFactory().createObj(outputObjects, t + 1, inputObject);
                outputObject.setT(t);
                outputObjects.add(outputObject);
            }

            // Adding coordinates to this object
            ObjI outputObject = outputObjects.get(t + 1);
            outputObject.getCoordinateSet().addAll(inputObject.getCoordinateSet());

        }

        return outputObjects;

    }

    @Override
    public Status process(WorkspaceI workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        ObjsI inputObjects = workspace.getObjects(inputObjectsName);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);

        // Merging objects
        ObjsI outputObjects = mergeSingleClass(inputObjects, outputObjectsName);

        // Adding objects to workspace
        workspace.addObjects(outputObjects);

        // Showing objects
        if (showOutput)
            outputObjects.convertToImageIDColours().showWithNormalisation(false);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
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
                "Input object collection that will have all objects from each timepoint merged into a single object.");

        parameters.get(OUTPUT_OBJECTS).setDescription(
                "Output merged objects (one per input timepoint).  These objects will be stored in the workspace and accessible via this name.");

    }
}
