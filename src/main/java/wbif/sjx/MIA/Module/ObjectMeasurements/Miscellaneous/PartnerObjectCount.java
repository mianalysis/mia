package wbif.sjx.MIA.Module.ObjectMeasurements.Miscellaneous;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Object.Measurements.PartnerCountMeasurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.PartnerObjectsP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRef;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

/**
 * Created by sc13967 on 05/05/2017.
 */
public class PartnerObjectCount extends Module {
    public static final String INPUT_SEPARATOR = "Object and image input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String PARTNER_OBJECTS = "Partner objects";

    public PartnerObjectCount(ModuleCollection modules) {
        super("Partner object count", modules);
    }

    public static String getFullName(String partnerObjectsName) {
        return "COUNT // " + partnerObjectsName;
    }


    @Override
    public Category getCategory() {
        return Categories.OBJECT_MEASUREMENTS_MISCELLANEOUS;
    }

    @Override
    public String getDescription() {
        return "Calculates the number of partners from a specific class.  Measurements are assigned to all objects in the input collection.  Unlike normal measurements, this value is evaluated at the time of use, so should always be up to date.";

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input objects
        String objectName = parameters.getValue(INPUT_OBJECTS);
        String partnerObjectsName = parameters.getValue(PARTNER_OBJECTS);

        ObjCollection objects = workspace.getObjects().get(objectName);
        String measurementName = getFullName(partnerObjectsName);

        if (objects == null)
            return Status.PASS;

        for (Obj obj : objects.values())
            obj.addMeasurement(new PartnerCountMeasurement(measurementName, obj, partnerObjectsName));

        if (showOutput)
            objects.showMeasurements(this, modules);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new PartnerObjectsP(PARTNER_OBJECTS, this));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ((PartnerObjectsP) parameters.get(PARTNER_OBJECTS)).setPartnerObjectsName(inputObjectsName);

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
        String partnerObjectsName = parameters.getValue(PARTNER_OBJECTS);

        String measurementName = getFullName(partnerObjectsName);

        ObjMeasurementRef ref = objectMeasurementRefs.getOrPut(measurementName);
        ref.setObjectsName(inputObjectsName);
        ref.setDescription("Number of \""+partnerObjectsName+"\" partner objects associated with this object.");
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

    void addParameterDescriptions() {
        parameters.get(INPUT_OBJECTS).setDescription(
                "For each object in this collection the number of associated partner objects (from the collection specified by \""
                        + PARTNER_OBJECTS
                        + "\") will be calculated.  The count is stored as a measurement associated with each input object.  The measurement is evaluated at the time of access (unlike \"normal\" measurements which have fixed values), so should always be correct.");

        parameters.get(PARTNER_OBJECTS).setDescription("Partner objects to be counted.");

    }
}
