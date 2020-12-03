package wbif.sjx.MIA.Module.ObjectProcessing.Refinement.FilterObjects;

import java.util.Iterator;

import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.PartnerObjectsP;
import wbif.sjx.MIA.Object.References.ObjMeasurementRef;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;

public class FilterByPartners extends AbstractNumericObjectFilter {
    public static final String PARTNER_OBJECTS = "Partner objects";

    public FilterByPartners(ModuleCollection modules) {
        super("Number of partners", modules);
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_REFINEMENT_FILTER_OBJECTS;
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECT_PROCESSING_REFINEMENT_FILTER_OBJECTS;
    }

    @Override
    public String getDescription() {
        return "Filter an object collection based on the number of partners each object has from another object collection.  The threshold (reference) value can be either a fixed value (same for all objects), a measurement associated with an image (same for all objects within a single analysis run) or a measurement associated with a parent object (potentially different for all objects).  Objects which satisfy the specified numeric filter (less than, equal to, greater than, etc.) can be removed from the input collection, moved to another collection (and removed from the input collection) or simply counted (but retained in the input collection).  The number of objects failing the filter can be stored as a metadata value.";

    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        String filterMode = parameters.getValue(FILTER_MODE);
        String outputObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS);
        String filterMethod = parameters.getValue(FILTER_METHOD);
        String partnerObjectsName = parameters.getValue(PARTNER_OBJECTS);
        boolean storeSummary = parameters.getValue(STORE_SUMMARY_RESULTS);
        boolean storeIndividual = parameters.getValue(STORE_INDIVIDUAL_RESULTS);

        boolean moveObjects = filterMode.equals(FilterModes.MOVE_FILTERED);
        boolean remove = !filterMode.equals(FilterModes.DO_NOTHING);

        ObjCollection outputObjects = moveObjects ? new ObjCollection(outputObjectsName, inputObjects) : null;

        int count = 0;
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();
            ObjCollection partnerObjects = inputObject.getPartners(partnerObjectsName);

            // Removing the object if it has no partners
            if (partnerObjects == null) {
                count++;
                if (remove)
                    processRemoval(inputObject, outputObjects, iterator);
                continue;
            }

            double value = partnerObjects.size();
            double refValue = getReferenceValue(workspace, inputObject);
            boolean conditionMet = testFilter(value, refValue, filterMethod);

            // Adding measurements
            if (storeIndividual) {
                String measurementName = getIndividualMeasurementName(partnerObjectsName);
                inputObject.addMeasurement(new Measurement(measurementName, conditionMet ? 1 : 0));
            }

            // Removing the object if it has too few partners
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
            String metadataName = getSummaryMeasurementName(partnerObjectsName);
            workspace.getMetadata().put(metadataName, count);
        }

        // Showing objects
        if (showOutput)
            inputObjects.convertToImageRandomColours().showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new PartnerObjectsP(PARTNER_OBJECTS, this));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.addAll(super.updateAndGetParameters());

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        returnedParameters.add(parameters.getParameter(PARTNER_OBJECTS));
        ((PartnerObjectsP) parameters.getParameter(PARTNER_OBJECTS)).setPartnerObjectsName(inputObjectsName);

        returnedParameters.addAll(updateAndGetMeasurementParameters());

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefCollection returnedRefs = super.updateAndGetObjectMeasurementRefs();

        if ((boolean) parameters.getValue(STORE_INDIVIDUAL_RESULTS)) {
            String partnerObjectsName = parameters.getValue(PARTNER_OBJECTS);
            String measurementName = getIndividualMeasurementName(partnerObjectsName);
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

            returnedRefs.add(new ObjMeasurementRef(measurementName, inputObjectsName));
            if (parameters.getValue(FILTER_MODE).equals(FilterModes.MOVE_FILTERED)) {
                String outputObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS);
                returnedRefs.add(new ObjMeasurementRef(measurementName, outputObjectsName));
            }
        }

        return returnedRefs;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        MetadataRefCollection returnedRefs = new MetadataRefCollection();

        // Filter results are stored as a metadata item since they apply to the whole
        // set
        if ((boolean) parameters.getValue(STORE_SUMMARY_RESULTS)) {
            String partnerObjectsName = parameters.getValue(PARTNER_OBJECTS);
            String metadataName = getSummaryMeasurementName(partnerObjectsName);

            returnedRefs.add(metadataRefs.getOrPut(metadataName));

        }

        return returnedRefs;

    }

    @Override
    protected void addParameterDescriptions() {
        super.addParameterDescriptions();
        
        parameters.get(PARTNER_OBJECTS).setDescription("Objects will be filtered against the number of partners they have from this object collection.");

    }
}
