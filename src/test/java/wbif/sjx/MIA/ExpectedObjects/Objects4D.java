package wbif.sjx.MIA.ExpectedObjects;

import wbif.sjx.common.Object.Volume.VolumeType;

import java.util.HashMap;
import java.util.List;

/**
 * Created by sc13967 on 21/02/2018.
 */
public class Objects4D extends ExpectedObjects {
    public Objects4D(VolumeType volumeType) {
        super(volumeType, 64, 76, 12, 4);
    }

    @Override
    public List<Integer[]> getCoordinates5D() {
        return getCoordinates5D("/coordinates/Objects4D.csv");
    }

    @Override
    public HashMap<Integer, HashMap<String, Double>> getMeasurements() {
        return null;
    }
}
