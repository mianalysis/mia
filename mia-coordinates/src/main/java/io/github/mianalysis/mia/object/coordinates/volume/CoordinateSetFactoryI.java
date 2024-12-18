package io.github.mianalysis.mia.object.coordinates.volume;

public interface CoordinateSetFactoryI {
    public String getName();
    public CoordinateSetI createCoordinateSet();
    public CoordinateSetFactoryI duplicate();

}
