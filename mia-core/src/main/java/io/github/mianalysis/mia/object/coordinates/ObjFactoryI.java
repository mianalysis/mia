package io.github.mianalysis.mia.object.coordinates;

import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetFactoryI;
import io.github.mianalysis.mia.object.coordinates.volume.SpatCal;
import io.github.mianalysis.mia.object.coordinates.volume.Volume;

public interface ObjFactoryI {
    public String getName();

    public ObjI createObj(ObjsI objCollection, CoordinateSetFactoryI factory, int ID);

    public ObjI createObj(ObjsI objCollection, CoordinateSetFactoryI factory, int ID, SpatCal spatCal);

    public default ObjI createObj(ObjsI objCollection, int ID, Volume exampleVolume) {
        return createObj(objCollection, exampleVolume.getCoordinateSetFactory(), ID);
    }

    public ObjFactoryI duplicate();

}