package io.github.mianalysis.mia.object.coordinates;

import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetFactoryI;
import io.github.mianalysis.mia.object.coordinates.volume.SpatCal;
import io.github.mianalysis.mia.object.coordinates.volume.Volume;

public interface ObjFactory {
    public String getName();

    public Obj createObj(Objs objCollection, CoordinateSetFactoryI factory, int ID);

    public Obj createObj(Objs objCollection, CoordinateSetFactoryI factory, int ID, SpatCal spatCal);

    public default Obj createObj(Objs objCollection, int ID, Volume exampleVolume) {
        return createObj(objCollection, exampleVolume.getCoordinateSetFactory(), ID);
    }

    public ObjFactory duplicate();

}