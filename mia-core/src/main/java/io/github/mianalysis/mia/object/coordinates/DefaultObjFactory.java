package io.github.mianalysis.mia.object.coordinates;

import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetFactoryI;

public class DefaultObjFactory implements ObjFactoryI {
    @Override
    public String getName() {
        return "Default";
    }

    @Override
    public ObjI createObj(CoordinateSetFactoryI factory, ObjsI objectCollection) {
        return new DefaultObj(factory, objectCollection);
    }

    @Override
    public ObjI createObjWithID(CoordinateSetFactoryI factory, ObjsI objectCollection, int ID) {
        return new DefaultObj(factory, objectCollection, ID);
    }

    @Override
    public ObjFactoryI duplicate() {
        return new DefaultObjFactory();
    }
}
