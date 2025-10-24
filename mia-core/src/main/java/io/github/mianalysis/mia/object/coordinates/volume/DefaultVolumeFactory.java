package io.github.mianalysis.mia.object.coordinates.volume;

public class DefaultVolumeFactory implements VolumeFactory {
    @Override
    public String getName() {
        return "Default";
    }
    
    @Override
    public VolumeI createVolume(CoordinateSetFactoryI factory, SpatCal spatCal) {
        return new DefaultVolume(factory, spatCal);
    }

    @Override
    public VolumeFactory duplicate() {
        return new DefaultVolumeFactory();
    }
    
}
