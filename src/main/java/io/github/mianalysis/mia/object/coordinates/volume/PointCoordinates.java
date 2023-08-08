package io.github.mianalysis.mia.object.coordinates.volume;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Spliterator;

import io.github.mianalysis.mia.object.coordinates.Point;

/**
 * Created by sc13967 on 28/07/2017.
 */
public class PointCoordinates extends CoordinateSet {
    private final HashSet<Point<Integer>> points;

    @Override
    public VolumeType getVolumeType() {
        return VolumeType.POINTLIST;
    }

    public PointCoordinates() {
        points = new HashSet<>();
    }

    // Adding and removing points

    @Override
    public boolean add(int x, int y, int z) {
        if (points.size() == Integer.MAX_VALUE) return false; //throw new IntegerOverflowException("Object too large (Integer overflow).");
        points.add(new Point<>(x,y,z));
        return true;
    }

    @Override
    public boolean add(Point<Integer> point) {
        return add(point.x, point.y, point.z);
    }
    
    @Override
    public CoordinateSet createEmptyCoordinateSet() {
        return new PointCoordinates();        
    }

    @Override
    public boolean contains(Object o) {
        return points.contains(o);
    }

    @Override
    public boolean remove(Object o) {
        return points.remove(o);
    }

    @Override
    public void clear() {
        points.clear();
    }

    @Override
    public void finalise() {}

    @Override
    public void finalise(int z) {}

    public CoordinateSet duplicate() {
        PointCoordinates newCoordinates = new PointCoordinates();

        for (Point<Integer> point:this) 
            newCoordinates.add(point.getX(),point.getY(),point.getZ());

        return newCoordinates;

    }


    // Creating coordinate subsets

    protected CoordinateSet calculateProjected() {
        CoordinateSet projectedCoordinates = new PointCoordinates();

        for (Point<Integer> point : this)
            projectedCoordinates.add(point.x, point.y, 0);

        return projectedCoordinates;

    }

    @Override
    public CoordinateSet getSlice(int slice) {
        CoordinateSet sliceCoordinateSet = new PointCoordinates();

        for (Point<Integer> point : points)
            if (point.getZ() == slice)
                sliceCoordinateSet.add(point);
                
        return sliceCoordinateSet;

    }

    
    // Volume properties

    @Override
    public int size() {
        return points.size();
    }

    @Override
    public long getNumberOfElements() {
        return points.size();
    }


    // Volume access

    @Override
    public Iterator<Point<Integer>> iterator() {
        return Collections.synchronizedSet(points).iterator();
    }

    // @Override
    // public void forEach(Consumer<? super Point<Integer>> action) {
    //     points.forEach(action);
    // }

    @Override
    public Spliterator<Point<Integer>> spliterator() {
        return points.spliterator();
    }
}