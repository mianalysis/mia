package io.github.mianalysis.mia.object;

import ij.ImagePlus;
import io.github.mianalysis.mia.object.coordinates.volume.SpatCal;
import ome.units.quantity.Time;
import ome.units.unit.Unit;

public interface ObjsFactoryI {
    public String getName();
    public ObjsI createObjs(String name, double dppXY, double dppZ, String units, int width, int height, int nSlices, int nFrames,
            double frameInterval, Unit<Time> temporalUnit);
    public ObjsI createFromExampleObjs(String name, ObjsI exampleObjs);
    public ObjsI createFromImage(String name, ImagePlus imageForCalibration);
    public ObjsI createFromSpatCal(String name, SpatCal cal, int nFrames, double frameInterval, Unit<Time> temporalUnit);
    public ObjsFactoryI duplicate();

}