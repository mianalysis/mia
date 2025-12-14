package io.github.mianalysis.mia.object.coordinates.volume;

import io.github.mianalysis.mia.object.coordinates.SpatiallyCalibrated;

public class DefaultVolumeFactory implements VolumeFactoryI {
    @Override
    public String getName() {
        return "Default";
    }

    @Override
    public VolumeFactoryI duplicate() {
        return new DefaultVolumeFactory();
    }

    @Override
    public VolumeI createVolume(CoordinateSetFactoryI factory, int width, int height, int nSlices, double dppXY,
            double dppZ, String units) {
        return new DefaultVolume(factory, width, height, nSlices, dppXY, dppZ, units);
    }

    @Override
    public VolumeI createVolumeFromExample(CoordinateSetFactoryI factory, SpatiallyCalibrated example) {
        return new DefaultVolume(factory, example.getWidth(), example.getHeight(), example.getNSlices(),
                example.getDppXY(), example.getDppZ(), example.getSpatialUnits());
    }
}
