package io.github.mianalysis.MIA.Module.ObjectMeasurements.Miscellaneous;

import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Module.Modules;
import io.github.mianalysis.MIA.Module.Category;
import io.github.mianalysis.MIA.Module.Categories;
import io.github.mianalysis.MIA.Object.Measurements.ChildCountMeasurement;
import io.github.mianalysis.MIA.Object.Obj;
import io.github.mianalysis.MIA.Object.Objs;
import io.github.mianalysis.MIA.Object.Status;
import io.github.mianalysis.MIA.Object.Workspace;
import io.github.mianalysis.MIA.Object.Parameters.ChildObjectsP;
import io.github.mianalysis.MIA.Object.Parameters.InputObjectsP;
import io.github.mianalysis.MIA.Object.Parameters.SeparatorP;
import io.github.mianalysis.MIA.Object.Refs.ObjMeasurementRef;
import io.github.mianalysis.MIA.Object.Refs.Collections.ImageMeasurementRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.MetadataRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.ObjMeasurementRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.ParentChildRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.PartnerRefs;
import io.github.mianalysis.MIA.Object.Parameters.Parameters;

/**
 * Created by sc13967 on 05/05/2017.
 */
public class ChildObjectCount extends Module {
    public static final String INPUT_SEPARATOR = "Object and image input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String CHILD_OBJECTS = "Child objects";

    public ChildObjectCount(Modules modules) {
        super("Child object count", modules);
    }

    public static String getFullName(String childObjectsName) {
        return "COUNT // " + childObjectsName;
    }


    @Override
    public Category getCategory() {
        return Categories.OBJECT_MEASUREMENTS_MISCELLANEOUS;
    }

    @Override
    public String getDescription() {
        return "Calculates the number of children from a specific class.  Measurements are assigned to all objects in the input collection.  Unlike normal measurements, this value is evaluated at the time of use, so should always be up to date.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input objects
        String objectName = parameters.getValue(INPUT_OBJECTS);
        String childObjectsName = parameters.getValue(CHILD_OBJECTS);

        Objs objects = workspace.getObjects().get(objectName);
        String measurementName = getFullName(childObjectsName);

        if (objects == null)
            return Status.PASS;

        for (Obj obj : objects.values())
            obj.addMeasurement(new ChildCountMeasurement(measurementName, obj, childObjectsName));

        if (showOutput)
            objects.showMeasurements(this, modules);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new ChildObjectsP(CHILD_OBJECTS, this));

        addParameterDescriptions();
        
    }

    @Override
    public Parameters updateAndGetParameters() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ((ChildObjectsP) parameters.get(CHILD_OBJECTS)).setParentObjectsName(inputObjectsName);

        return parameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String childObjectsName = parameters.getValue(CHILD_OBJECTS);

        String measurementName = getFullName(childObjectsName);

        ObjMeasurementRef ref = objectMeasurementRefs.getOrPut(measurementName);
        ref.setObjectsName(inputObjectsName);
        ref.setDescription("Number of \""+childObjectsName+"\" child objects associated with this object.");
        returnedRefs.add(ref);

        return returnedRefs;

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
                        + "\") will be calculated.  The count is stored as a measurement associated with each input object.  The measurement is evaluated at the time of access (unlike \"normal\" measurements which have fixed values), so should always be correct.");

        parameters.get(CHILD_OBJECTS).setDescription("Child objects to be counted.");

    }
}