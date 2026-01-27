package io.github.mianalysis.mia.object;

import io.github.mianalysis.mia.object.coordinates.SpatioTemporallyCalibrated;
import io.github.mianalysis.mia.object.image.ImageI;
import ome.units.quantity.Time;
import ome.units.unit.Unit;

public class DefaultObjsFactory implements ObjsFactoryI {

    @Override
    public String getName() {
        return "Default";
    }

    @Override
    public ObjsI createFromExample(String name, SpatioTemporallyCalibrated example) {
        return new DefaultObjs(name, example);
    }

    @Override
    public ObjsI createObjs(String name, int width, int height, int nSlices, double dppXY, double dppZ,
            String spatialUnits, int nFrames, double frameInterval, Unit<Time> temporalUnit) {
        return new DefaultObjs(name, width, height, nSlices, dppXY, dppZ, spatialUnits, nFrames, frameInterval, temporalUnit);
    }

    @Override
    public ObjsI createFromImage(String name, ImageI imageForCalibration) {
        return new DefaultObjs(name, imageForCalibration);
    }

    @Override
    public ObjsFactoryI duplicate() {
        return new DefaultObjsFactory();
    }
}
