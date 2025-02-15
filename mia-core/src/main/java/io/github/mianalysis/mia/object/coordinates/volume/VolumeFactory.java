package io.github.mianalysis.mia.object.coordinates.volume;

public class VolumeFactory implements VolumeFactoryI {
    @Override
    public String getName() {
        return "Default";
    }
    
    @Override
    public VolumeI createVolume(CoordinateSetFactoryI factory, SpatCal spatCal) {
        return new Volume(factory, spatCal);
    }

    @Override
    public VolumeFactoryI duplicate() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'duplicate'");
    }
    
}
