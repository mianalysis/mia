package io.github.mianalysis.mia.object.coordinates.volume;

import java.util.HashSet;

import io.github.mianalysis.mia.object.coordinates.Point;

/**
 * Created by sc13967 on 28/07/2017.
 */
public class PointCoordinates extends HashSet<Point<Integer>> implements CoordinateSetI {

    @Override
    public CoordinateSetFactoryI getFactory() {
        return new PointListFactory();
    }

    @Override
    public boolean addCoord(int x, int y, int z) {
        if (size() == Integer.MAX_VALUE) return false; //throw new IntegerOverflowException("Object too large (Integer overflow).");
        add(new Point<>(x,y,z));
        return true;
    }
    
    @Override
    public CoordinateSetI createEmptyCoordinateSet() {
        return new PointCoordinates();        
    }

    @Override
    public void finalise() {}

    @Override
    public void finaliseSlice(int z) {}

    @Override
    public CoordinateSetI duplicate() {
        PointCoordinates newCoordinates = new PointCoordinates();

        for (Point<Integer> point:this) 
            newCoordinates.addCoord(point.getX(),point.getY(),point.getZ());

        return newCoordinates;

    }


    // Creating coordinate subsets

    @Override
    public CoordinateSetI calculateProjected() {
        CoordinateSetI projectedCoordinates = new PointCoordinates();

        for (Point<Integer> point : this)
            projectedCoordinates.addCoord(point.x, point.y, 0);

        return projectedCoordinates;

    }

    @Override
    public CoordinateSetI getSlice(int slice) {
        CoordinateSetI sliceCoordinateSet = new PointCoordinates();

        for (Point<Integer> point : this)
            if (point.getZ() == slice)
                sliceCoordinateSet.add(point);
                
        return sliceCoordinateSet;

    }

    
    // Volume properties

    @Override
    public long getNumberOfElements() {
        return size();
    }

    @Override
    public boolean contains(Point<Integer> point) {
        return super.contains(point);
    }
}