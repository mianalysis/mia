package io.github.mianalysis.mia.object.coordinates.volume;

import java.util.Iterator;

import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.process.analysis.CentroidCalculator;

public class Volume implements VolumeI {
    protected SpatCal spatCal;
    protected CoordinateSetI coordinateSet;
    protected Volume surface = null;
    protected Volume projection = null;
    protected Point<Double> meanCentroidPx = null;

    public Volume(CoordinateSetFactoryI factory, SpatCal spatCal) {
        this.spatCal = spatCal;
        coordinateSet = factory.createCoordinateSet();
    }

    public Volume(CoordinateSetFactoryI factory, int width, int height, int nSlices, double dppXY, double dppZ,
            String units) {
        this.spatCal = new SpatCal(dppXY, dppZ, units, width, height, nSlices);
        coordinateSet = factory.createCoordinateSet();
    }

    @Override
    public CoordinateSetFactoryI getFactory() {
        return coordinateSet.getFactory();
    }

    @Override
    public VolumeI getSurface(boolean ignoreEdgesXY, boolean ignoreEdgesZ) {
        // If ignoring edges, we want to create a new surface as it's not the "proper"
        // surface. We also don't want to retain this surface, so we return it directly.
        if (ignoreEdgesXY || ignoreEdgesZ) {
            Volume tempSurface = new Volume(new PointListFactory(), getSpatialCalibration());
            CoordinateSetI tempSurfaceCoords = new PointCoordinates();
            for (Point<Integer> pt : coordinateSet.calculateSurface(is2D())) {
                if (ignoreEdgesXY && isOnEdgeXY(pt))
                    continue;

                if (ignoreEdgesZ && isOnEdgeZ(pt))
                    continue;

                tempSurfaceCoords.add(pt);

            }

            tempSurface.setCoordinateSet(tempSurfaceCoords);

            return tempSurface;

        }

        if (surface == null) {
            surface = new Volume(new PointListFactory(), getSpatialCalibration());
            surface.setCoordinateSet(coordinateSet.calculateSurface(is2D()));
        }

        return surface;

    }

    @Override
    public boolean hasCalculatedSurface() {
        return surface != null;
    }

    @Override
    public VolumeI getProjected() {
        if (projection == null) {
            // Octree is best represented by quadtree. Pointlist can stay as pointlist.
            CoordinateSetFactoryI factory = getFactory();
            if (factory instanceof OctreeFactory)
                factory = new QuadtreeFactory();

            projection = new Volume(factory, spatCal.width, spatCal.height, 1, spatCal.dppXY, spatCal.dppZ,
                    spatCal.units);

            projection.setCoordinateSet(coordinateSet.calculateProjected());

        }

        return projection;

    }

    @Override
    public boolean hasCalculatedProjection() {
        return projection != null;
    }

    @Override
    public Point<Double> getMeanCentroid(boolean pixelDistances, boolean matchXY) {
        if (meanCentroidPx == null)
            meanCentroidPx = CentroidCalculator.calculateMeanCentroid(coordinateSet);

        if (pixelDistances) {
            if (matchXY) {
                // Keeping X and Y, but changing Z to match
                double xCent = meanCentroidPx.getX();
                double yCent = meanCentroidPx.getY();
                double zCent = getXYScaledZ(meanCentroidPx.getZ());

                return new Point<Double>(xCent, yCent, zCent);

            } else {
                // Using raw X,Y,Z units
                return meanCentroidPx.duplicate();
            }
        }

        // Converting to calibrated units
        double xCent = meanCentroidPx.getX() * spatCal.dppXY;
        double yCent = meanCentroidPx.getY() * spatCal.dppXY;
        double zCent = meanCentroidPx.getZ() * spatCal.dppZ;

        return new Point<Double>(xCent, yCent, zCent);

    }

    @Override
    public boolean hasCalculatedCentroid() {
        return meanCentroidPx != null;
    }

    // PUBLIC METHODS

    @Override
    public void clearAllCoordinates() {
        coordinateSet.clear();
        surface = null;
        projection = null;
        meanCentroidPx = null;
    }

    @Override
    public void clearSurface() {
        surface = null;
    }

    @Override
    public void clearPoints() {
        coordinateSet.clear();
    }

    @Override
    public void clearProjected() {
        projection = null;
    }

    @Override
    public void clearCentroid() {
        meanCentroidPx = null;
    }

    @Override
    public int hashCode() {
        int hash = coordinateSet.hashCode();

        hash = 31 * hash + ((Number) spatCal.dppXY).hashCode();
        hash = 31 * hash + ((Number) spatCal.dppZ).hashCode();
        hash = 31 * hash + spatCal.units.toUpperCase().hashCode();

        return hash;

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof Volume))
            return false;

        Volume volume = (Volume) obj;

        if (spatCal.dppXY != volume.getDppXY())
            return false;
        if (spatCal.dppZ != volume.getDppZ())
            return false;
        if (!spatCal.units.toUpperCase().equals(volume.getUnits().toUpperCase()))
            return false;

        return coordinateSet.equals(volume.coordinateSet);

    }

    @Override
    public SpatCal getSpatialCalibration() {
        return spatCal;
    }

    @Override
    public void setSpatialCalibration(SpatCal spatCal) {
        this.spatCal = spatCal;
        if (surface != null)
            surface.setSpatialCalibration(spatCal);
        if (projection != null)
            projection.setSpatialCalibration(spatCal);
    }

    @Override
    public CoordinateSetI getCoordinateSet() {
        return coordinateSet;
    }

    @Override
    public void setCoordinateSet(CoordinateSetI coordinateSet) {
        this.coordinateSet = coordinateSet;

        // Calculated properties are now invalid
        clearSurface();
        clearProjected();
        clearCentroid();

    }
    
    @Override
    public VolumeI createNewVolume(CoordinateSetFactoryI factory, SpatCal spatCal) {
        return new Volume(factory, spatCal);
    }

    @Override
    public Iterator<Point<Double>> getCalibratedIterator(boolean pixelDistances, boolean matchXY) {
        return new VolumeIterator(pixelDistances, matchXY);
    }

    private class VolumeIterator implements Iterator<Point<Double>> {
        private Iterator<Point<Integer>> iterator;
        private boolean pixelDistances;
        private boolean matchXY;

        public VolumeIterator(boolean pixelDistances, boolean matchXY) {
            this.pixelDistances = pixelDistances;
            this.iterator = coordinateSet.iterator();
            this.matchXY = matchXY;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Point<Double> next() {
            Point<Integer> nextPoint = iterator.next();
            int x = nextPoint.x;
            int y = nextPoint.y;
            int z = nextPoint.z;

            if (pixelDistances && matchXY) {
                return new Point<>((double) x, (double) y, (double) z * spatCal.dppZ / spatCal.dppXY);
            } else if (pixelDistances & !matchXY) {
                return new Point<>((double) x, (double) y, (double) z);
            } else {
                return new Point<>((double) x * spatCal.dppXY, (double) y * spatCal.dppXY, (double) z * spatCal.dppZ);
            }
        }
    }
}
