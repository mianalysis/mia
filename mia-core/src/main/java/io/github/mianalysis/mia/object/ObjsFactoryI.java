package io.github.mianalysis.mia.object;

import io.github.mianalysis.mia.object.coordinates.SpatioTemporallyCalibrated;
import io.github.mianalysis.mia.object.image.ImageI;
import ome.units.quantity.Time;
import ome.units.unit.Unit;

public interface ObjsFactoryI {
    public String getName();
    public ObjsI createObjs(String name, int width, int height, int nSlices, double dppXY, double dppZ,
            String spatialUnits, int nFrames, double frameInterval, Unit<Time> temporalUnit);
    public ObjsI createFromExample(String name, SpatioTemporallyCalibrated example);
    public ObjsI createFromImage(String name, ImageI imageForCalibration);
    public ObjsFactoryI duplicate();

}