package io.github.mianalysis.mia.module.objects.filter;

import java.util.Iterator;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.ObjMetadata;
import io.github.mianalysis.mia.object.ObjsFactories;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.ObjectMetadataP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Filter an object collection based on a measurement value associated with this
 * object. The threshold (reference) value can be either a fixed value (same for
 * all objects), a measurement associated with an image (same for all objects
 * within a single analysis run) or a measurement associated with a parent
 * object (potentially different for all objects). Objects which satisfy the
 * specified numeric filter (less than, equal to, greater than, etc.) can be
 * removed from the input collection, moved to another collection (and removed
 * from the input collection) or simply counted (but retained in the input
 * collection). The number of objects failing the filter can be stored as a
 * metadata value.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class FilterByMetadata extends AbstractTextObjectFilter {

    public static final String METADATA_VALUE = "Metadata to filter on";

    public FilterByMetadata(Modules modules) {
        super("Based on metadata", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_FILTER;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Filter an object collection based on a metadata value associated with this object.";
    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        ObjsI inputObjects = workspace.getObjects(inputObjectsName);

        // Getting parameters
        String filterMode = parameters.getValue(FILTER_MODE, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS, workspace);
        String filterMethod = parameters.getValue(FILTER_METHOD, workspace);
        String metadataName = parameters.getValue(METADATA_VALUE, workspace);
        String refValue = parameters.getValue(REFERENCE_VALUE, workspace);
        boolean storeSummary = parameters.getValue(STORE_SUMMARY_RESULTS, workspace);
        boolean storeIndividual = parameters.getValue(STORE_INDIVIDUAL_RESULTS, workspace);

        boolean moveObjects = filterMode.equals(FilterModes.MOVE_FILTERED);
        boolean remove = !filterMode.equals(FilterModes.DO_NOTHING);

        ObjsI outputObjects = moveObjects ? ObjsFactories.getDefaultFactory().createFromExampleObjs(outputObjectsName, inputObjects) : null;

        int count = 0;
        Iterator<ObjI> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            ObjI inputObject = iterator.next();

            // Skipping this object if it doesn't have the measurement
            ObjMetadata metadata = inputObject.getMetadataItem(metadataName);
            if (metadata == null)
                continue;

            String value = metadata.getValue();

            // Checking the main filter
            boolean conditionMet = testFilter(value, refValue, filterMethod);

            // Adding measurements
            if (storeIndividual) {
                String measurementName = getIndividualFullName(filterMethod, metadataName, refValue);
                inputObject.addMeasurement(new Measurement(measurementName, conditionMet ? 1 : 0));
            }

            if (conditionMet) {
                count++;
                if (remove)
                    processRemoval(inputObject, outputObjects, iterator);
            }
        }

        // If moving objects, add them to the workspace
        if (moveObjects)
            workspace.addObjects(outputObjects);

        // If storing the result, create a new metadata item for it
        if (storeSummary) {
            String measurementName = getSummaryFullName(inputObjectsName, filterMethod, metadataName, refValue);
            workspace.getMetadata().put(measurementName, count);
        }

        // Showing objects
        if (showOutput) {
            inputObjects.convertToImageIDColours().showWithNormalisation(false);
            if (moveObjects && outputObjects != null)
                outputObjects.convertToImageIDColours().showWithNormalisation(false);

        }

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new ObjectMetadataP(METADATA_VALUE, this));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();
        returnedParameters.addAll(super.updateAndGetParameters());

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        returnedParameters.add(parameters.getParameter(METADATA_VALUE));
        ((ObjectMetadataP) parameters.getParameter(METADATA_VALUE)).setObjectName(inputObjectsName);

        returnedParameters.addAll(updateAndGetMeasurementParameters());

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        WorkspaceI workspace = null;
        ObjMeasurementRefs returnedRefs = super.updateAndGetObjectMeasurementRefs();

        if ((boolean) parameters.getValue(STORE_INDIVIDUAL_RESULTS, workspace)) {
            String metadataName = parameters.getValue(METADATA_VALUE, workspace);
            String filterMethod = parameters.getValue(FILTER_METHOD, workspace);
            String refValue = parameters.getValue(REFERENCE_VALUE, workspace);
            String measurementName = getIndividualFullName(filterMethod, metadataName, refValue);
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);

            returnedRefs.add(new ObjMeasurementRef(measurementName, inputObjectsName));
            if (parameters.getValue(FILTER_METHOD, workspace).equals(FilterModes.MOVE_FILTERED)) {
                String outputObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS, workspace);
                returnedRefs.add(new ObjMeasurementRef(measurementName, outputObjectsName));
            }
        }

        return returnedRefs;

    }

    @Override
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
        return super.updateAndGetObjectMetadataRefs();
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        WorkspaceI workspace = null;
        MetadataRefs returnedRefs = new MetadataRefs();

        // Filter results are stored as a metadata item since they apply to the whole
        // set
        if ((boolean) parameters.getValue(STORE_SUMMARY_RESULTS, workspace)) {
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
            String metadataName = parameters.getValue(METADATA_VALUE, workspace);
            String filterMethod = parameters.getValue(FILTER_METHOD, workspace);
            String refValue = parameters.getValue(REFERENCE_VALUE, workspace);
            String measurementName = getSummaryFullName(inputObjectsName, filterMethod, metadataName, refValue);

            returnedRefs.add(metadataRefs.getOrPut(measurementName));

        }

        return returnedRefs;

    }

    @Override
    protected void addParameterDescriptions() {
        super.addParameterDescriptions();

        parameters.get(METADATA_VALUE).setDescription(
                "Objects will be filtered against their value of this measurement.  Objects missing this measurement are not removed; however, they can be removed by using the module \""
                        + new FilterWithWithoutMeasurement(null).getName() + "\".");

    }
}
