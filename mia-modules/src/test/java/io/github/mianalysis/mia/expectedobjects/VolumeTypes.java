package io.github.mianalysis.mia.expectedobjects;

import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetFactoryI;
import io.github.mianalysis.mia.object.coordinates.volume.OctreeFactory;
import io.github.mianalysis.mia.object.coordinates.volume.PointListFactory;
import io.github.mianalysis.mia.object.coordinates.volume.QuadtreeFactory;

public enum VolumeTypes {
    POINTLIST, QUADTREE, OCTREE;

    public static CoordinateSetFactoryI getFactory(VolumeTypes volumeType) {
        switch (volumeType) {
            case POINTLIST:
                return new PointListFactory();
            case QUADTREE:
                return new QuadtreeFactory();
            case OCTREE:
                return new OctreeFactory();
            default:
                return new PointListFactory();
        }
    }
}
