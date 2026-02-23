package io.github.mianalysis.mia.module.objects.transform;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModulesI;
import io.github.mianalysis.mia.object.ObjsFactories;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeI;
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
import io.github.mianalysis.mia.process.exceptions.IntegerOverflowException;



/**
* 
*/
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class ProjectObjects extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Object input/output";

	/**
	* Objects to be projected into the xy-plane.  These are related as a parent of their respective projected object.
	*/
    public static final String INPUT_OBJECTS = "Input objects";

	/**
	* Output projected objects to be stored in the workspace.  These are related as children of the respective input object.
	*/
    public static final String OUTPUT_OBJECTS = "Output objects";

    public ProjectObjects(ModulesI modules) {
        super("Project objects",modules);
    }

    public static ObjI process(ObjI inputObject, ObjsI outputObjects, boolean addRelationship) throws IntegerOverflowException {
        VolumeI projected = inputObject.getProjected();

        ObjI outputObject = outputObjects.createAndAddNewObjectWithID(inputObject.getCoordinateSetFactory(), inputObject.getID());
        outputObject.setCoordinateSet(projected.getCoordinateSet());
        outputObject.setT(inputObject.getT());

        if (addRelationship) {
            outputObject.addParent(inputObject);
            inputObject.addChild(outputObject);
        }

        return outputObject;

    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }
    
    @Override
    public Category getCategory() {
        return Categories.OBJECTS_TRANSFORM;
    }

    @Override
    public Status process(WorkspaceI workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS,workspace);

        ObjsI inputObjects = workspace.getObjects(inputObjectsName);
        ObjsI outputObjects = ObjsFactories.getDefaultFactory().createFromExample(outputObjectsName, inputObjects);
        outputObjects.setNSlices(1);

        for (ObjI inputObject:inputObjects.values()) {
            try {
                process(inputObject,outputObjects, true);
            } catch (IntegerOverflowException e) {
                return Status.FAIL;
            }
        }

        workspace.addObjects(outputObjects);

        // Showing objects
        if (showOutput) outputObjects.convertToImageIDColours().showWithNormalisation(false);

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

        returnedRelationships.add(parentChildRefs.getOrPut(parameters.getValue(INPUT_OBJECTS,workspace),parameters.getValue(OUTPUT_OBJECTS,workspace)));

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
        parameters.get(INPUT_OBJECTS).setDescription("Objects to be projected into the xy-plane.  These are related as a parent of their respective projected object.");

        parameters.get(OUTPUT_OBJECTS).setDescription("Output projected objects to be stored in the workspace.  These are related as children of the respective input object.");

    }
}
