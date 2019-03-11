package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.BooleanP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.InputObjectsP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.ParameterCollection;
import wbif.sjx.common.Object.Point;

import java.util.HashSet;

/**
 * Created by sc13967 on 07/02/2018.
 */
public class MeasureObjectOverlap extends Module {
    public final static String OBJECT_SET_1 = "Object set 1";
    public final static String OBJECT_SET_2 = "Object set 2";
    public final static String LINK_IN_SAME_FRAME = "Only link objects in same frame";


    public interface Measurements {
        String OVERLAP_VOX_1 = "OVERLAP_VOXELS_1";
        String OVERLAP_VOL_PX_1 = "OVERLAP_VOLUME_(PX^3)_1";
        String OVERLAP_VOL_CAL_1 = "OVERLAP_VOLUME_(${CAL}^3)_1";
        String OVERLAP_PERCENT_1 = "OVERLAP_PERCENT_1";
        String OVERLAP_VOX_2 = "OVERLAP_VOXELS_2";
        String OVERLAP_VOL_PX_2 = "OVERLAP_VOLUME_(PX^3)_2";
        String OVERLAP_VOL_CAL_2 = "OVERLAP_VOLUME_(${CAL}^3)_2";
        String OVERLAP_PERCENT_2 = "OVERLAP_PERCENT_2";

    }

    public static String getFullName(String objectsName, String measurement) {
        return "OBJ_OVERLAP // "+objectsName+"_"+Units.replace(measurement.substring(0,measurement.length()-2));

    }

    public static int getNOverlappingPoints(Obj inputObject1, ObjCollection inputObjects1, ObjCollection inputObjects2, boolean linkInSameFrame) {
        HashSet<Point<Integer>> overlap = new HashSet<>();

        // Running through each object, getting a list of overlapping pixels
        for (Obj obj2:inputObjects2.values()) {
            // If only linking objects in the same frame, we may just skip this object
            if (linkInSameFrame && inputObject1.getT() != obj2.getT()) continue;

            HashSet<Point<Integer>> currentOverlap = inputObject1.getOverlappingPoints(obj2);

            overlap.addAll(currentOverlap);

        }

        return overlap.size();

    }


    @Override
    public String getTitle() {
        return "Measure object overlap";
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_SPATIAL;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public boolean run(Workspace workspace) {
        // Getting objects
        String inputObjects1Name = parameters.getValue(OBJECT_SET_1);
        ObjCollection inputObjects1 = workspace.getObjectSet(inputObjects1Name);
        String inputObjects2Name = parameters.getValue(OBJECT_SET_2);
        ObjCollection inputObjects2 = workspace.getObjectSet(inputObjects2Name);

        // Getting parameters
        boolean linkInSameFrame = parameters.getValue(LINK_IN_SAME_FRAME);

        int totalObjects = inputObjects1.size()+inputObjects2.size();
        int count = 0;

        // Iterating over all object pairs, adding overlapping pixels to a HashSet based on their index
        for (Obj obj1:inputObjects1.values()) {
            double dppXY = obj1.getDistPerPxXY();
            double dppZ = obj1.getDistPerPxZ();

            // Calculating volume
            double objVolume = (double) obj1.getNVoxels();
            double overlap = (double) getNOverlappingPoints(obj1,inputObjects1,inputObjects2,linkInSameFrame);
            double overlapVolPx = overlap*dppZ/dppXY;
            double overlapVolCal = overlap*dppXY*dppXY*dppZ;
            double overlapPC = 100*overlap/objVolume;

            // Adding the measurements
            obj1.addMeasurement(new Measurement(getFullName(inputObjects2Name,Measurements.OVERLAP_VOX_1),overlap));
            obj1.addMeasurement(new Measurement(getFullName(inputObjects2Name,Measurements.OVERLAP_VOL_PX_1),overlapVolPx));
            obj1.addMeasurement(new Measurement(getFullName(inputObjects2Name,Measurements.OVERLAP_VOL_CAL_1),overlapVolCal));
            obj1.addMeasurement(new Measurement(getFullName(inputObjects2Name,Measurements.OVERLAP_PERCENT_1),overlapPC));

            writeMessage("Processed "+(++count)+" objects of "+totalObjects);

        }

        // Iterating over all object pairs, adding overlapping pixels to a HashSet based on their index
        for (Obj obj2:inputObjects2.values()) {
            double dppXY = obj2.getDistPerPxXY();
            double dppZ = obj2.getDistPerPxZ();

            // Calculating volume
            double objVolume = (double) obj2.getNVoxels();
            double overlap = (double) getNOverlappingPoints(obj2,inputObjects2,inputObjects1,linkInSameFrame);
            double overlapVolPx = overlap*dppZ/dppXY;
            double overlapVolCal = overlap*dppXY*dppXY*dppZ;
            double overlapPC = 100*overlap/objVolume;

            // Adding the measurements
            obj2.addMeasurement(new Measurement(getFullName(inputObjects1Name,Measurements.OVERLAP_VOX_2),overlap));
            obj2.addMeasurement(new Measurement(getFullName(inputObjects1Name,Measurements.OVERLAP_VOL_PX_2),overlapVolPx));
            obj2.addMeasurement(new Measurement(getFullName(inputObjects1Name,Measurements.OVERLAP_VOL_CAL_2),overlapVolCal));
            obj2.addMeasurement(new Measurement(getFullName(inputObjects1Name,Measurements.OVERLAP_PERCENT_2),overlapPC));

            writeMessage("Processed "+(++count)+" objects of "+totalObjects);

        }

        if (showOutput) inputObjects1.showMeasurements(this);
        if (showOutput) inputObjects2.showMeasurements(this);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(OBJECT_SET_1,this));
        parameters.add(new InputObjectsP(OBJECT_SET_2,this));
        parameters.add(new BooleanP(LINK_IN_SAME_FRAME,this,true));

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
        objectMeasurementRefs.setAllCalculated(false);

        String objects1Name = parameters.getValue(OBJECT_SET_1);
        String objects2Name = parameters.getValue(OBJECT_SET_2);

        String name = getFullName(objects2Name, Measurements.OVERLAP_VOX_1);
        MeasurementRef reference = objectMeasurementRefs.getOrPut(name);
        reference.setImageObjName(objects1Name);
        reference.setCalculated(true);

        name = getFullName(objects2Name, Measurements.OVERLAP_VOL_PX_1);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setImageObjName(objects1Name);
        reference.setCalculated(true);

        name = getFullName(objects2Name, Measurements.OVERLAP_VOL_CAL_1);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setImageObjName(objects1Name);
        reference.setCalculated(true);

        name = getFullName(objects2Name, Measurements.OVERLAP_PERCENT_1);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setImageObjName(objects1Name);
        reference.setCalculated(true);

        name = getFullName(objects1Name, Measurements.OVERLAP_VOX_2);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setImageObjName(objects2Name);
        reference.setCalculated(true);

        name = getFullName(objects1Name, Measurements.OVERLAP_VOL_PX_2);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setImageObjName(objects2Name);
        reference.setCalculated(true);

        name = getFullName(objects1Name, Measurements.OVERLAP_VOL_CAL_2);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setImageObjName(objects2Name);
        reference.setCalculated(true);

        name = getFullName(objects1Name, Measurements.OVERLAP_PERCENT_2);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setImageObjName(objects2Name);
        reference.setCalculated(true);

        return objectMeasurementRefs;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipCollection updateAndGetRelationships() {
        return null;
    }

}
