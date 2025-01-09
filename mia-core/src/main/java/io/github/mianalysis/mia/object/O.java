package io.github.mianalysis.mia.object;

import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetFactoryI;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetI;
import io.github.mianalysis.mia.object.coordinates.volume.DefaultVolume;
import io.github.mianalysis.mia.object.coordinates.volume.SpatCal;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeI;

public class O implements OI {

    @Override
    public CoordinateSetFactoryI getFactory() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFactory'");
    }

    @Override
    public VolumeI createNewVolume(CoordinateSetFactoryI factory, SpatCal spatCal) {
        DefaultVolume volume = new DefaultVolume();
        volume.initialise(factory, spatCal);
        return volume;
        
    }

    @Override
    public SpatCal getSpatialCalibration() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSpatialCalibration'");
    }

    @Override
    public void setSpatialCalibration(SpatCal spatCal) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setSpatialCalibration'");
    }

    @Override
    public CoordinateSetI getCoordinateSet() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCoordinateSet'");
    }

    @Override
    public void setCoordinateSet(CoordinateSetI coordinateSet) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setCoordinateSet'");
    }
    
}
