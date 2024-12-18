package io.github.mianalysis.mia.expectedobjects;

import java.util.HashMap;
import java.util.List;

import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetFactoryI;
import ome.units.UNITS;

/**
 * Created by sc13967 on 21/02/2018.
 */
public class Objects4D extends ExpectedObjects {
    public Objects4D(CoordinateSetFactoryI factory) {
        super(factory, 64, 76, 12, 4, 0.02, UNITS.SECOND);
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
