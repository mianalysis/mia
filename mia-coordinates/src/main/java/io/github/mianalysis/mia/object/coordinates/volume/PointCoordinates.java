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
    public boolean add(int x, int y, int z) {
        if (size() == Integer.MAX_VALUE) return false; //throw new IntegerOverflowException("Object too large (Integer overflow).");
        add(new Point<>(x,y,z));
        return true;
    }

    @Override
    public boolean add(Point<Integer> point) {
        return add(point.x, point.y, point.z);
    }
    
    @Override
    public CoordinateSetI createEmptyCoordinateSet() {
        return new PointCoordinates();        
    }

    @Override
    public boolean contains(Object o) {
        return contains(o);
    }

    @Override
    public boolean remove(Object o) {
        return remove(o);
    }

    @Override
    public void clear() {
        clear();
    }

    @Override
    public void finalise() {}

    @Override
    public void finalise(int z) {}

    @Override
    public CoordinateSetI duplicate() {
        PointCoordinates newCoordinates = new PointCoordinates();

        for (Point<Integer> point:this) 
            newCoordinates.add(point.getX(),point.getY(),point.getZ());

        return newCoordinates;

    }


    // Creating coordinate subsets

    @Override
    public CoordinateSetI calculateProjected() {
        CoordinateSetI projectedCoordinates = new PointCoordinates();

        for (Point<Integer> point : this)
            projectedCoordinates.add(point.x, point.y, 0);

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