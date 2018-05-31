package wbif.sjx.ModularImageAnalysis.ExpectedObjects;

import java.util.HashMap;
import java.util.List;

/**
 * Created by sc13967 on 12/02/2018.
 */
public class ExpectedProxSquares2 extends ExpectedObjects {
    public enum Measures {SURF_PROX_DIST_PX,CENT_SURF_PROX_DIST_PX};

    public HashMap<Integer,HashMap<String,Double>> getMeasurements() {
        HashMap<Integer, HashMap<String, Double>> expectedValues = new HashMap<>();

        HashMap<String, Double> obj = new HashMap<>();
        obj.put(Measures.SURF_PROX_DIST_PX.name(), 4d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX.name(), 5.5d);
        expectedValues.put(51, obj);

        obj = new HashMap<>();
        obj.put(Measures.SURF_PROX_DIST_PX.name(), 2d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX.name(), 3.5d);
        expectedValues.put(85, obj);

        obj = new HashMap<>();
        obj.put(Measures.SURF_PROX_DIST_PX.name(), 1d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX.name(), 2.5d);
        expectedValues.put(221, obj);

        obj = new HashMap<>();
        obj.put(Measures.SURF_PROX_DIST_PX.name(), 0d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX.name(), 1.5d);
        expectedValues.put(59, obj);

        obj = new HashMap<>();
        obj.put(Measures.SURF_PROX_DIST_PX.name(), 0d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX.name(), 0.5d);
        expectedValues.put(144, obj);

        obj = new HashMap<>();
        obj.put(Measures.SURF_PROX_DIST_PX.name(), 0d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX.name(), -0.5d);
        expectedValues.put(168, obj);

        obj = new HashMap<>();
        obj.put(Measures.SURF_PROX_DIST_PX.name(), 0d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX.name(), -1.5d);
        expectedValues.put(123, obj);

        obj = new HashMap<>();
        obj.put(Measures.SURF_PROX_DIST_PX.name(), -1d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX.name(), -2.5d);
        expectedValues.put(29, obj);

        obj = new HashMap<>();
        obj.put(Measures.SURF_PROX_DIST_PX.name(), -3d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX.name(), -4.5d);
        expectedValues.put(96, obj);

        return expectedValues;

    }

    @Override
    public List<Integer[]> getCoordinates5D() {
        return getCoordinates5D("/coordinates/ExpectedProxSquares2.csv");
    }

    @Override
    public boolean is2D() {
        return true;
    }
}
