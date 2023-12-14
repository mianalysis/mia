package io.github.mianalysis.mia.object.coordinates.volume;

import java.util.Iterator;

import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.quadtree.OcTree;


/**
 * Created by sc13967 on 28/07/2017.
 */
public class OctreeCoordinates extends CoordinateSet {
    private OcTree ocTree;

    public OctreeCoordinates() {
        ocTree = new OcTree();
    }

    @Override
    public VolumeType getVolumeType() {
        return VolumeType.OCTREE;
    }

    // Adding and removing points

    @Override
    public boolean add(int x, int y, int z) {
        ocTree.add(x, y, z);

        return true;
    }

    @Override
    public boolean add(Point<Integer> point) {
        return add(point.x, point.y, point.z);
    }

    @Override
    public CoordinateSet createEmptyCoordinateSet() {
        return new OctreeCoordinates();
    }
    
    @Override
    public boolean contains(Object o) {
        Point<Integer> point = (Point<Integer>) o;

        return ocTree.contains(point.x, point.y, point.z);
    }

    @Override
    public boolean remove(Object o) {
        Point<Integer> point = (Point<Integer>) o;
        ocTree.remove(point.x, point.y, point.z);

        return true;
    }

    @Override
    public void clear() {
        ocTree.clear();
    }

    @Override
    public void finalise() {
        ocTree.optimise();
    }

    @Override
    public void finalise(int z) {
        // No need to implement this, as Octree works on all slices simultaneously

    }
    
    public CoordinateSet duplicate() {
        OctreeCoordinates newCoordinates = new OctreeCoordinates();
        
        // Adding slice by slice
        OcTree newOcTree = new OcTree(ocTree);
        newCoordinates.setOcTree(newOcTree);

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

        for (Point<Integer> point : ocTree)
            if (point.getZ() == slice)
                sliceCoordinateSet.add(point);

        sliceCoordinateSet.finalise();

        return sliceCoordinateSet;

    }

    // Volume properties

    @Override
    public int size() {
        return ocTree.size();
    }

    @Override
    public long getNumberOfElements() {
        return ocTree.getNodeCount();
    }

    // Volume access

    @Override
    public Iterator<Point<Integer>> iterator() {
        return ocTree.iterator();
    }

    protected OcTree getOcTree() {
        return ocTree;
    }

    public void setOcTree(OcTree ocTree) {
        this.ocTree = ocTree;
    }
}
