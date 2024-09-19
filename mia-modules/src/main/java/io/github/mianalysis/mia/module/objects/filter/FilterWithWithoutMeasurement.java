package io.github.mianalysis.mia.module.objects.filter;

import java.util.Iterator;

import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.Module;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Filter an object collection based on the presence of a specific measurement
 * for each object. Objects which do/don't have the relevant measurement can be
 * removed from the input collection, moved to another collection (and removed
 * from the input collection) or simply counted (but retained in the input
 * collection). The number of objects failing the filter can be stored as a
 * metadata value.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class FilterWithWithoutMeasurement extends AbstractObjectFilter {

    /**
    * 
    */
    public static final String FILTER_SEPARATOR = "Object filtering";

    /**
     * Controls whether objects are removed when a specific measurement is present
     * or not:<br>
     * <br>
     * - "Remove objects without measurement" Objects without the measurement
     * specified by "Measurement to filter on" are removed, counted or moved
     * (depending on the "Filter mode" parameter).<br>
     * <br>
     * - "Remove objects with measurement" Objects with the measurement specified by
     * "Measurement to filter on" are removed, counted or moved (depending on the
     * "Filter mode" parameter).<br>
     */
    public static final String FILTER_METHOD = "Method for filtering";

    /**
     * Measurement to filter by. The presence or absence of this measurement will
     * determine which of the input objects are counted, removed or moved (depending
     * on the "Filter mode" parameter).
     */
    public static final String MEASUREMENT = "Measurement to filter on";

    /**
     * When selected, the number of removed (or moved) objects is counted and stored
     * as a metadata item (name in the format "FILTER // NUM_[inputObjectsName]
     * WITHOUT [measurementName] MEASUREMENT").
     */
    public static final String STORE_RESULTS = "Store filter results";

    public FilterWithWithoutMeasurement(Modules modules) {
        super("With / without measurement", modules);
    }

    public interface FilterMethods {
        String WITH_MEASUREMENT = "Remove objects with measurement";
        String WITHOUT_MEASUREMENT = "Remove objects without measurement";

        String[] ALL = new String[] { WITH_MEASUREMENT, WITHOUT_MEASUREMENT };

    }

    public String getMetadataName(String inputObjectsName, String filterMethod, String measName) {
        switch (filterMethod) {
            case FilterMethods.WITH_MEASUREMENT:
                return "FILTER // NUM_" + inputObjectsName + " WITH " + measName + " MEASUREMENT";
            case FilterMethods.WITHOUT_MEASUREMENT:
                return "FILTER // NUM_" + inputObjectsName + " WITHOUT " + measName + " MEASUREMENT";
            default:
                return "";
        }
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
        return "Filter an object collection based on the presence of a specific measurement for each object.  Objects which do/don't have the relevant measurement can be removed from the input collection, moved to another collection (and removed from the input collection) or simply counted (but retained in the input collection).  The number of objects failing the filter can be stored as a metadata value.";
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
        String measName = parameters.getValue(MEASUREMENT, workspace);
        boolean storeResults = parameters.getValue(STORE_RESULTS, workspace);

        boolean moveObjects = filterMode.equals(FilterModes.MOVE_FILTERED);
        boolean remove = !filterMode.equals(FilterModes.DO_NOTHING);

        Objs outputObjects = moveObjects ? new Objs(outputObjectsName, inputObjects) : null;

        int count = 0;
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();

            // Removing the object if it has no children
            Measurement measurement = inputObject.getMeasurement(measName);
            switch (filterMethod) {
                case FilterMethods.WITHOUT_MEASUREMENT:
                    if (measurement == null || Double.isNaN(measurement.getValue())) {
                        count++;
                        if (remove)
                            processRemoval(inputObject, outputObjects, iterator);
                    }
                    break;

                case FilterMethods.WITH_MEASUREMENT:
                    if (measurement != null && !Double.isNaN(measurement.getValue())) {
                        count++;
                        if (remove)
                            processRemoval(inputObject, outputObjects, iterator);
                    }
                    break;
            }

        }

        // If moving objects, addRef them to the workspace
        if (moveObjects)
            workspace.addObjects(outputObjects);

        // If storing the result, create a new metadata item for it
        if (storeResults) {
            String metadataName = getMetadataName(inputObjectsName, filterMethod, measName);
            workspace.getMetadata().put(metadataName, count);
        }

        // Showing objects
        if (showOutput)
            inputObjects.convertToImageIDColours().show(false);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(FILTER_SEPARATOR, this));
        parameters.add(new ChoiceP(FILTER_METHOD, this, FilterMethods.WITHOUT_MEASUREMENT, FilterMethods.ALL));
        parameters.add(new ObjectMeasurementP(MEASUREMENT, this));
        parameters.add(new BooleanP(STORE_RESULTS, this, false));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);

        Parameters returnedParameters = new Parameters();
        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(FILTER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(FILTER_METHOD));
        returnedParameters.add(parameters.getParameter(MEASUREMENT));
        ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT)).setObjectName(inputObjectsName);

        returnedParameters.add(parameters.getParameter(STORE_RESULTS));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        return super.updateAndGetObjectMeasurementRefs();

    }

    @Override
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
        return super.updateAndGetObjectMetadataRefs();
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        Workspace workspace = null;
        MetadataRefs returnedRefs = new MetadataRefs();

        // Filter results are stored as a metadata item since they apply to the whole
        // set
        if ((boolean) parameters.getValue(STORE_RESULTS, workspace)) {
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
            String filterMethod = parameters.getValue(FILTER_METHOD, workspace);
            String measName = parameters.getValue(MEASUREMENT, workspace);

            String metadataName = getMetadataName(inputObjectsName, filterMethod, measName);

            returnedRefs.add(metadataRefs.getOrPut(metadataName));

        }

        return returnedRefs;

    }

    @Override
    protected void addParameterDescriptions() {
        super.addParameterDescriptions();

        parameters.get(FILTER_METHOD).setDescription(
                "Controls whether objects are removed when a specific measurement is present or not:<br>"

                        + "<br>- \"" + FilterMethods.WITHOUT_MEASUREMENT
                        + "\" Objects without the measurement specified by \"" + MEASUREMENT
                        + "\" are removed, counted or moved (depending on the \"" + FILTER_MODE + "\" parameter).<br>"

                        + "<br>- \"" + FilterMethods.WITH_MEASUREMENT
                        + "\" Objects with the measurement specified by \"" + MEASUREMENT
                        + "\" are removed, counted or moved (depending on the \"" + FILTER_MODE + "\" parameter).<br>"

        );

        parameters.get(MEASUREMENT).setDescription(
                "Measurement to filter by.  The presence or absence of this measurement will determine which of the input objects are counted, removed or moved (depending on the \""
                        + FILTER_MODE + "\" parameter).");

        String metadataName = getMetadataName("[inputObjectsName]", FilterMethods.WITHOUT_MEASUREMENT,
                "[measurementName]");
        parameters.get(STORE_RESULTS).setDescription(
                "When selected, the number of removed (or moved) objects is counted and stored as a metadata item (name in the format \""
                        + metadataName + "\").");

    }
}
