package io.github.mianalysis.mia.object.coordinates;

import ij.ImagePlus;
import io.github.mianalysis.mia.object.units.TemporalUnit;
import ome.units.UNITS;
import ome.units.quantity.Time;
import ome.units.unit.Unit;

public interface SpatioTemporallyCalibrated extends SpatiallyCalibrated {
    public int getNFrames();

    public void setNFrames(int nFrames);

    public double getFrameInterval();

    public void setFrameInterval(double frameInterval);

    public Unit<Time> getTemporalUnit();

    public void setTemporalUnit(Unit<Time> temporalUnit);

    @Override
    public default void applyCalibrationToImage(ImagePlus ipl) {
        ipl.getCalibration().frameInterval = getFrameInterval();
        ipl.getCalibration().fps = 1 / TemporalUnit.getOMEUnit().convertValue(getFrameInterval(), UNITS.SECOND);
        
    }

    public default void setSpatioTemporalCalibrationFromExample(SpatioTemporallyCalibrated example) {
        setSpatialCalibrationFromExample(example);

        setNFrames(example.getNFrames());
        setFrameInterval(example.getFrameInterval());
        setTemporalUnit(example.getTemporalUnit());
        
    }
}
