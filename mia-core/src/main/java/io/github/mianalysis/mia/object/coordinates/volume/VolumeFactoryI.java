package io.github.mianalysis.mia.object.coordinates.volume;

public interface VolumeFactoryI {
    public String getName();
    public VolumeI createVolume(CoordinateSetFactoryI factory, SpatCal spatCal);
    public VolumeFactoryI duplicate();

}
