package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Refinement;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.MeasurementRefCollection;
import wbif.sjx.ModularImageAnalysis.Object.MetadataRefCollection;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.BooleanP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.ParameterCollection;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.RemovedObjectsP;
import wbif.sjx.ModularImageAnalysis.Object.RelationshipCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

/**
 * Created by sc13967 on 07/02/2018.
 */
public class RemoveObjects extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String RETAIN_MEASUREMENTS = "Retain measurements";

    @Override
    public String getTitle() {
        return "Remove objects";
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_REFINEMENT;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public boolean run(Workspace workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        boolean retainMeasurements = parameters.getValue(RETAIN_MEASUREMENTS);

        workspace.removeObject(inputObjectsName,retainMeasurements);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new RemovedObjectsP(INPUT_OBJECTS,this));
        parameters.add(new BooleanP(RETAIN_MEASUREMENTS,this,false));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }

}
