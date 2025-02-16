package io.github.mianalysis.mia.module.objects.process;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.volume.Volume;
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
* Create surface objects for each input object.  Surface coordinates are those with at least one non-object neighbouring pixel (using 6-way connectivity).  Surfaces are stored as children of the input object.
*/
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class GetObjectSurface extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Image input, object output";

	/**
	* Input objects to extract surface from.
	*/
    public static final String INPUT_OBJECTS = "Input objects";

	/**
	* Output surface objects to be stored in the workspace.
	*/
    public static final String OUTPUT_OBJECTS = "Output objects";

    public static Obj getSurface(Obj inputObject, Objs outputObjects, boolean assignRelationships) {
        Volume outputVolume = inputObject.getSurface();
        Obj outputObject = outputObjects.createAndAddNewObject(inputObject.getCoordinateSetFactory());
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
        return Categories.OBJECTS_PROCESS;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Create surface objects for each input object.  Surface coordinates are those with at least one "
                + "non-object neighbouring pixel (using 6-way connectivity).  Surfaces are stored as children of the "
                + "input object.";
    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting parameters
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS,workspace);

        Objs inputObjects = workspace.getObjects(inputObjectsName);
        Objs outputObjects = new Objs(outputObjectsName, inputObjects);

        for (Obj inputObject : inputObjects.values())
            getSurface(inputObject, outputObjects, true);
            
        workspace.addObjects(outputObjects);

        // Showing objects
        if (showOutput)
            outputObjects.convertToImageIDColours().show(false);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this, "", "Input objects to extract surface from."));
        parameters.add(
                new OutputObjectsP(OUTPUT_OBJECTS, this, "", "Output surface objects to be stored in the workspace."));

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
WorkspaceI workspace = null;
        ParentChildRefs returnedRelationships = new ParentChildRefs();

        returnedRelationships
                .add(parentChildRefs.getOrPut(parameters.getValue(INPUT_OBJECTS,workspace), parameters.getValue(OUTPUT_OBJECTS,workspace)));

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
