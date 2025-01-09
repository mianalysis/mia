package io.github.mianalysis.mia.object.coordinates.volume;

public class DefaultVolumeFactory implements VolumeFactoryI {
    @Override
    public String getName() {
        return "Default";
    }
    
    @Override
    public VolumeI createVolume() {
        return new DefaultVolume();
    }

    @Override
    public VolumeFactoryI duplicate() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'duplicate'");
    }
    
}
