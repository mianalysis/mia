package io.github.mianalysis.mia.object.coordinates.volume;

public class DefaultVolumeFactory implements VolumeFactory {
    @Override
    public String getName() {
        return "Default";
    }
    
    @Override
    public Volume createVolume(CoordinateSetFactoryI factory, SpatCal spatCal) {
        return new DefaultVolume(factory, spatCal);
    }

    @Override
    public VolumeFactory duplicate() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'duplicate'");
    }
    
}
