package wbif.sjx.MIA.ExpectedObjects;

import wbif.sjx.common.Object.Volume.VolumeType;

import java.util.HashMap;
import java.util.List;

/**
 * Created by sc13967 on 12/02/2018.
 */
public class ProxSquares2 extends ExpectedObjects {
    public ProxSquares2(VolumeType volumeType) {
        super(volumeType, 64, 76, 1);
    }

    public enum Measures {
        CENT_PROX_DIST_PX, SURF_PROX_DIST_PX_INOUT, CENT_SURF_PROX_DIST_PX_INOUT, SURF_PROX_DIST_PX_IN,
        CENT_SURF_PROX_DIST_PX_IN, SURF_PROX_DIST_PX_OUT, CENT_SURF_PROX_DIST_PX_OUT};

    public HashMap<Integer,HashMap<String,Double>> getMeasurements() {
        HashMap<Integer, HashMap<String, Double>> expectedValues = new HashMap<>();

        HashMap<String, Double> obj = new HashMap<>();
        obj.put(Measures.CENT_PROX_DIST_PX.name(), 26.66d);
        obj.put(Measures.SURF_PROX_DIST_PX_INOUT.name(), 4d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX_INOUT.name(), 5.5d);
        obj.put(Measures.SURF_PROX_DIST_PX_IN.name(), 0d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX_IN.name(), 0d);
        obj.put(Measures.SURF_PROX_DIST_PX_OUT.name(), 4d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX_OUT.name(), 5.5d);
        expectedValues.put(51, obj);

        obj = new HashMap<>();
        obj.put(Measures.CENT_PROX_DIST_PX.name(), 20.36d);
        obj.put(Measures.SURF_PROX_DIST_PX_INOUT.name(), 2d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX_INOUT.name(), 3.5d);
        obj.put(Measures.SURF_PROX_DIST_PX_IN.name(), 0d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX_IN.name(), 0d);
        obj.put(Measures.SURF_PROX_DIST_PX_OUT.name(), 2d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX_OUT.name(), 3.5d);
        expectedValues.put(85, obj);

        obj = new HashMap<>();
        obj.put(Measures.CENT_PROX_DIST_PX.name(), 14.58d);
        obj.put(Measures.SURF_PROX_DIST_PX_INOUT.name(), 1d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX_INOUT.name(), 2.5d);
        obj.put(Measures.SURF_PROX_DIST_PX_IN.name(), 0d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX_IN.name(), 0d);
        obj.put(Measures.SURF_PROX_DIST_PX_OUT.name(), 1d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX_OUT.name(), 2.5d);
        expectedValues.put(221, obj);

        obj = new HashMap<>();
        obj.put(Measures.CENT_PROX_DIST_PX.name(), 9.19d);
        obj.put(Measures.SURF_PROX_DIST_PX_INOUT.name(), 0d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX_INOUT.name(), 1.5d);
        obj.put(Measures.SURF_PROX_DIST_PX_IN.name(), 0d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX_IN.name(), 0d);
        obj.put(Measures.SURF_PROX_DIST_PX_OUT.name(), 0d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX_OUT.name(), 1.5d);
        expectedValues.put(59, obj);

        obj = new HashMap<>();
        obj.put(Measures.CENT_PROX_DIST_PX.name(), 5.52d);
        obj.put(Measures.SURF_PROX_DIST_PX_INOUT.name(), 0d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX_INOUT.name(), -0.5d);
        obj.put(Measures.SURF_PROX_DIST_PX_IN.name(), 0d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX_IN.name(), -0.5d);
        obj.put(Measures.SURF_PROX_DIST_PX_OUT.name(), 0d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX_OUT.name(), 0d);
        expectedValues.put(144, obj);

        obj = new HashMap<>();
        obj.put(Measures.CENT_PROX_DIST_PX.name(), 7.11d);
        obj.put(Measures.SURF_PROX_DIST_PX_INOUT.name(), 0d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX_INOUT.name(), -0.5d);
        obj.put(Measures.SURF_PROX_DIST_PX_IN.name(), 0d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX_IN.name(), -0.5d);
        obj.put(Measures.SURF_PROX_DIST_PX_OUT.name(), 0d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX_OUT.name(), 0d);
        expectedValues.put(168, obj);

        obj = new HashMap<>();
        obj.put(Measures.CENT_PROX_DIST_PX.name(), 12.02d);
        obj.put(Measures.SURF_PROX_DIST_PX_INOUT.name(), 0d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX_INOUT.name(), -1.5d);
        obj.put(Measures.SURF_PROX_DIST_PX_IN.name(), 0d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX_IN.name(), -1.5d);
        obj.put(Measures.SURF_PROX_DIST_PX_OUT.name(), 0d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX_OUT.name(), 0d);
        expectedValues.put(123, obj);

        obj = new HashMap<>();
        obj.put(Measures.CENT_PROX_DIST_PX.name(), 17.68d);
        obj.put(Measures.SURF_PROX_DIST_PX_INOUT.name(), -1d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX_INOUT.name(), -2.5d);
        obj.put(Measures.SURF_PROX_DIST_PX_IN.name(), -1d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX_IN.name(), -2.5d);
        obj.put(Measures.SURF_PROX_DIST_PX_OUT.name(), 0d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX_OUT.name(), 0d);
        expectedValues.put(29, obj);

        obj = new HashMap<>();
        obj.put(Measures.CENT_PROX_DIST_PX.name(), 23.51d);
        obj.put(Measures.SURF_PROX_DIST_PX_INOUT.name(), -3d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX_INOUT.name(), -4.5d);
        obj.put(Measures.SURF_PROX_DIST_PX_IN.name(), -3d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX_IN.name(), -4.5d);
        obj.put(Measures.SURF_PROX_DIST_PX_OUT.name(), 0d);
        obj.put(Measures.CENT_SURF_PROX_DIST_PX_OUT.name(), 0d);
        expectedValues.put(96, obj);

        return expectedValues;

    }

    @Override
    public List<Integer[]> getCoordinates5D() {
        return getCoordinates5D("/coordinates/ProxSquares2.csv");
    }
}
