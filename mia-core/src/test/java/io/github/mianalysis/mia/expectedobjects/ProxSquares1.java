package io.github.mianalysis.mia.expectedobjects;

import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;

import java.util.HashMap;
import java.util.List;

import ome.units.UNITS;

/**
 * Created by sc13967 on 12/02/2018.
 */
public class ProxSquares1 extends ExpectedObjects {
    public ProxSquares1(VolumeType volumeType) {
        super(volumeType, 64, 76, 1, 1, 0.02, UNITS.SECOND);
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
