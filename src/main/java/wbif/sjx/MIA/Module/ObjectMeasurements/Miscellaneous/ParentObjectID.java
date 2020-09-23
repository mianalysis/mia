// TODO: Could addRef an optional parameter to select the channel of the input image to use for measurement

package wbif.sjx.MIA.Module.ObjectMeasurements.Miscellaneous;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Measurements.ParentIDMeasurement;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.ParentObjectsP;
import wbif.sjx.MIA.Object.References.ObjMeasurementRef;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

/**
 * Created by sc13967 on 05/05/2017.
 */
public class ParentObjectID extends Module {
    public static final String INPUT_SEPARATOR = "Object and image input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String PARENT_OBJECT = "Parent object";

    public ParentObjectID(ModuleCollection modules) {
        super("Parent object ID", modules);
    }

    public static String getFullName(String parentObjectsName) {
        return "PARENT_ID // "+ parentObjectsName;
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_MISCELLANEOUS;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input objects
        String objectName = parameters.getValue(INPUT_OBJECTS);
        String parentObjectsName = parameters.getValue(PARENT_OBJECT);

        ObjCollection objects = workspace.getObjects().get(objectName);
        String measurementName = getFullName(parentObjectsName);

        if (objects == null)
            return Status.PASS;
            
        for (Obj obj : objects.values())
            obj.addMeasurement(new ParentIDMeasurement(measurementName, obj, parentObjectsName));
        
        if (showOutput) objects.showMeasurements(this,modules);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new ParentObjectsP(PARENT_OBJECT, this));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ((ParentObjectsP) parameters.get(PARENT_OBJECT)).setChildObjectsName(inputObjectsName);

        return parameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefCollection returnedRefs = new ObjMeasurementRefCollection();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String parentObjectsName = parameters.getValue(PARENT_OBJECT);
        
        String measurementName = getFullName(parentObjectsName);

        // We don't want statistics for this measurement
        ObjMeasurementRef ref = objectMeasurementRefs.getOrPut(measurementName);
        ref.setObjectsName(inputObjectsName);
        ref.setExportMax(false);
        ref.setExportMean(false);
        ref.setExportMin(false);
        ref.setExportStd(false);
        ref.setExportSum(false);

        returnedRefs.add(ref);

        return returnedRefs;

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
