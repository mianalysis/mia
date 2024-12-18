package io.github.mianalysis.mia.object.coordinates.volume;

public class OctreeFactory implements CoordinateSetFactoryI {
    public static final String NAME = "Octree";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CoordinateSetI createCoordinateSet() {
        return new OctreeCoordinates();
    }

    @Override
    public CoordinateSetFactoryI duplicate() {
        return new OctreeFactory();
    }   
}
