package io.github.mianalysis.mia.module.objects.measure.miscellaneous;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.Module;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.object.measurements.PartnerCountMeasurement;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.PartnerObjectsP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.object.parameters.Parameters;

/**
 * Created by sc13967 on 05/05/2017.
 */
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class PartnerObjectCount extends Module {
    public static final String INPUT_SEPARATOR = "Object and image input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String PARTNER_OBJECTS = "Partner objects";

    public PartnerObjectCount(Modules modules) {
        super("Partner object count", modules);
    }

    public static String getFullName(String partnerObjectsName) {
        return "COUNT // " + partnerObjectsName;
    }


    @Override
    public Category getCategory() {
        return Categories.OBJECTS_MEASURE_MISCELLANEOUS;
    }

    @Override
    public String getDescription() {
        return "Calculates the number of partners from a specific class.  Measurements are assigned to all objects in the input collection.  Unlike normal measurements, this value is evaluated at the time of use, so should always be up to date.";

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input objects
        String objectName = parameters.getValue(INPUT_OBJECTS,workspace);
        String partnerObjectsName = parameters.getValue(PARTNER_OBJECTS,workspace);

        Objs objects = workspace.getObjects().get(objectName);
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
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);
        ((PartnerObjectsP) parameters.get(PARTNER_OBJECTS)).setPartnerObjectsName(inputObjectsName);

        return parameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
Workspace workspace = null;
        return null;
    }

    @Override
public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
Workspace workspace = null;
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);
        String partnerObjectsName = parameters.getValue(PARTNER_OBJECTS,workspace);

        String measurementName = getFullName(partnerObjectsName);

        ObjMeasurementRef ref = objectMeasurementRefs.getOrPut(measurementName);
        ref.setObjectsName(inputObjectsName);
        ref.setDescription("Number of \""+partnerObjectsName+"\" partner objects associated with this object.");
        returnedRefs.add(ref);

        return returnedRefs;

    }

    @Override
public MetadataRefs updateAndGetMetadataReferences() {
Workspace workspace = null;
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
Workspace workspace = null;
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
Workspace workspace = null;
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
