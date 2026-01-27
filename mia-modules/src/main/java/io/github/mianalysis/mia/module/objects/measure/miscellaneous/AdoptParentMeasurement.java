package io.github.mianalysis.mia.module.objects.measure.miscellaneous;

import java.util.LinkedHashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.measurements.MeasurementI;
import io.github.mianalysis.mia.object.measurements.MeasurementFactories;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.ParentObjectsP;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class AdoptParentMeasurement extends Module {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Object input";

    /**
    * 
    */
    public static final String INPUT_OBJECTS = "Input objects";

    /**
    * 
    */
    public static final String PARENT_OBJECT = "Parent objects";

    /**
    * 
    */
    public static final String MEASUREMENT_SEPARATOR = "Measurements";

    /**
    * 
    */
    public static final String ADD_MEASUREMENT = "Add measurement";

    /**
    * 
    */
    public static final String MEASUREMENT = "Measurement";

    public static String getFullName(String parentObjectName, String measurement) {
        return "PARENT_STATS // " + parentObjectName + " // [" + measurement + "]";
    }

    public AdoptParentMeasurement(Modules modules) {
        super("Adopt parent measurement", modules);
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
        return "";
    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting parameters
        String objectName = parameters.getValue(INPUT_OBJECTS, workspace);
        String parentObjectsName = parameters.getValue(PARENT_OBJECT, workspace);
        LinkedHashMap<Integer, Parameters> collections = parameters.getValue(ADD_MEASUREMENT, workspace);

        ObjsI objects = workspace.getObjects(objectName);

        if (objects == null)
            return Status.PASS;

        int count = 0;
        int total = objects.size();
        for (ObjI obj : objects.values()) {
            count++;

            ObjI parentObj = obj.getParent(parentObjectsName);
            if (parentObj == null)
                continue;

            for (Parameters collection : collections.values()) {
                String measurementName = collection.getValue(MEASUREMENT, workspace);
                MeasurementI parentMeasurement = parentObj.getMeasurement(measurementName);
                if (parentMeasurement == null)
                    continue;

                obj.addMeasurement(
                        MeasurementFactories.getDefaultFactory().createMeasurement(getFullName(parentObjectsName, measurementName), parentMeasurement.getValue()));

            }

            writeProgressStatus(count, total, "objects");

        }

        if (showOutput)
            objects.showMeasurements(this, modules);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new ParentObjectsP(PARENT_OBJECT, this));

        parameters.add(new SeparatorP(MEASUREMENT_SEPARATOR, this));
        Parameters collection = new Parameters();
        collection.add(new ObjectMeasurementP(MEASUREMENT, this));
        parameters.add(new ParameterGroup(ADD_MEASUREMENT, this, collection));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(PARENT_OBJECT));

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        ((ParentObjectsP) parameters.get(PARENT_OBJECT)).setChildObjectsName(inputObjectsName);

        returnedParameters.add(parameters.getParameter(MEASUREMENT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ADD_MEASUREMENT));

        String parentObjectsName = parameters.getValue(PARENT_OBJECT, workspace);
        ParameterGroup parameterGroup = parameters.getParameter(ADD_MEASUREMENT);
        for (Parameters collection : parameterGroup.getCollections(true).values())
            ((ObjectMeasurementP) collection.get(MEASUREMENT)).setObjectName(parentObjectsName);

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

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String parentObjectsName = parameters.getValue(PARENT_OBJECT, workspace);

        ParameterGroup parameterGroup = parameters.getParameter(ADD_MEASUREMENT);
        for (Parameters collection : parameterGroup.getCollections(true).values()) {
            String measurementName = collection.getValue(MEASUREMENT, workspace);

            ObjMeasurementRef ref = objectMeasurementRefs.getOrPut(getFullName(parentObjectsName, measurementName));
            ref.setObjectsName(inputObjectsName);
            returnedRefs.add(ref);

        }

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

    }
}
