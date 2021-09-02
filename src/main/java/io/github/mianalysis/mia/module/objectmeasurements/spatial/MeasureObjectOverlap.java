package io.github.mianalysis.mia.module.objectmeasurements.spatial;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.sjcross.common.Object.Volume.Volume;

/**
 * Created by sc13967 on 07/02/2018.
 */
public class MeasureObjectOverlap extends Module {
    public final static String INPUT_SEPARATOR = "Object input";
    public static final String OBJECT_SOURCE_MODE = "Object source mode";
    public final static String OBJECT_SET_1 = "Object set 1";
    public final static String OBJECT_SET_2 = "Object set 2";
    public final static String LINK_IN_SAME_FRAME = "Only link objects in same frame";

    public interface ObjectSourceModes {
        String DIFFERENT_CLASSES = "Different classes";
        String SAME_CLASS = "Same class";

        String[] ALL = new String[] { DIFFERENT_CLASSES, SAME_CLASS };

    }

    public MeasureObjectOverlap(Modules modules) {
        super("Measure object overlap", modules);
    }

    public interface Measurements {
        String OVERLAP_VOX_1 = "OVERLAP_VOXELS_1";
        String OVERLAP_VOL_PX_1 = "OVERLAP_VOLUME_(PX³)_1";
        String OVERLAP_VOL_CAL_1 = "OVERLAP_VOLUME_(${SCAL}³)_1";
        String OVERLAP_PERCENT_1 = "OVERLAP_PERCENT_1";
        String OVERLAP_VOX_2 = "OVERLAP_VOXELS_2";
        String OVERLAP_VOL_PX_2 = "OVERLAP_VOLUME_(PX³)_2";
        String OVERLAP_VOL_CAL_2 = "OVERLAP_VOLUME_(${SCAL}³)_2";
        String OVERLAP_PERCENT_2 = "OVERLAP_PERCENT_2";

    }

    public static String getFullName(String objectsName, String measurement) {
        return "OBJ_OVERLAP // " + objectsName + "_" + measurement.substring(0, measurement.length() - 2);

    }

    public static int getNOverlappingPoints(Obj inputObject1, Objs inputObjects2, boolean linkInSameFrame) {
        Volume overlap = new Volume(inputObject1.getVolumeType(), inputObject1.getSpatialCalibration());

        // Running through each object, getting a list of overlapping pixels
        for (Obj obj2 : inputObjects2.values()) {
            // If only linking objects in the same frame, we may just skip this object
            if (linkInSameFrame && inputObject1.getT() != obj2.getT())
                continue;

            // If comparing the same set, don't add the current object
            if (inputObject1 == obj2)
                continue;

            Volume currentOverlap = inputObject1.getOverlappingPoints(obj2);

            overlap.getCoordinateSet().addAll(currentOverlap.getCoordinateSet());

        }

        return overlap.size();

    }

    @Override
    public Category getCategory() {
        return Categories.OBJECT_MEASUREMENTS_SPATIAL;
    }

    @Override
    public String getDescription() {
        return "Calculates the overlap of each object in an object collection with any object from another collection.  Overlaps are calculated for both specified object collections and are stored as measurements associated with the relevant object.  Overlap can occur for multiple objects; however, doubly-overlapped regions will only be counted once (i.e. an object can have no more than 100% overlap).  For example, an object in the first collection with 20% overlap with one object and 12% overlap with another would receive an overlap measurement of 32% (assuming the two overlapping objects weren't themselves overlapped in the overlapping region).";
    }

