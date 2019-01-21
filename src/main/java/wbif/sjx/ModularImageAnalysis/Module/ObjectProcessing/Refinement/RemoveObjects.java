package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Refinement;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.ParameterCollection;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.RemovedObjectsP;

/**
 * Created by sc13967 on 07/02/2018.
 */
public class RemoveObjects extends Module {
    public static final String INPUT_OBJECTS = "Input objects";

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
    protected boolean run(Workspace workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        workspace.removeObject(inputObjectsName);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new RemovedObjectsP(INPUT_OBJECTS,this));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementReferences() {
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
