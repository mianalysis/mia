package io.github.mianalysis.mia.object.coordinates;

import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetFactoryI;

public interface ObjFactoryI {
    public String getName();

    public ObjFactoryI duplicate();

    public ObjI createObj(CoordinateSetFactoryI factory, ObjsI objectCollection);

    public ObjI createObjWithID(CoordinateSetFactoryI factory, ObjsI objectCollection, int ID);

}