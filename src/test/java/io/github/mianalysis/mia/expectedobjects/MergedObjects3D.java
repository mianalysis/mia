package io.github.mianalysis.mia.expectedobjects;

import io.github.sjcross.common.object.volume.VolumeType;

import java.util.HashMap;
import java.util.List;

import ome.units.UNITS;

public class MergedObjects3D extends ExpectedObjects {
    public MergedObjects3D(VolumeType volumeType) {
        super(volumeType, 64, 76, 12, 1, 0.02, UNITS.SECOND);
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