    @Override
    public Status process(Workspace workspace) {
        String objectSourceMode = parameters.getValue(OBJECT_SOURCE_MODE);
        
        // Getting input objects
        String inputObjects1Name = parameters.getValue(OBJECT_SET_1);
        Objs inputObjects1 = workspace.getObjects().get(inputObjects1Name);

        String inputObjects2Name = parameters.getValue(OBJECT_SET_2);
        Objs inputObjects2;

        switch (objectSourceMode) {
            default:
                MIA.log.writeError("Unknown object source mode");
                return Status.FAIL;
            case ObjectSourceModes.DIFFERENT_CLASSES:
                inputObjects2 = workspace.getObjects().get(inputObjects2Name);
                inputObjects1.removePartners(inputObjects2Name);
                inputObjects2.removePartners(inputObjects1Name);
                break;
            case ObjectSourceModes.SAME_CLASS:
                inputObjects2 = inputObjects1;
                inputObjects1.removePartners(inputObjects1Name);
                break;
        }

        // Getting parameters
        boolean linkInSameFrame = parameters.getValue(LINK_IN_SAME_FRAME);

        int totalObjects = inputObjects1.size() + inputObjects2.size();
        int count = 0;

        // Iterating over all object pairs, adding overlapping pixels to a HashSet based
        // on their index
        for (Obj obj1 : inputObjects1.values()) {
            double dppXY = obj1.getDppXY();
            double dppZ = obj1.getDppZ();

            // Calculating volume
            double objVolume = (double) obj1.size();
            double overlap = (double) getNOverlappingPoints(obj1, inputObjects2, linkInSameFrame);
            double overlapVolPx = overlap * dppZ / dppXY;
            double overlapVolCal = overlap * dppXY * dppXY * dppZ;
            double overlapPC = 100 * overlap / objVolume;

            // Adding the measurements
            obj1.addMeasurement(new Measurement(getFullName(inputObjects2Name, Measurements.OVERLAP_VOX_1), overlap));
            obj1.addMeasurement(
                    new Measurement(getFullName(inputObjects2Name, Measurements.OVERLAP_VOL_PX_1), overlapVolPx));
            obj1.addMeasurement(
                    new Measurement(getFullName(inputObjects2Name, Measurements.OVERLAP_VOL_CAL_1), overlapVolCal));
            obj1.addMeasurement(
                    new Measurement(getFullName(inputObjects2Name, Measurements.OVERLAP_PERCENT_1), overlapPC));

            writeProgressStatus(++count, totalObjects, "objects");

        }

        // Iterating over all object pairs, adding overlapping pixels to a HashSet based
        // on their index
        for (Obj obj2 : inputObjects2.values()) {
            double dppXY = obj2.getDppXY();
            double dppZ = obj2.getDppZ();

            // Calculating volume
            double objVolume = (double) obj2.size();
            double overlap = (double) getNOverlappingPoints(obj2, inputObjects1, linkInSameFrame);
            double overlapVolPx = overlap * dppZ / dppXY;
            double overlapVolCal = overlap * dppXY * dppXY * dppZ;
            double overlapPC = 100 * overlap / objVolume;

            // Adding the measurements
            obj2.addMeasurement(new Measurement(getFullName(inputObjects1Name, Measurements.OVERLAP_VOX_2), overlap));
            obj2.addMeasurement(
                    new Measurement(getFullName(inputObjects1Name, Measurements.OVERLAP_VOL_PX_2), overlapVolPx));
            obj2.addMeasurement(
                    new Measurement(getFullName(inputObjects1Name, Measurements.OVERLAP_VOL_CAL_2), overlapVolCal));
            obj2.addMeasurement(
                    new Measurement(getFullName(inputObjects1Name, Measurements.OVERLAP_PERCENT_2), overlapPC));

            writeProgressStatus(++count, totalObjects, "objects");

        }

        if (showOutput)
            inputObjects1.showMeasurements(this, modules);
        if (showOutput)
            inputObjects2.showMeasurements(this, modules);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters
                .add(new ChoiceP(OBJECT_SOURCE_MODE, this, ObjectSourceModes.DIFFERENT_CLASSES, ObjectSourceModes.ALL));
        parameters.add(new InputObjectsP(OBJECT_SET_1, this));
        parameters.add(new InputObjectsP(OBJECT_SET_2, this));
        parameters.add(new BooleanP(LINK_IN_SAME_FRAME, this, true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(OBJECT_SOURCE_MODE));
        switch ((String) parameters.getValue(OBJECT_SOURCE_MODE)) {
            case ObjectSourceModes.DIFFERENT_CLASSES:
                returnedParameters.add(parameters.getParameter(OBJECT_SET_1));
                returnedParameters.add(parameters.getParameter(OBJECT_SET_2));
                break;
            case ObjectSourceModes.SAME_CLASS:
                returnedParameters.add(parameters.getParameter(OBJECT_SET_1));
                break;
        }

        returnedParameters.add(parameters.getParameter(LINK_IN_SAME_FRAME));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();
        String objects1Name = parameters.getValue(OBJECT_SET_1);
        String objects2Name = parameters.getValue(OBJECT_SET_2);
        if (((String) parameters.getValue(OBJECT_SOURCE_MODE)).equals(ObjectSourceModes.SAME_CLASS))
            objects2Name = objects1Name;

        String name = getFullName(objects2Name, Measurements.OVERLAP_VOX_1);
        ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(objects1Name);
        returnedRefs.add(reference);

        name = getFullName(objects2Name, Measurements.OVERLAP_VOL_PX_1);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(objects1Name);
        returnedRefs.add(reference);

        name = getFullName(objects2Name, Measurements.OVERLAP_VOL_CAL_1);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(objects1Name);
        returnedRefs.add(reference);

        name = getFullName(objects2Name, Measurements.OVERLAP_PERCENT_1);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(objects1Name);
        returnedRefs.add(reference);

        name = getFullName(objects1Name, Measurements.OVERLAP_VOX_2);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(objects2Name);
        returnedRefs.add(reference);

        name = getFullName(objects1Name, Measurements.OVERLAP_VOL_PX_2);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(objects2Name);
        returnedRefs.add(reference);

        name = getFullName(objects1Name, Measurements.OVERLAP_VOL_CAL_2);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(objects2Name);
        returnedRefs.add(reference);

        name = getFullName(objects1Name, Measurements.OVERLAP_PERCENT_2);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(objects2Name);
        returnedRefs.add(reference);

        return returnedRefs;

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
        parameters.get(OBJECT_SOURCE_MODE).setDescription(
                "Controls whether overlap of objects from the same class should be calculated, or whether objects from two different classes should be compared.");
                
        parameters.get(OBJECT_SET_1).setDescription(
                "Object collection for which, the overlap of each object with any object from a separate object collection (specified by the \""
                        + OBJECT_SET_2 + "\" parameter) will be calculated.");

        parameters.get(OBJECT_SET_2).setDescription(
                "Object collection for which, the overlap of each object with any object from a separate object collection (specified by the \""
                        + OBJECT_SET_1 + "\" parameter) will be calculated.");

        parameters.get(LINK_IN_SAME_FRAME).setDescription(
                "When selected, objects will only be considered to have any overlap if they're present in the same frame (timepoint).");
    }
}
