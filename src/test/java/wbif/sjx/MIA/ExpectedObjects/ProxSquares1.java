package wbif.sjx.MIA.ExpectedObjects;

import wbif.sjx.common.Object.Volume.VolumeType;

import java.util.HashMap;
import java.util.List;

/**
 * Created by sc13967 on 12/02/2018.
 */
public class ProxSquares1 extends ExpectedObjects {
    public ProxSquares1() {
        super(VolumeType.POINTLIST, 64, 76, 1);
    }

    public enum Measures {};

    public HashMap<Integer,HashMap<String,Double>> getMeasurements() {
        return null;
    }

    @Override
    public List<Integer[]> getCoordinates5D() {
        return getCoordinates5D("/coordinates/ProxSquares1.csv");
    }
}
