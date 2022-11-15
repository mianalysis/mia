package io.github.mianalysis.mia.module.objects.filter;

import java.util.Iterator;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.Module;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.ChildObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.system.Status;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class FilterByChildren extends AbstractNumericObjectFilter {
    public static final String CHILD_OBJECTS = "Child objects";

    public FilterByChildren(Modules modules) {
        super("Number of children", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_FILTER;
    }

    @Override
    public String getDescription() {
        return "Filter an object collection based on the number of children each object has from another object collection.  The threshold (reference) value can be either a fixed value (same for all objects), a measurement associated with an image (same for all objects within a single analysis run) or a measurement associated with a parent object (potentially different for all objects).  Objects which satisfy the specified numeric filter (less than, equal to, greater than, etc.) can be removed from the input collection, moved to another collection (and removed from the input collection) or simply counted (but retained in the input collection).  The number of objects failing the filter can be stored as a metadata value.";
    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        Objs inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        String filterMode = parameters.getValue(FILTER_MODE, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS, workspace);
        String filterMethod = parameters.getValue(FILTER_METHOD, workspace);
        String childObjectsName = parameters.getValue(CHILD_OBJECTS, workspace);
        boolean storeSummary = parameters.getValue(STORE_SUMMARY_RESULTS, workspace);
        boolean storeIndividual = parameters.getValue(STORE_INDIVIDUAL_RESULTS, workspace);

        boolean moveObjects = filterMode.equals(FilterModes.MOVE_FILTERED);
        boolean remove = !filterMode.equals(FilterModes.DO_NOTHING);

        Objs outputObjects = moveObjects ? new Objs(outputObjectsName, inputObjects) : null;

        int count = 0;
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();
            Objs childObjects = inputObject.getChildren(childObjectsName);

            // Removing the object if it has no children
            if (childObjects == null) {
                count++;
                if (remove)
                    processRemoval(inputObject, outputObjects, iterator);
                continue;
            }

            double value = childObjects.size();
            double refValue = getReferenceValue(workspace, inputObject);
            boolean conditionMet = testFilter(value, refValue, filterMethod);

            // Adding measurements
            if (storeIndividual) {
                String measurementName = getIndividualMeasurementName(childObjectsName, workspace);
                inputObject.addMeasurement(new Measurement(measurementName, conditionMet ? 1 : 0));
            }

            // Removing the object if it has too few children
            if (conditionMet) {
                count++;
                if (remove)
                    processRemoval(inputObject, outputObjects, iterator);
            }
        }

        // If moving objects, addRef them to the workspace
        if (moveObjects)
            workspace.addObjects(outputObjects);

        // If storing the result, create a new metadata item for it
        if (storeSummary) {
            String metadataName = getSummaryMeasurementName(childObjectsName, workspace);
            workspace.getMetadata().put(metadataName, count);
        }

        // Showing objects
        if (showOutput)
            inputObjects.convertToImageIDColours().showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new ChildObjectsP(CHILD_OBJECTS, this));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();
        returnedParameters.addAll(super.updateAndGetParameters());

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        returnedParameters.add(parameters.getParameter(CHILD_OBJECTS));
        ((ChildObjectsP) parameters.getParameter(CHILD_OBJECTS)).setParentObjectsName(inputObjectsName);

        returnedParameters.addAll(updateAndGetMeasurementParameters());

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        Workspace workspace = null;
        ObjMeasurementRefs returnedRefs = super.updateAndGetObjectMeasurementRefs();

        if ((boolean) parameters.getValue(STORE_INDIVIDUAL_RESULTS, workspace)) {
            String childObjectsName = parameters.getValue(CHILD_OBJECTS, workspace);
            String measurementName = getIndividualMeasurementName(childObjectsName, null);
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);

            returnedRefs.add(new ObjMeasurementRef(measurementName, inputObjectsName));
            if (parameters.getValue(FILTER_MODE, workspace).equals(FilterModes.MOVE_FILTERED)) {
                String outputObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS, workspace);
                returnedRefs.add(new ObjMeasurementRef(measurementName, outputObjectsName));
            }
        }

        return returnedRefs;

    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        Workspace workspace = null;
        MetadataRefs returnedRefs = new MetadataRefs();

        // Filter results are stored as a metadata item since they apply to the whole
        // set
        if ((boolean) parameters.getValue(STORE_SUMMARY_RESULTS, workspace)) {
            String childObjectsName = parameters.getValue(CHILD_OBJECTS, workspace);
            String metadataName = getSummaryMeasurementName(childObjectsName, null);

            returnedRefs.add(metadataRefs.getOrPut(metadataName));

        }

        return returnedRefs;
    }

    @Override
    protected void addParameterDescriptions() {
        super.addParameterDescriptions();

        parameters.get(CHILD_OBJECTS).setDescription(
                "Objects will be filtered against the number of children they have from this object collection.");

    }
}
