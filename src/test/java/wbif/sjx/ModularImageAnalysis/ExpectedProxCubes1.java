package wbif.sjx.ModularImageAnalysis;

import wbif.sjx.ModularImageAnalysis.ExpectedObjects;
import wbif.sjx.ModularImageAnalysis.Module.Visualisation.AddObjectsOverlay;

import java.util.HashMap;

/**
 * Created by sc13967 on 12/02/2018.
 */
public class ExpectedProxCubes1 extends ExpectedObjects {
    public enum Measures {N_VOXELS,ID_8BIT,SURF_PROX_ID,SURF_PROX_DIST_PX,SURF_PROX_DIST_CAL,
        SURF_PROX_ID_3PX,SURF_PROX_DIST_PX_3PX,SURF_PROX_DIST_CAL_3PX};

    public static HashMap<Integer,HashMap<Measures,Object>> getExpectedValues3D() {
        HashMap<Integer, HashMap<Measures, Object>> expectedValues = new HashMap<>();

        HashMap<Measures, Object> obj = new HashMap<>();
        obj.put(Measures.N_VOXELS, 64);
        obj.put(Measures.ID_8BIT, 25);
        obj.put(Measures.SURF_PROX_ID,89);
        obj.put(Measures.SURF_PROX_DIST_PX,4);
        obj.put(Measures.SURF_PROX_DIST_CAL,0.8);
        obj.put(Measures.SURF_PROX_ID_3PX,null);
        obj.put(Measures.SURF_PROX_DIST_PX_3PX,Double.NaN);
        obj.put(Measures.SURF_PROX_DIST_CAL_3PX,Double.NaN);
        expectedValues.put(64, obj);

        obj = new HashMap<>();
        obj.put(Measures.N_VOXELS, 1);
        obj.put(Measures.ID_8BIT, 43);
        obj.put(Measures.SURF_PROX_ID,65);
        obj.put(Measures.SURF_PROX_DIST_PX,-1);
        obj.put(Measures.SURF_PROX_DIST_CAL,-1);
        obj.put(Measures.SURF_PROX_ID_3PX,65);
        obj.put(Measures.SURF_PROX_DIST_PX_3PX,-1);
        obj.put(Measures.SURF_PROX_DIST_CAL_3PX,-1);
        expectedValues.put(1, obj);

        obj = new HashMap<>();
        obj.put(Measures.N_VOXELS, 12);
        obj.put(Measures.ID_8BIT, 20);
        obj.put(Measures.SURF_PROX_ID,65);
        obj.put(Measures.SURF_PROX_DIST_PX,2);
        obj.put(Measures.SURF_PROX_DIST_CAL,0.4);
        obj.put(Measures.SURF_PROX_ID_3PX,65);
        obj.put(Measures.SURF_PROX_DIST_PX_3PX,2);
        obj.put(Measures.SURF_PROX_DIST_CAL_3PX,0.4);
        expectedValues.put(12, obj);

        return expectedValues;

    }


    @Override
    public int[][] getCoordinates3D() {
        return new int[][]{
                {25,16,13,0,3,0},
                {25,17,13,0,3,0},
                {25,18,13,0,3,0},
                {25,19,13,0,3,0},
                {25,16,14,0,3,0},
                {25,17,14,0,3,0},
                {25,18,14,0,3,0},
                {25,19,14,0,3,0},
                {25,16,15,0,3,0},
                {25,17,15,0,3,0},
                {25,18,15,0,3,0},
                {25,19,15,0,3,0},
                {25,16,16,0,3,0},
                {25,17,16,0,3,0},
                {25,18,16,0,3,0},
                {25,19,16,0,3,0},
                {25,16,13,0,4,0},
                {25,17,13,0,4,0},
                {25,18,13,0,4,0},
                {25,19,13,0,4,0},
                {25,16,14,0,4,0},
                {25,17,14,0,4,0},
                {25,18,14,0,4,0},
                {25,19,14,0,4,0},
                {25,16,15,0,4,0},
                {25,17,15,0,4,0},
                {25,18,15,0,4,0},
                {25,19,15,0,4,0},
                {25,16,16,0,4,0},
                {25,17,16,0,4,0},
                {25,18,16,0,4,0},
                {25,19,16,0,4,0},
                {25,16,13,0,5,0},
                {25,17,13,0,5,0},
                {25,18,13,0,5,0},
                {25,19,13,0,5,0},
                {25,16,14,0,5,0},
                {25,17,14,0,5,0},
                {25,18,14,0,5,0},
                {25,19,14,0,5,0},
                {25,16,15,0,5,0},
                {25,17,15,0,5,0},
                {25,18,15,0,5,0},
                {25,19,15,0,5,0},
                {25,16,16,0,5,0},
                {25,17,16,0,5,0},
                {25,18,16,0,5,0},
                {25,19,16,0,5,0},
                {25,16,13,0,6,0},
                {25,17,13,0,6,0},
                {25,18,13,0,6,0},
                {25,19,13,0,6,0},
                {25,16,14,0,6,0},
                {25,17,14,0,6,0},
                {25,18,14,0,6,0},
                {25,19,14,0,6,0},
                {25,16,15,0,6,0},
                {25,17,15,0,6,0},
                {25,18,15,0,6,0},
                {25,19,15,0,6,0},
                {25,16,16,0,6,0},
                {25,17,16,0,6,0},
                {25,18,16,0,6,0},
                {25,19,16,0,6,0},
                {43,49,38,0,8,0},
                {20,39,42,0,10,0},
                {20,40,42,0,10,0},
                {20,39,43,0,10,0},
                {20,40,43,0,10,0},
                {20,39,42,0,11,0},
                {20,40,42,0,11,0},
                {20,39,43,0,11,0},
                {20,40,43,0,11,0},
                {20,39,42,0,12,0},
                {20,40,42,0,12,0},
                {20,39,43,0,12,0},
                {20,40,43,0,12,0}
        };
    }
}
