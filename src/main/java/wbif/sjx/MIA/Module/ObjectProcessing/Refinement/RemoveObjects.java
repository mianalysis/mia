package wbif.sjx.MIA.Module.ObjectProcessing.Refinement;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Objects.RemovedObjectsP;
import wbif.sjx.MIA.Object.References.*;

/**
 * Created by sc13967 on 07/02/2018.
 */
public class RemoveObjects extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String RETAIN_MEASUREMENTS = "Retain measurements";

    public RemoveObjects(ModuleCollection modules) {
        super("Remove objects",modules);
    }


    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_REFINEMENT;
    }

    @Override
    public String getDescription() {
        return "Removes the specified object set from the workspace.  Doing this helps keep memory usage down.  Measurements associated with an object set can be retained for further use.";
    }

    @Override
    public Status process(Workspace workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        boolean retainMeasurements = parameters.getValue(RETAIN_MEASUREMENTS);

        workspace.removeObjects(inputObjectsName,retainMeasurements);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new RemovedObjectsP(INPUT_OBJECTS,this,"","Name of the object set to be removed from the workspace."));
        parameters.add(new BooleanP(RETAIN_MEASUREMENTS,this,false,"Retain measurements for this object set, or remove everything.  When selected, the object coordinates will be removed, as this is typically where most memory us used, however any measurements associated with each object will be retained."));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}
