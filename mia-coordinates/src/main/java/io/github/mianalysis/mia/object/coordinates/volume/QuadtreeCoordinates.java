package io.github.mianalysis.mia.object.coordinates.volume;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.volume.quadtree.Quadtree;

/**
 * Created by sc13967 on 28/07/2017.
 */
public class QuadtreeCoordinates extends Quadtree implements CoordinateSetI {
    private final Map<Integer, Quadtree> quadTrees;

    public QuadtreeCoordinates() {
        quadTrees = new TreeMap<>();
    }

    @Override
    public CoordinateSetFactoryI getFactory() {
        return new QuadtreeFactory();
    }

    @Override
    public boolean addCoord(int x, int y, int z) {
        quadTrees.putIfAbsent(z, new Quadtree());

        // Get relevant Quadtree
        Quadtree quadTree = quadTrees.get(z);

        // Adding this point
        quadTree.add(x, y);

        return true;
    }

    public boolean add(Point<Integer> point) {
        return addCoord(point.x, point.y, point.z);
    }

    public CoordinateSetI createEmptyCoordinateSet() {
        return new QuadtreeCoordinates();
    }

    public boolean contains(Point<Integer> point) {
        if (!quadTrees.containsKey(point.z))
            return false;
        
        return quadTrees.get(point.z).contains(point.x, point.y);

    }

    public boolean remove(Point<Integer> point) {
        if (!quadTrees.containsKey(point.z))
            return false;
        quadTrees.get(point.z).remove(point.x, point.y);

        return true;
    }

    public void clear() {
        for (Quadtree quadTree : quadTrees.values())
            quadTree.clear();
    }

    @Override
    public void finalise() {
        for (Quadtree quadTree : quadTrees.values())
            quadTree.optimise();
    }

    @Override
    public void finaliseSlice(int z) {
        if (quadTrees.containsKey(z))
            quadTrees.get(z).optimise();
    }

    @Override
    public CoordinateSetI duplicate() {
        QuadtreeCoordinates newCoordinates = new QuadtreeCoordinates();

        // Adding slice by slice
        for (Integer slice : quadTrees.keySet()) {
            Quadtree quadTree = new Quadtree(quadTrees.get(slice));
            newCoordinates.putQuadtree(slice, quadTree);
        }

        return newCoordinates;

    }

    // Creating coordinate subsets

    @Override
    public CoordinateSetI calculateProjected() {
        CoordinateSetI projectedCoordinates = new QuadtreeCoordinates();

        for (Quadtree quadTree : quadTrees.values())
            for (Point<Integer> point : quadTree)
                projectedCoordinates.addCoord(point.x, point.y, 0);

        projectedCoordinates.finalise();

        return projectedCoordinates;

    }

    public CoordinateSetI getSlice(int slice) {
        CoordinateSetI sliceCoordinateSet = new QuadtreeCoordinates();
        if (!quadTrees.containsKey(slice))
            return sliceCoordinateSet;

        for (Point<Integer> point : quadTrees.get(slice))
            sliceCoordinateSet.add(point);

        sliceCoordinateSet.finalise();

        return sliceCoordinateSet;

    }

    // Volume properties

    public int size() {
        int nVoxels = 0;

        for (Quadtree quadTree : quadTrees.values())
            nVoxels += quadTree.size();

        return nVoxels;
    }

    public long getNumberOfElements() {
        long nElements = 0;

        for (Quadtree quadTree : quadTrees.values())
            nElements += quadTree.getNodeCount();

        return nElements;
    }

    // Volume access

    public Iterator<Point<Integer>> iterator() {
        return new QuadtreeVolumeIterator();
    }

    protected void putQuadtree(int slice, Quadtree quadTree) {
        quadTrees.put(slice, quadTree);
    }

    protected Quadtree getQuadtree(int slice) {
        return quadTrees.get(slice);
    }

    // Miscellaneous

    private class QuadtreeVolumeIterator implements Iterator<Point<Integer>> {
        private Iterator<Integer> sliceIterator = Collections.synchronizedSet(quadTrees.keySet()).iterator();
        private Iterator<Point<Integer>> quadTreeIterator = null;
        private int z = 0;

        public QuadtreeVolumeIterator() {
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