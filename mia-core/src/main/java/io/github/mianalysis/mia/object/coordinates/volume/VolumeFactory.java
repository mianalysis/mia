package io.github.mianalysis.mia.object.coordinates.volume;

public interface VolumeFactory {
    public String getName();
    public VolumeI createVolume(CoordinateSetFactoryI factory, int width, int height, int nSlices, double dppXY,
            double dppZ, String units);
    public VolumeI createVolumeFromExample(CoordinateSetFactoryI factory, VolumeI exampleVolume);
    public VolumeFactory duplicate();

}
