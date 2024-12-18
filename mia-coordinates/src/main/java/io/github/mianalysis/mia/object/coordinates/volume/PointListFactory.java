package io.github.mianalysis.mia.object.coordinates.volume;

public class PointListFactory implements CoordinateSetFactoryI {
    public static final String NAME = "Pointlist";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CoordinateSetI createCoordinateSet() {
        return new PointCoordinates();
    }

    @Override
    public CoordinateSetFactoryI duplicate() {
        return new PointListFactory();
    }   
}
