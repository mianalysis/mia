package io.github.mianalysis.MIA.Module.ObjectProcessing.Identification;

import io.github.mianalysis.MIA.Module.Categories;
import io.github.mianalysis.MIA.Module.Category;
import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Module.Modules;
import io.github.mianalysis.MIA.Object.Obj;
import io.github.mianalysis.MIA.Object.Objs;
import io.github.mianalysis.MIA.Object.Status;
import io.github.mianalysis.MIA.Object.Workspace;
import io.github.mianalysis.MIA.Object.Parameters.InputObjectsP;
import io.github.mianalysis.MIA.Object.Parameters.Parameters;
import io.github.mianalysis.MIA.Object.Parameters.SeparatorP;
import io.github.mianalysis.MIA.Object.Parameters.Objects.OutputObjectsP;
import io.github.mianalysis.MIA.Object.Refs.Collections.ImageMeasurementRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.MetadataRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.ObjMeasurementRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.ParentChildRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.PartnerRefs;
import io.github.sjcross.common.Object.Volume.Volume;

public class GetObjectSurface extends Module {
    public static final String INPUT_SEPARATOR = "Image input, object output";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_OBJECTS = "Output objects";

    public static Obj getSurface(Obj inputObject, Objs outputObjects, boolean assignRelationships) {
        Volume outputVolume = inputObject.getSurface();
        Obj outputObject = outputObjects.createAndAddNewObject(inputObject.getVolumeType());
        outputObject.setCoordinateSet(outputVolume.getCoordinateSet());

        if (assignRelationships) {
            outputObject.addParent(inputObject);
            inputObject.addChild(outputObject);
        }

        return outputObject;

    }

    public GetObjectSurface(Modules modules) {
        super("Get object surface", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECT_PROCESSING_IDENTIFICATION;
    }

    @Override
    public String getDescription() {
        return "Create surface objects for each input object.  Surface coordinates are those with at least one "
                + "non-object neighbouring pixel (using 6-way connectivity).  Surfaces are stored as children of the "
                + "input object.";
    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting parameters
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        Objs inputObjects = workspace.getObjectSet(inputObjectsName);
        Objs outputObjects = new Objs(outputObjectsName, inputObjects);

        for (Obj inputObject : inputObjects.values())
            getSurface(inputObject, outputObjects, true);
            
        workspace.addObjects(outputObjects);

        // Showing objects
        if (showOutput)
            outputObjects.convertToImageRandomColours().showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this, "", "Input objects to extract surface from."));
        parameters.add(
                new OutputObjectsP(OUTPUT_OBJECTS, this, "", "Output surface objects to be stored in the workspace."));

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
        ParentChildRefs returnedRelationships = new ParentChildRefs();

        returnedRelationships
                .add(parentChildRefs.getOrPut(parameters.getValue(INPUT_OBJECTS), parameters.getValue(OUTPUT_OBJECTS)));

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
}
