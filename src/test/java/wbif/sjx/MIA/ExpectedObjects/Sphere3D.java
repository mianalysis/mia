package wbif.sjx.MIA.ExpectedObjects;

import wbif.sjx.common.Object.Volume.VolumeType;

import java.util.HashMap;
import java.util.List;

/**
 * Created by sc13967 on 23/03/2018.
 */
public class Sphere3D extends ExpectedObjects {
    public Sphere3D(VolumeType volumeType) {
        super(volumeType, 64,76,12);
    }

    @Override
    public List<Integer[]> getCoordinates5D() {
        return getCoordinates5D("/coordinates/Sphere3D.csv");
    }

    @Override
    public HashMap<Integer, HashMap<String, Double>> getMeasurements() {
        return null;
    }
}
