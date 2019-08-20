package wbif.sjx.MIA.ExpectedObjects;

import wbif.sjx.common.Object.Volume.VolumeType;

import java.util.HashMap;
import java.util.List;

public class MergedObjects3D extends ExpectedObjects {
    public MergedObjects3D(VolumeType volumeType) {
        super(volumeType, 64, 76, 12);
    }

    @Override
    public List<Integer[]> getCoordinates5D() {
        return getCoordinates5D("/coordinates/MergedObjects3D.csv");
    }

    @Override
    public HashMap<Integer, HashMap<String, Double>> getMeasurements() {
        return null;
    }
}
