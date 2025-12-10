package io.github.mianalysis.mia.object.coordinates.volume;

public class DefaultVolumeFactory implements VolumeFactory {
    @Override
    public String getName() {
        return "Default";
    }

    @Override
    public VolumeFactory duplicate() {
        return new DefaultVolumeFactory();
    }

    @Override
    public VolumeI createVolume(CoordinateSetFactoryI factory, int width, int height, int nSlices, double dppXY,
            double dppZ, String units) {
        return new DefaultVolume(factory, width, height, nSlices, dppXY, dppZ, units);
    }

    @Override
    public VolumeI createVolumeFromExample(CoordinateSetFactoryI factory, VolumeI exampleVolume) {
        return new DefaultVolume(factory, exampleVolume);
    }    
}
