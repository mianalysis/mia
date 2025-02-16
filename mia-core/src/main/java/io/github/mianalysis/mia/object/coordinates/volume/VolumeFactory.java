package io.github.mianalysis.mia.object.coordinates.volume;

public interface VolumeFactory {
    public String getName();
    public Volume createVolume(CoordinateSetFactoryI factory, SpatCal spatCal);
    public VolumeFactory duplicate();

}
