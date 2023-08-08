package io.github.mianalysis.mia.object;

import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;

public interface VolumeTypesInterface {
    String OCTREE = "Octree";
    // String OPTIMISED = "Optimised";
    String POINTLIST = "Pointlist";
    String QUADTREE = "Quadtree";

    // String[] ALL = new String[]{OCTREE, OPTIMISED, POINTLIST, QUADTREE};
    String[] ALL = new String[] { OCTREE, POINTLIST, QUADTREE };

    public static VolumeType getVolumeType(String volumeType) {
        switch (volumeType) {
            case OCTREE:
                return VolumeType.OCTREE;
            // case OPTIMISED:                
            default:
            case POINTLIST:
                return VolumeType.POINTLIST;
            case QUADTREE:
                return VolumeType.QUADTREE;
        }
    }

}
