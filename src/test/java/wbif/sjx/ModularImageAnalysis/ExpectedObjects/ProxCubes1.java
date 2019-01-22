package wbif.sjx.ModularImageAnalysis.ExpectedObjects;

import java.util.HashMap;
import java.util.List;

/**
 * Created by sc13967 on 12/02/2018.
 */
public class ProxCubes1 extends ExpectedObjects {
    public enum Measures {N_VOXELS,ID_8BIT,SURF_PROX_ID,SURF_PROX_DIST_PX,SURF_PROX_DIST_CAL,
        SURF_PROX_ID_5PX, SURF_PROX_DIST_PX_5PX, SURF_PROX_DIST_CAL_5PX, CENT_SURF_PROX_ID, CENT_SURF_PROX_DIST_PX,
        CENT_SURF_PROX_DIST_CAL, CENT_SURF_PROX_ID_5PX, CENT_SURF_PROX_DIST_PX_5PX, CENT_SURF_PROX_DIST_CAL_5PX};

    public HashMap<Integer,HashMap<String,Double>> getMeasurements() {
        HashMap<Integer, HashMap<String, Double>> expectedValues = new HashMap<>();

        HashMap<String, Double> obj = new HashMap<>();
        obj.put(Measures.N_VOXELS.name(), 64d);
        obj.put(Measures.ID_8BIT.name(), 25d);
        obj.put(Measures.SURF_PROX_ID.name(),89d);
        obj.put(Measures.SURF_PROX_DIST_PX.name(),4d);
        obj.put(Measures.SURF_PROX_DIST_CAL.name(),0.08);
        obj.put(Measures.SURF_PROX_ID_5PX.name(),89d);
        obj.put(Measures.SURF_PROX_DIST_PX_5PX.name(),4d);
        obj.put(Measures.SURF_PROX_DIST_CAL_5PX.name(),0.08d);
        obj.put(Measures.CENT_SURF_PROX_ID.name(),89d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX.name(),6.06d);
        obj.put(Measures.CENT_SURF_PROX_DIST_CAL.name(),0.12d);
        obj.put(Measures.CENT_SURF_PROX_ID_5PX.name(),Double.NaN);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX_5PX.name(),Double.NaN);
        obj.put(Measures.CENT_SURF_PROX_DIST_CAL_5PX.name(),Double.NaN);
        expectedValues.put(25, obj);

        obj = new HashMap<>();
        obj.put(Measures.N_VOXELS.name(), 1d);
        obj.put(Measures.ID_8BIT.name(), 43d);
        obj.put(Measures.SURF_PROX_ID.name(),65d);
        obj.put(Measures.SURF_PROX_DIST_PX.name(),-5d);
        obj.put(Measures.SURF_PROX_DIST_CAL.name(),-0.1d);
        obj.put(Measures.SURF_PROX_ID_5PX.name(),65d);
        obj.put(Measures.SURF_PROX_DIST_PX_5PX.name(),-5d);
        obj.put(Measures.SURF_PROX_DIST_CAL_5PX.name(),-0.1d);
        obj.put(Measures.CENT_SURF_PROX_ID.name(),65d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX.name(),-5d);
        obj.put(Measures.CENT_SURF_PROX_DIST_CAL.name(),-0.1d);
        obj.put(Measures.CENT_SURF_PROX_ID_5PX.name(),65d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX_5PX.name(),-5d);
        obj.put(Measures.CENT_SURF_PROX_DIST_CAL_5PX.name(),-0.1d);
        expectedValues.put(43, obj);

        obj = new HashMap<>();
        obj.put(Measures.N_VOXELS.name(), 12d);
        obj.put(Measures.ID_8BIT.name(), 20d);
        obj.put(Measures.SURF_PROX_ID.name(),65d);
        obj.put(Measures.SURF_PROX_DIST_PX.name(),5.3852d);
        obj.put(Measures.SURF_PROX_DIST_CAL.name(),0.1077d);
        obj.put(Measures.SURF_PROX_ID_5PX.name(),Double.NaN);
        obj.put(Measures.SURF_PROX_DIST_PX_5PX.name(),Double.NaN);
        obj.put(Measures.SURF_PROX_DIST_CAL_5PX.name(),Double.NaN);
        obj.put(Measures.CENT_SURF_PROX_ID.name(),65d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX.name(),10.32d);
        obj.put(Measures.CENT_SURF_PROX_DIST_CAL.name(),0.21d);
        obj.put(Measures.CENT_SURF_PROX_ID_5PX.name(),Double.NaN);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX_5PX.name(),Double.NaN);
        obj.put(Measures.CENT_SURF_PROX_DIST_CAL_5PX.name(),Double.NaN);
        expectedValues.put(20, obj);

        return expectedValues;

    }

    @Override
    public List<Integer[]> getCoordinates5D() {
        return getCoordinates5D("/coordinates/ProxCubes1.csv");
    }

    @Override
    public boolean is2D() {
        return false;
    }
}
