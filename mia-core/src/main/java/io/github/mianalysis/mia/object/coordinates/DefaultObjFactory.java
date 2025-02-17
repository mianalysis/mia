package io.github.mianalysis.mia.object.coordinates;

import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetFactoryI;
import io.github.mianalysis.mia.object.coordinates.volume.SpatCal;

public class DefaultObjFactory implements ObjFactory {
    @Override
    public String getName() {
        return "Default";
    }

    @Override
    public Obj createObj(Objs objCollection, CoordinateSetFactoryI factory, int ID) {
        return new DefaultObj(objCollection, factory, ID);
    }

    @Override
    public Obj createObj(Objs objCollection, CoordinateSetFactoryI factory, int ID, SpatCal spatCal) {
        return new DefaultObj(objCollection, factory, ID, spatCal);
    }

    @Override
    public ObjFactory duplicate() {
        return new DefaultObjFactory();
    }
}
