package io.github.mianalysis.mia.module.objects.measure.miscellaneous;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModulesI;
import io.github.mianalysis.mia.module.Module;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.measurements.MeasurementI;
import io.github.mianalysis.mia.object.measurements.MeasurementFactories;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by sc13967 on 05/05/2017.
 */

/**
* Store object timepoint as a measurement.  Timepoint counting starts at 0 (e.g. the third frame will have a timepoint of 2).
*/
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class ObjectTimepoint extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Object and image input";

	/**
	* Objects from the workspace.  Each object in this collection will have the index of timepoint it's present in stored as a measurement.  Note: Timepoint indexing starts at 0.
	*/
    public static final String INPUT_OBJECTS = "Input objects";

    public ObjectTimepoint(ModulesI modules) {
        super("Object timepoint", modules);
    }


    @Override
    public Category getCategory() {
        return Categories.OBJECTS_MEASURE_MISCELLANEOUS;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Store object timepoint as a measurement.  Timepoint counting starts at 0 (e.g. the third frame will have a timepoint of 2).";
    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting input objects
        String objectName = parameters.getValue(INPUT_OBJECTS,workspace);
        ObjsI objects = workspace.getObjects(objectName);

        if (objects == null)
            return Status.PASS;

        for (ObjI obj : objects.values())
            obj.addMeasurement(MeasurementFactories.getDefaultFactory().createMeasurement("TIMEPOINT", obj.getT()));

        if (showOutput)
            objects.showMeasurements(this, modules);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        
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
WorkspaceI workspace = null;
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);

        ObjMeasurementRef ref = objectMeasurementRefs.getOrPut("TIMEPOINT");
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        return returnedRefs;

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
        parameters.get(INPUT_OBJECTS).setDescription("Objects from the workspace.  Each object in this collection will have the index of timepoint it's present in stored as a measurement.  Note: Timepoint indexing starts at 0.");
    }
}
