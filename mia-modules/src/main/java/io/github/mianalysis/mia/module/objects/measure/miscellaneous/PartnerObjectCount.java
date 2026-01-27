package io.github.mianalysis.mia.module.objects.measure.miscellaneous;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.measurements.MeasurementI;
import io.github.mianalysis.mia.object.measurements.PartnerCountMeasurement;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.PartnerObjectsP;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by sc13967 on 05/05/2017.
 */

/**
 * Calculates the number of partners from a specific class. Measurements are
 * assigned to all objects in the input collection. Unlike normal measurements,
 * this value can optionally be evaluated at the time of use, so should always
 * be up to date.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class PartnerObjectCount extends Module {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Object and image input";

    /**
     * For each object in this collection the number of associated partner objects
     * (from the collection specified by "Partner objects") will be calculated. The
     * count is stored as a measurement associated with each input object. The
     * measurement is evaluated at the time of access (unlike "normal" measurements
     * which have fixed values), so should always be correct.
     */
    public static final String INPUT_OBJECTS = "Input objects";

    /**
     * Partner objects to be counted.
     */
    public static final String PARTNER_OBJECTS = "Partner objects";

    public static final String COUNT_SEPARATOR = "Count controls";

    public static final String LIVE_MEASUREMENT = "Live measurement";

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
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Calculates the number of partners from a specific class.  Measurements are assigned to all objects in the input collection.  Unlike normal measurements, this value can optionally be evaluated at the time of use, so should always be up to date.";

    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting parameters
        String objectName = parameters.getValue(INPUT_OBJECTS, workspace);
        String partnerObjectsName = parameters.getValue(PARTNER_OBJECTS, workspace);
        boolean liveMeasurement = parameters.getValue(LIVE_MEASUREMENT, workspace);

        ObjsI objects = workspace.getObjects(objectName);
        String measurementName = getFullName(partnerObjectsName);

        if (objects == null)
            return Status.PASS;

        for (ObjI obj : objects.values())
            if (liveMeasurement)
                obj.addMeasurement(new PartnerCountMeasurement(measurementName, obj, partnerObjectsName));
            else {
                ObjsI partners = obj.getPartners(partnerObjectsName);
                int count = partners == null ? 0 : partners.size();
                obj.addMeasurement(new MeasurementI(measurementName, count));
            }

        if (showOutput)
            objects.showMeasurements(this, modules);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new PartnerObjectsP(PARTNER_OBJECTS, this));

        parameters.add(new SeparatorP(COUNT_SEPARATOR, this));
        parameters.add(new BooleanP(LIVE_MEASUREMENT, this, true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        ((PartnerObjectsP) parameters.get(PARTNER_OBJECTS)).setPartnerObjectsName(inputObjectsName);

        return parameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        WorkspaceI workspace = null;
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String partnerObjectsName = parameters.getValue(PARTNER_OBJECTS, workspace);

        String measurementName = getFullName(partnerObjectsName);

        ObjMeasurementRef ref = objectMeasurementRefs.getOrPut(measurementName);
        ref.setObjectsName(inputObjectsName);
        ref.setDescription("Number of \"" + partnerObjectsName + "\" partner objects associated with this object.");
        returnedRefs.add(ref);

        return returnedRefs;

    }

    @Override
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
        return null;
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
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
                        + "\") will be calculated.  The count is stored as a measurement associated with each input object.  "
                        +"Depending on the \""+LIVE_MEASUREMENT+"\" parameters, the measurement can be evaluated at the time of access "
                        +"(unlike \"normal\" measurements which have fixed values), so should always be correct.");

        parameters.get(PARTNER_OBJECTS).setDescription("Partner objects to be counted.");

        parameters.get(LIVE_MEASUREMENT).setDescription("When selected, the partner object count will be evaluated at the time of access, "
        + "so will always be up to date.  When not selected it is fixed at the time of evaluation.");

    }
}
