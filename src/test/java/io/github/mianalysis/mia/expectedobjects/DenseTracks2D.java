package io.github.mianalysis.mia.expectedobjects;

import io.github.sjcross.common.Object.Volume.VolumeType;

import java.util.HashMap;
import java.util.List;

import ome.units.UNITS;

public class DenseTracks2D extends ExpectedObjects {
    public DenseTracks2D(VolumeType volumeType) {
        super(volumeType, 600, 600, 1, 100, 0.02, UNITS.SECOND);
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
