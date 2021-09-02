package io.github.mianalysis.mia.expectedobjects;

import io.github.sjcross.common.Object.Volume.VolumeType;

import java.util.HashMap;
import java.util.List;

import ome.units.UNITS;

/**
 * Created by sc13967 on 23/03/2018.
 */
public class Sphere3D extends ExpectedObjects {
    public Sphere3D(VolumeType volumeType) {
        super(volumeType, 64,76,12,1, 0.02, UNITS.SECOND);
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
