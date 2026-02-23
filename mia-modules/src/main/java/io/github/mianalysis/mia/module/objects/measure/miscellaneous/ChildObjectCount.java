package io.github.mianalysis.mia.module.objects.measure.miscellaneous;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModulesI;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.measurements.MeasurementI;
import io.github.mianalysis.mia.object.measurements.MeasurementFactories;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChildObjectsP;
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
 * Calculates the number of children from a specific class. Measurements are
 * assigned to all objects in the input collection.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ChildObjectCount extends Module {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Object input";

    /**
     * For each object in this collection the number of associated child objects
     * (from the collection specified by "Child objects") will be calculated. The
     * count is stored as a measurement associated with each input object.
     */
    public static final String INPUT_OBJECTS = "Input objects";

    /**
     * Child objects to be counted.
     */
    public static final String CHILD_OBJECTS = "Child objects";


    public ChildObjectCount(ModulesI modules) {
        super("Child object count", modules);
    }

    public static String getFullName(String childObjectsName) {
        return "COUNT // " + childObjectsName;
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
        return "Calculates the number of children from a specific class.  Measurements are assigned to all objects in the input collection.";
    }

    @Override

    public Status process(WorkspaceI workspace) {
        // Getting input objects
        String objectName = parameters.getValue(INPUT_OBJECTS, workspace);
        String childObjectsName = parameters.getValue(CHILD_OBJECTS, workspace);

        ObjsI objects = workspace.getObjects(objectName);
        String measurementName = getFullName(childObjectsName);

        if (objects == null)
            return Status.PASS;

        for (ObjI obj : objects.values()) {
            ObjsI children = obj.getChildren(childObjectsName);
            int count = children == null ? 0 : children.size();
            obj.addMeasurement(MeasurementFactories.getDefaultFactory().createMeasurement(measurementName, count));
        }

        if (showOutput)
            objects.showMeasurements(this, modules);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new ChildObjectsP(CHILD_OBJECTS, this));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        ((ChildObjectsP) parameters.get(CHILD_OBJECTS)).setParentObjectsName(inputObjectsName);

        return parameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        Workspace workspace = null;
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String childObjectsName = parameters.getValue(CHILD_OBJECTS, workspace);

        String measurementName = getFullName(childObjectsName);

        ObjMeasurementRef ref = objectMeasurementRefs.getOrPut(measurementName);
        ref.setObjectsName(inputObjectsName);
        ref.setDescription("Number of \"" + childObjectsName + "\" child objects associated with this object.");
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
        parameters.get(INPUT_OBJECTS).setDescription(
                "For each object in this collection the number of associated child objects (from the collection specified by \""
                        + CHILD_OBJECTS
                        + "\") will be calculated.  The count is stored as a measurement associated with each input object.");

        parameters.get(CHILD_OBJECTS).setDescription("Child objects to be counted.");

    }
}
