package wbif.sjx.ModularImageAnalysis;

import wbif.sjx.ModularImageAnalysis.ExpectedObjects;
import wbif.sjx.ModularImageAnalysis.Module.Visualisation.AddObjectsOverlay;

import java.util.HashMap;
import java.util.List;

/**
 * Created by sc13967 on 12/02/2018.
 */
public class ExpectedProxCubes1 extends ExpectedObjects {
    public enum Measures {N_VOXELS,ID_8BIT,SURF_PROX_ID,SURF_PROX_DIST_PX,SURF_PROX_DIST_CAL,
        SURF_PROX_ID_3PX,SURF_PROX_DIST_PX_3PX,SURF_PROX_DIST_CAL_3PX};

    public HashMap<Integer,HashMap<String,Double>> getMeasurements() {
        HashMap<Integer, HashMap<String, Double>> expectedValues = new HashMap<>();

        HashMap<String, Double> obj = new HashMap<>();
        obj.put(Measures.N_VOXELS.name(), 64d);
        obj.put(Measures.ID_8BIT.name(), 25d);
        obj.put(Measures.SURF_PROX_ID.name(),89d);
        obj.put(Measures.SURF_PROX_DIST_PX.name(),4d);
        obj.put(Measures.SURF_PROX_DIST_CAL.name(),0.08);
        obj.put(Measures.SURF_PROX_ID_3PX.name(),Double.NaN);
        obj.put(Measures.SURF_PROX_DIST_PX_3PX.name(),Double.NaN);
        obj.put(Measures.SURF_PROX_DIST_CAL_3PX.name(),Double.NaN);
        expectedValues.put(64, obj);

        obj = new HashMap<>();
        obj.put(Measures.N_VOXELS.name(), 1d);
        obj.put(Measures.ID_8BIT.name(), 43d);
        obj.put(Measures.SURF_PROX_ID.name(),65d);
        obj.put(Measures.SURF_PROX_DIST_PX.name(),-1d);
        obj.put(Measures.SURF_PROX_DIST_CAL.name(),-0.1d);
        obj.put(Measures.SURF_PROX_ID_3PX.name(),65d);
        obj.put(Measures.SURF_PROX_DIST_PX_3PX.name(),-1d);
        obj.put(Measures.SURF_PROX_DIST_CAL_3PX.name(),-0.1d);
        expectedValues.put(1, obj);

        obj = new HashMap<>();
        obj.put(Measures.N_VOXELS.name(), 12d);
        obj.put(Measures.ID_8BIT.name(), 20d);
        obj.put(Measures.SURF_PROX_ID.name(),65d);
        obj.put(Measures.SURF_PROX_DIST_PX.name(),2d);
        obj.put(Measures.SURF_PROX_DIST_CAL.name(),0.04);
        obj.put(Measures.SURF_PROX_ID_3PX.name(),65d);
        obj.put(Measures.SURF_PROX_DIST_PX_3PX.name(),2d);
        obj.put(Measures.SURF_PROX_DIST_CAL_3PX.name(),0.04d);
        expectedValues.put(12, obj);

        return expectedValues;

    }

    @Override
    public List<Integer[]> getCoordinates3D() {
        return getCoordinates3D("/coordinates/ExpectedProxCubes1.csv");
    }
}
