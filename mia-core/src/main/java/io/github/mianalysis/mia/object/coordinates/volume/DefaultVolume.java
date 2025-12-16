package io.github.mianalysis.mia.object.coordinates.volume;

import java.util.Iterator;

import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.SpatiallyCalibrated;
import io.github.mianalysis.mia.process.analysis.CentroidCalculator;

public class DefaultVolume implements VolumeI {
    protected int width;
    protected int height;
    protected int nSlices;
    protected double dppXY;
    protected double dppZ;
    protected String spatialUnits;
    protected CoordinateSetI coordinateSet;
    protected VolumeI surface = null;
    protected VolumeI projection = null;
    protected Point<Double> meanCentroidPx = null;

    public DefaultVolume(CoordinateSetFactoryI factory, int width, int height, int nSlices, double dppXY, double dppZ,
            String spatialUnits) {
        this.coordinateSet = factory.createCoordinateSet();

        this.width = width;
        this.height = height;
        this.nSlices = nSlices;
        this.dppXY = dppXY;
        this.dppZ = dppZ;
        this.spatialUnits = spatialUnits;

    }

    public DefaultVolume(CoordinateSetFactoryI factory, SpatiallyCalibrated example) {
        this.coordinateSet = factory.createCoordinateSet();

        this.width = example.getWidth();
        this.height = example.getHeight();
        this.nSlices = example.getNSlices();
        this.dppXY = example.getDppXY();
        this.dppZ = example.getDppZ();
        this.spatialUnits = example.getSpatialUnits();

    }

    @Override
    public VolumeFactoryI getFactory() {
        return new DefaultVolumeFactory();
    }

    @Override
    public CoordinateSetFactoryI getCoordinateSetFactory() {
        return getCoordinateSet().getFactory();
    }

    @Override
    public VolumeI getSurface(boolean ignoreEdgesXY, boolean ignoreEdgesZ) {
        // If ignoring edges, we want to create a new surface as it's not the "proper"
        // surface. We also don't want to retain this surface, so we return it directly.
        if (ignoreEdgesXY || ignoreEdgesZ) {
            VolumeI tempSurface = createNewVolume(new PointListFactory(), this);
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
            surface = createNewVolume(new PointListFactory(), this);
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
            CoordinateSetFactoryI factory = getCoordinateSet().getFactory();

            if (factory instanceof OctreeFactory)
                factory = new QuadtreeFactory();

            projection = getFactory().createVolume(factory, width, height, nSlices, dppXY, dppZ, spatialUnits);

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
        double xCent = meanCentroidPx.getX() * getDppXY();
        double yCent = meanCentroidPx.getY() * getDppXY();
        double zCent = meanCentroidPx.getZ() * getDppZ();

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

        hash = 31 * hash + ((Number) getDppXY()).hashCode();
        hash = 31 * hash + ((Number) getDppZ()).hashCode();
        hash = 31 * hash + getSpatialUnits().toUpperCase().hashCode();

        return hash;

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        
        if (!(obj instanceof VolumeI))
            return false;

        VolumeI volume = (VolumeI) obj;

        if (getDppXY() != volume.getDppXY())
            return false;
        if (getDppZ() != volume.getDppZ())
            return false;
        if (!getSpatialUnits().toUpperCase().equals(volume.getSpatialUnits().toUpperCase()))
            return false;

        return coordinateSet.equals(volume.getCoordinateSet());

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
                return new Point<>((double) x, (double) y, (double) z * getDppZ() / getDppXY());
            } else if (pixelDistances & !matchXY) {
                return new Point<>((double) x, (double) y, (double) z);
            } else {
                return new Point<>((double) x * getDppXY(), (double) y * getDppXY(), (double) z * getDppZ());
            }
        }
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public int getNSlices() {
        return nSlices;
    }

    @Override
    public void setNSlices(int nSlices) {
        this.nSlices = nSlices;
    }

    @Override
    public double getDppXY() {
        return dppXY;
    }

     @Override
    public void setDppXY(double dppXY) {
        this.dppXY = dppXY;
    }

    @Override
    public double getDppZ() {
        return dppZ;
    }

    @Override
    public void setDppZ(double dppZ) {
        this.dppZ = dppZ;
    }

    @Override
    public String getSpatialUnits() {
        return spatialUnits;
    }

    @Override
    public void setSpatialUnits(String spatialUnits) {
        this.spatialUnits = spatialUnits;
    }

    @Override
    public VolumeI createNewVolume(CoordinateSetFactoryI coordinateSetFactory, VolumeI exampleVolume) {
        return new DefaultVolume(coordinateSetFactory, exampleVolume.getWidth(), exampleVolume.getHeight(),
                exampleVolume.getNSlices(), exampleVolume.getDppXY(), exampleVolume.getDppZ(),
                exampleVolume.getSpatialUnits());
    }
}
