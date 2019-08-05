package wbif.sjx.MIA.ExpectedObjects;

import wbif.sjx.common.Object.Volume.VolumeType;

import java.util.HashMap;
import java.util.List;

/**
 * Created by sc13967 on 12/02/2018.
 */
public class ProxCubes2 extends ExpectedObjects {
    public ProxCubes2() {
        super(VolumeType.POINTLIST, 64, 76, 12);
    }

    @Override
    public List<Integer[]> getCoordinates5D() {
        return getCoordinates5D("/coordinates/ProxCubes2.csv");
    }

    @Override
    public HashMap<Integer, HashMap<String, Double>> getMeasurements() {
        return null;
    }
}
