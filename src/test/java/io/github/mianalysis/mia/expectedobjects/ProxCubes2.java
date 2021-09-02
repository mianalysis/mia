package io.github.mianalysis.mia.expectedobjects;

import io.github.sjcross.common.object.volume.VolumeType;

import java.util.HashMap;
import java.util.List;

import ome.units.UNITS;

/**
 * Created by sc13967 on 12/02/2018.
 */
public class ProxCubes2 extends ExpectedObjects {
    public ProxCubes2(VolumeType volumeType) {
        super(volumeType, 64, 76, 12, 1, 0.02, UNITS.SECOND);
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
