package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial;

import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by sc13967 on 07/02/2018.
 */
public class MeasureObjectOverlap extends Module {
    public final static String OBJECT_SET_1 = "Object set 1";
    public final static String OBJECT_SET_2 = "Object set 2";


    public interface Measurements {
        String OVERLAP_VOX_1 = "OVERLAP_VOXELS_1";
        String OVERLAP_PERCENT_1 = "OVERLAP_PERCENT_1";
        String OVERLAP_VOX_2 = "OVERLAP_VOXELS_2";
        String OVERLAP_PERCENT_2 = "OVERLAP_PERCENT_2";

    }

    private String getFullName(String objectsName, String measurement) {
        return "OBJ_OVERLAP//"+objectsName+"_"+measurement.substring(0,measurement.length()-2);

    }

    @Override
    public String getTitle() {
        return "Measure object overlap";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected void run(Workspace workspace, boolean verbose) throws GenericMIAException {
        // Getting objects
        String inputObjects1Name = parameters.getValue(OBJECT_SET_1);
        ObjCollection inputObjects1 = workspace.getObjectSet(inputObjects1Name);
        String inputObjects2Name = parameters.getValue(OBJECT_SET_2);
        ObjCollection inputObjects2 = workspace.getObjectSet(inputObjects2Name);

        // Iterating over all object pairs, checking the overlap
        for (Obj obj1:inputObjects1.values()) {
            int overlap = 0;

            // Running through each object, calculating its contribution to the overlap
            for (Obj obj2:inputObjects2.values()) {
                overlap += obj1.getOverlap(obj2);
            }

            // Adding the measurements
            int objVolume = obj1.getNVoxels();
            double overlapPC = 100*(double) overlap/(double) objVolume;
            obj1.addMeasurement(new Measurement(getFullName(inputObjects2Name,Measurements.OVERLAP_VOX_1),overlap));
            obj1.addMeasurement(new Measurement(getFullName(inputObjects2Name,Measurements.OVERLAP_PERCENT_1),overlapPC));
        }

        for (Obj obj2:inputObjects2.values()) {
            int overlap = 0;

            // Running through each object, calculating its contribution to the overlap
            for (Obj obj1:inputObjects1.values()) {
                overlap += obj2.getOverlap(obj1);
            }

            // Adding the measurements
            int objVolume = obj2.getNVoxels();
            double overlapPC = 100*(double) overlap/(double) objVolume;
            obj2.addMeasurement(new Measurement(getFullName(inputObjects1Name,Measurements.OVERLAP_VOX_2),overlap));
            obj2.addMeasurement(new Measurement(getFullName(inputObjects1Name,Measurements.OVERLAP_PERCENT_2),overlapPC));
        }
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(OBJECT_SET_1,Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(OBJECT_SET_2,Parameter.INPUT_OBJECTS,null));

    }

    @Override
    protected void initialiseMeasurementReferences() {
        objectMeasurementReferences.add(new MeasurementReference(Measurements.OVERLAP_VOX_1));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.OVERLAP_PERCENT_1));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.OVERLAP_VOX_2));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.OVERLAP_PERCENT_2));
        
    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        String objects1Name = parameters.getValue(OBJECT_SET_1);
        String objects2Name = parameters.getValue(OBJECT_SET_2);
        
        MeasurementReference overlapVox1 = objectMeasurementReferences.get(Measurements.OVERLAP_VOX_1);
        MeasurementReference overlapPercent1 = objectMeasurementReferences.get(Measurements.OVERLAP_PERCENT_1);
        MeasurementReference overlapVox2 = objectMeasurementReferences.get(Measurements.OVERLAP_VOX_2);
        MeasurementReference overlapPercent2 = objectMeasurementReferences.get(Measurements.OVERLAP_PERCENT_2);

        overlapVox1.setNickName(getFullName(objects2Name, overlapVox1.getName()));
        overlapPercent1.setNickName(getFullName(objects2Name, overlapPercent1.getName()));
        overlapVox2.setNickName(getFullName(objects1Name, overlapVox2.getName()));
        overlapPercent2.setNickName(getFullName(objects1Name, overlapPercent2.getName()));

        overlapVox1.setImageObjName(objects1Name);
        overlapPercent1.setImageObjName(objects1Name);
        overlapVox2.setImageObjName(objects2Name);
        overlapPercent2.setImageObjName(objects2Name);

        return objectMeasurementReferences;

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
