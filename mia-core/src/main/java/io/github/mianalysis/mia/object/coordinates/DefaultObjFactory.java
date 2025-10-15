package io.github.mianalysis.mia.object.coordinates;

import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetFactoryI;
import io.github.mianalysis.mia.object.coordinates.volume.SpatCal;

public class DefaultObjFactory implements ObjFactoryI {
    @Override
    public String getName() {
        return "Default";
    }

    @Override
    public ObjI createObj(ObjsI objCollection, CoordinateSetFactoryI factory, int ID) {
        return new DefaultObj(objCollection, factory, ID);
    }

    @Override
    public ObjI createObj(ObjsI objCollection, CoordinateSetFactoryI factory, int ID, SpatCal spatCal) {
        return new DefaultObj(objCollection, factory, ID, spatCal);
    }

    @Override
    public ObjFactoryI duplicate() {
        return new DefaultObjFactory();
    }
}
