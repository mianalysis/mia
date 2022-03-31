package io.github.mianalysis.mia.module.objects.relate.mergeobjects;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.Module;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class MergeSingleClass extends Module {
    public final static String INPUT_SEPARATOR = "Object input";
    public final static String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_OBJECTS = "Output merged objects";

    public MergeSingleClass(Modules modules) {
        super("Merge single class", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_RELATE_MERGE;
    }

    @Override
    public String getDescription() {
        return "Combines all objects at a single timepoint from a specific object collection into a single object.  Due to the fact objects are stored in 3D, there are still separate objects for each timepoint.";
    }

    public static Objs mergeSingleClass(Objs inputObjects, String outputObjectsName) {
        Objs outputObjects = new Objs(outputObjectsName, inputObjects);
        
        // Iterating over all input objects, adding their coordinates to the relevant
        // object
        for (Obj inputObject : inputObjects.values()) {
            // Getting the current timepoint instance
            int t = inputObject.getT();
            
            // If it doesn't already exist, creating a new object for this timepoint. The ID
            // of this object is the timepoint index (numbering starting at 1).
            if (!outputObjects.containsKey(t+1))
                outputObjects.add(new Obj(outputObjects, t + 1, inputObject));
            
            // Adding coordinates to this object
            Obj outputObject = outputObjects.get(t + 1);
            outputObject.getCoordinateSet().addAll(inputObject.getCoordinateSet());
            

        }

        return outputObjects;

    }

    @Override
    protected Status process(Workspace workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        Objs inputObjects = workspace.getObjectSet(inputObjectsName);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        // Merging objects
        Objs outputObjects = mergeSingleClass(inputObjects, outputObjectsName);

        // Adding objects to workspace
        workspace.addObjects(outputObjects);

        // Showing objects
        if (showOutput)
            outputObjects.convertToImageRandomColours().showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
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
                "Input object collection that will have all objects from each timepoint merged into a single object.");

        parameters.get(OUTPUT_OBJECTS).setDescription(
                "Output merged objects (one per input timepoint).  These objects will be stored in the workspace and accessible via this name.");

    }
}
