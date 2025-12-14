package io.github.mianalysis.mia.object.coordinates.volume;

import io.github.mianalysis.mia.object.coordinates.SpatiallyCalibrated;

public interface VolumeFactoryI {
    public String getName();

    public VolumeFactoryI duplicate();
    
    public VolumeI createVolume(CoordinateSetFactoryI factory, int width, int height, int nSlices, double dppXY,
            double dppZ, String units);

    public VolumeI createVolumeFromExample(CoordinateSetFactoryI factory, SpatiallyCalibrated example);

}
