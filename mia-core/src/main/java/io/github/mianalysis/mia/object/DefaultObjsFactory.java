package io.github.mianalysis.mia.object;

import ij.ImagePlus;
import io.github.mianalysis.mia.object.coordinates.volume.SpatCal;
import ome.units.quantity.Time;
import ome.units.unit.Unit;

public class DefaultObjsFactory implements ObjsFactoryI {

    @Override
    public String getName() {
        return "Default";
    }

    @Override
    public ObjsI createFromExampleObjs(String name, ObjsI exampleObjs) {
        return new DefaultObjs(name, exampleObjs);
    }

    @Override
    public ObjsI createObjs(String name, double dppXY, double dppZ, String units, int width, int height, int nSlices,
            int nFrames, double frameInterval, Unit<Time> temporalUnit) {
        return new DefaultObjs(name, dppXY, dppZ, units, width, height, nSlices, nFrames, frameInterval, temporalUnit);
    }

    @Override
    public ObjsI createFromSpatCal(String name, SpatCal cal, int nFrames, double frameInterval, Unit<Time> temporalUnit) {
        return new DefaultObjs(name, cal, nFrames, frameInterval, temporalUnit);
    }

    @Override
    public ObjsI createFromImage(String name, ImagePlus imageForCalibration) {
        return new DefaultObjs(name, imageForCalibration);
    }

    @Override
    public ObjsFactoryI duplicate() {
        return new DefaultObjsFactory();
    }
}
