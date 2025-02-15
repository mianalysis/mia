package io.github.mianalysis.mia.object.coordinates.volume.quadtree;

import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetFactoryI;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetI;

public class QuadtreeFactory implements CoordinateSetFactoryI {
    public static final String NAME = "Quadtree";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CoordinateSetI createCoordinateSet() {
        return new QuadtreeCoordinates();
    }

    @Override
    public CoordinateSetFactoryI duplicate() {
        return new QuadtreeFactory();
    }   
}
