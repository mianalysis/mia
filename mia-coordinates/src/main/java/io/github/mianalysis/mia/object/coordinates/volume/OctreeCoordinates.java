package io.github.mianalysis.mia.object.coordinates.volume;

import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.volume.quadtree.Octree;


/**
 * Created by sc13967 on 28/07/2017.
 */
public class OctreeCoordinates extends Octree implements CoordinateSetI {
    public OctreeCoordinates() {
    
    }

    public OctreeCoordinates(Octree ocTree) {
        super(ocTree);
    }

    @Override
    public CoordinateSetFactoryI getFactory() {
        return new OctreeFactory();
    }

    // Adding and removing points

    @Override
    public CoordinateSetI createEmptyCoordinateSet() {
        return new OctreeCoordinates();
    }

    @Override
    public void finaliseSlice(int z) {
        // No need to implement this, as Octree works on all slices simultaneously
    }
    
    @Override
    public CoordinateSetI duplicate() {
        OctreeCoordinates newCoordinates = new OctreeCoordinates(this);
        
        // Adding slice by slice
        Octree newOctree = new Octree(this);
        

        return newCoordinates;

    }

    // Creating coordinate subsets
    @Override
    public CoordinateSetI calculateProjected() {
        CoordinateSetI projectedCoordinates = new QuadtreeCoordinates();

        for (Point<Integer> point : this)
            projectedCoordinates.addCoord(point.x, point.y, 0);

        projectedCoordinates.finalise();

        return projectedCoordinates;

    }

    @Override
    public CoordinateSetI getSlice(int slice) {
        CoordinateSetI sliceCoordinateSet = new QuadtreeCoordinates();

        for (Point<Integer> point : this)
            if (point.getZ() == slice)
                sliceCoordinateSet.add(point);

        sliceCoordinateSet.finalise();

        return sliceCoordinateSet;

    }

    // Volume properties

    @Override
    public long getNumberOfElements() {
        return getNodeCount();
    }

    // Volume access

    @Override
    public void finalise() {
        // No need to implement this, as Octree works on all slices simultaneously
    }
}
