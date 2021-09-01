package io.github.mianalysis.MIA.Module.ObjectProcessing.Refinement.MergeObjects;

import io.github.mianalysis.MIA.Module.Categories;
import io.github.mianalysis.MIA.Module.Category;
import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Module.ModuleCollection;
import io.github.mianalysis.MIA.Object.Obj;
import io.github.mianalysis.MIA.Object.ObjCollection;
import io.github.mianalysis.MIA.Object.Status;
import io.github.mianalysis.MIA.Object.Workspace;
import io.github.mianalysis.MIA.Object.Parameters.InputObjectsP;
import io.github.mianalysis.MIA.Object.Parameters.ParameterCollection;
import io.github.mianalysis.MIA.Object.Parameters.SeparatorP;
import io.github.mianalysis.MIA.Object.Parameters.Objects.OutputObjectsP;
import io.github.mianalysis.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.MetadataRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.ParentChildRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.PartnerRefCollection;

public class MergeSingleClass extends Module {
    public final static String INPUT_SEPARATOR = "Object input";
    public final static String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_OBJECTS = "Output merged objects";

    public MergeSingleClass(ModuleCollection modules) {
        super("Merge single class", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECT_PROCESSING_REFINEMENT_MERGE_OBJECTS;
    }

    @Override
    public String getDescription() {
        return "Combines all objects at a single timepoint from a specific object collection into a single object.  Due to the fact objects are stored in 3D, there are still separate objects for each timepoint.";
    }

    public static ObjCollection mergeSingleClass(ObjCollection inputObjects, String outputObjectsName) {
        ObjCollection outputObjects = new ObjCollection(outputObjectsName, inputObjects);
        
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
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        // Merging objects
        ObjCollection outputObjects = mergeSingleClass(inputObjects, outputObjectsName);

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
        parameters.get(INPUT_OBJECTS).setDescription(
                "Input object collection that will have all objects from each timepoint merged into a single object.");

        parameters.get(OUTPUT_OBJECTS).setDescription(
                "Output merged objects (one per input timepoint).  These objects will be stored in the workspace and accessible via this name.");

    }
}
