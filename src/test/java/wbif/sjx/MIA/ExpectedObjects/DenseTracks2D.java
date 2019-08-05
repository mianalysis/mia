package wbif.sjx.MIA.ExpectedObjects;

import wbif.sjx.common.Object.Volume.VolumeType;

import java.util.HashMap;
import java.util.List;

public class DenseTracks2D extends ExpectedObjects {
    public DenseTracks2D() {
        super(VolumeType.POINTLIST, 600, 600, 1);
    }

    @Override
    public List<Integer[]> getCoordinates5D() {
        return getCoordinates5D("/coordinates/DenseTracks2D.csv");
    }

    @Override
    public HashMap<Integer, HashMap<String, Double>> getMeasurements() {
        return null;
    }
}
