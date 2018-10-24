package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial;

import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MathFunc.Indexer;
import wbif.sjx.common.Object.Point;

import java.util.ArrayList;
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
        String OVERLAP_PERCENT_1 = "OVERLAP_PERCENT_1";
        String OVERLAP_VOX_2 = "OVERLAP_VOXELS_2";
        String OVERLAP_PERCENT_2 = "OVERLAP_PERCENT_2";

    }

    public static String getFullName(String objectsName, String measurement) {
        return "OBJ_OVERLAP // "+objectsName+"_"+measurement.substring(0,measurement.length()-2);

    }

    public static int getNOverlappingPoints(Obj inputObject1, ObjCollection inputObjects1, ObjCollection inputObjects2, boolean linkInSameFrame) {
        // Creating an Indexer based on the range of each object set
        int[][] limits1 = inputObjects1.getSpatialLimits();
        int[][] limits2 = inputObjects2.getSpatialLimits();
        int[] maxLimits = new int[limits1.length];
        for (int i=0;i<limits1.length;i++) maxLimits[i] = Math.max(limits1[i][1],limits2[i][1]);

        Indexer indexer = new Indexer(maxLimits);
        HashSet<Integer> overlap = new HashSet<>();

        // Running through each object, getting a list of overlapping pixels
        for (Obj obj2:inputObjects2.values()) {
            // If only linking objects in the same frame, we may just skip this object
            if (linkInSameFrame && inputObject1.getT() != obj2.getT()) continue;

            ArrayList<Point<Integer>> currentOverlap = inputObject1.getOverlappingPoints(obj2);

            for (Point<Integer> point:currentOverlap) {
                overlap.add(indexer.getIndex(new int[]{point.getX(),point.getY(),point.getZ()}));
            }
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
        return null;
    }

    @Override
    protected void run(Workspace workspace) throws GenericMIAException {
        // Getting objects
        String inputObjects1Name = parameters.getValue(OBJECT_SET_1);
        ObjCollection inputObjects1 = workspace.getObjectSet(inputObjects1Name);
        String inputObjects2Name = parameters.getValue(OBJECT_SET_2);
        ObjCollection inputObjects2 = workspace.getObjectSet(inputObjects2Name);

        // Getting parameters
        boolean linkInSameFrame = parameters.getValue(LINK_IN_SAME_FRAME);

        // Iterating over all object pairs, adding overlapping pixels to a HashSet based on their index
        for (Obj obj1:inputObjects1.values()) {
            double objVolume = (double) obj1.getNVoxels();
            double overlap = (double) getNOverlappingPoints(obj1,inputObjects1,inputObjects2,linkInSameFrame);

            // Adding the measurements
            double overlapPC = 100*overlap/objVolume;
            obj1.addMeasurement(new Measurement(getFullName(inputObjects2Name,Measurements.OVERLAP_VOX_1),overlap));
            obj1.addMeasurement(new Measurement(getFullName(inputObjects2Name,Measurements.OVERLAP_PERCENT_1),overlapPC));

        }

        // Iterating over all object pairs, adding overlapping pixels to a HashSet based on their index
        for (Obj obj2:inputObjects2.values()) {
            double objVolume = (double) obj2.getNVoxels();
            double overlap = (double) getNOverlappingPoints(obj2,inputObjects2,inputObjects1,linkInSameFrame);

            // Adding the measurements
            double overlapPC = 100*overlap/objVolume;
            obj2.addMeasurement(new Measurement(getFullName(inputObjects1Name,Measurements.OVERLAP_VOX_2),overlap));
            obj2.addMeasurement(new Measurement(getFullName(inputObjects1Name,Measurements.OVERLAP_PERCENT_2),overlapPC));

        }
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(OBJECT_SET_1,Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(OBJECT_SET_2,Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(LINK_IN_SAME_FRAME,Parameter.BOOLEAN,true));

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
        objectMeasurementReferences.setAllCalculated(false);

        String objects1Name = parameters.getValue(OBJECT_SET_1);
        String objects2Name = parameters.getValue(OBJECT_SET_2);

        String name = getFullName(objects2Name, Measurements.OVERLAP_VOX_1);
        MeasurementReference reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(objects1Name);
        reference.setCalculated(true);

        name = getFullName(objects2Name, Measurements.OVERLAP_PERCENT_1);
        reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(objects1Name);
        reference.setCalculated(true);

        name = getFullName(objects1Name, Measurements.OVERLAP_VOX_2);
        reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(objects2Name);
        reference.setCalculated(true);

        name = getFullName(objects1Name, Measurements.OVERLAP_PERCENT_2);
        reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(objects2Name);
        reference.setCalculated(true);

        return objectMeasurementReferences;

    }

    @Override
    public MetadataReferenceCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
