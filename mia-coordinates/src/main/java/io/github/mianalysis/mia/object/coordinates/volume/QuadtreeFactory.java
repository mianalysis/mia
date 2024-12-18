package io.github.mianalysis.mia.object.coordinates.volume;

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
