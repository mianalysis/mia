package io.github.mianalysis.mia.object.coordinates.volume;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.quadtree.QuadTree;


/**
 * Created by sc13967 on 28/07/2017.
 */
public class QuadtreeCoordinates extends CoordinateSet {
    private final Map<Integer, QuadTree> quadTrees;

    public QuadtreeCoordinates() {
        quadTrees = new TreeMap<>();
    }

    @Override
    public VolumeType getVolumeType() {
        return VolumeType.QUADTREE;
    }

    // Adding and removing points

    @Override
    public boolean add(int x, int y, int z) {
        quadTrees.putIfAbsent(z, new QuadTree());

        // Get relevant QuadTree
        QuadTree quadTree = quadTrees.get(z);

        // Adding this point
        quadTree.add(x, y);

        return true;
    }

    @Override
    public boolean add(Point<Integer> point) {
        return add(point.x, point.y, point.z);
    }

    @Override
    public CoordinateSet createEmptyCoordinateSet() {
        return new QuadtreeCoordinates();
    }

    @Override
    public boolean contains(Object o) {
        Point<Integer> point = (Point<Integer>) o;

        if (!quadTrees.containsKey(point.z))
            return false;
        return quadTrees.get(point.z).contains(point.x, point.y);
    }

    @Override
    public boolean remove(Object o) {
        Point<Integer> point = (Point<Integer>) o;

        if (!quadTrees.containsKey(point.z))
            return false;
        quadTrees.get(point.z).remove(point.x, point.y);

        return true;
    }

    @Override
    public void clear() {
        for (QuadTree quadTree : quadTrees.values())
            quadTree.clear();
    }

    @Override
    public void finalise() {
        for (QuadTree quadTree : quadTrees.values())
            quadTree.optimise();
    }

    @Override
    public void finalise(int z) {
        if (quadTrees.containsKey(z))
            quadTrees.get(z).optimise();
    }

    public CoordinateSet duplicate() {
        QuadtreeCoordinates newCoordinates = new QuadtreeCoordinates();

        // Adding slice by slice
        for (Integer slice : quadTrees.keySet()) {
            QuadTree quadTree = new QuadTree(quadTrees.get(slice));
            newCoordinates.putQuadTree(slice, quadTree);
        }

        return newCoordinates;

    }

    // Creating coordinate subsets

    protected CoordinateSet calculateProjected() {
        CoordinateSet projectedCoordinates = new QuadtreeCoordinates();

        for (Point<Integer> point : this)
            projectedCoordinates.add(point.x, point.y, 0);

        projectedCoordinates.finalise();

        return projectedCoordinates;

    }

    @Override
    public CoordinateSet getSlice(int slice) {
        CoordinateSet sliceCoordinateSet = new QuadtreeCoordinates();
        if (!quadTrees.containsKey(slice))
            return sliceCoordinateSet;

        sliceCoordinateSet.addAll(quadTrees.get(slice));

        sliceCoordinateSet.finalise();

        return sliceCoordinateSet;

    }

    // Volume properties

    @Override
    public int size() {
        int nVoxels = 0;

        for (QuadTree quadTree : quadTrees.values())
            nVoxels += quadTree.size();

        return nVoxels;
    }

    @Override
    public long getNumberOfElements() {
        long nElements = 0;

        for (QuadTree quadTree : quadTrees.values())
            nElements += quadTree.getNodeCount();

        return nElements;
    }

    // Volume access

    @Override
    public Iterator<Point<Integer>> iterator() {
        return new QuadTreeVolumeIterator();
    }

    protected void putQuadTree(int slice, QuadTree quadTree) {
        quadTrees.put(slice, quadTree);
    }

    protected QuadTree getQuadTree(int slice) {
        return quadTrees.get(slice);
    }

    // Miscellaneous

    private class QuadTreeVolumeIterator implements Iterator<Point<Integer>> {
        private Iterator<Integer> sliceIterator = Collections.synchronizedSet(quadTrees.keySet()).iterator();
        private Iterator<Point<Integer>> quadTreeIterator = null;
        private int z = 0;

        public QuadTreeVolumeIterator() {
            if (!sliceIterator.hasNext())
                return;
            this.z = sliceIterator.next();
            quadTreeIterator = quadTrees.get(z).iterator();

        }

        @Override
        public boolean hasNext() {
            // Check we have an iterator
            if (quadTreeIterator == null)
                return false;

            // Check if this current slice has another point
            if (quadTreeIterator.hasNext())
                return true;

            // If the current slice doesn't have another point, check if there is another
            // slice
            if (!sliceIterator.hasNext())
                return false;

            // Now we've got to access the next slice without incrementing the slice
            // iterator
            Iterator<Integer> tempZIterator = quadTrees.keySet().iterator();
            while (tempZIterator.hasNext()) {
                int tempZ = tempZIterator.next();
                if (tempZ != z)
                    continue;

                tempZ = tempZIterator.next();
                return quadTrees.get(tempZ).iterator().hasNext();

            }

            // Shouldn't get this far
            return false;

        }

        @Override
        public Point<Integer> next() {
            // If the current slice has another point, return this with the appropriate
            // slice index
            if (quadTreeIterator.hasNext()) {
                Point<Integer> slicePoint = quadTreeIterator.next();
                return new Point<>(slicePoint.getX(), slicePoint.getY(), z);
            }

            if (sliceIterator.hasNext()) {
                this.z = sliceIterator.next();
                this.quadTreeIterator = quadTrees.get(z).iterator();
                Point<Integer> slicePoint = quadTreeIterator.next();
                return new Point<>(slicePoint.getX(), slicePoint.getY(), z);

            }

            return null;

        }
    }

}