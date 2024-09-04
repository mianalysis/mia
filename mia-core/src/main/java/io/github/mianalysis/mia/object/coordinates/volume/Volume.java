package io.github.mianalysis.mia.object.coordinates.volume;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.drew.lang.annotations.Nullable;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Roi;
import ij.plugin.filter.ThresholdToSelection;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.process.analysis.CentroidCalculator;
import io.github.mianalysis.mia.process.coordinates.PointSurfaceSeparatorCalculator;
import io.github.mianalysis.mia.process.coordinates.SurfaceSeparationCalculator;
import io.github.mianalysis.mia.process.exceptions.IntegerOverflowException;

public class Volume {
    protected SpatCal spatCal;
    protected CoordinateSet coordinateSet;
    protected Volume surface = null;
    protected Volume projection = null;
    protected Point<Double> meanCentroidPx = null;

    public Volume(VolumeType volumeType, SpatCal spatCal) {
        this.spatCal = spatCal;
        coordinateSet = createCoordinateStore(volumeType);
    }

    public Volume(VolumeType volumeType, int width, int height, int nSlices, double dppXY, double dppZ, String units) {
        this.spatCal = new SpatCal(dppXY, dppZ, units, width, height, nSlices);
        coordinateSet = createCoordinateStore(volumeType);
    }

    CoordinateSet createCoordinateStore(VolumeType type) {
        switch (type) {
            case OCTREE:
                return new OctreeCoordinates();
            case QUADTREE:
                return new QuadtreeCoordinates();
            case POINTLIST:
            default:
                return new PointCoordinates();
        }
    }

    public void add(int x, int y, int z) throws PointOutOfRangeException {
        if (x < 0 || x >= spatCal.width)
            throw new PointOutOfRangeException("Coordinate out of bounds! (x: " + x + ")");
        if (y < 0 || y >= spatCal.height)
            throw new PointOutOfRangeException("Coordinate out of bounds! (y: " + y + ")");
        if (z < 0 || z >= spatCal.nSlices)
            throw new PointOutOfRangeException("Coordinate out of bounds! (z: " + z + ")");

        coordinateSet.add(x, y, z);

    }

    public void add(Point<Integer> point) throws PointOutOfRangeException {
        add(point.x, point.y, point.z);

    }

    public void translateCoords(int xOffs, int yOffs, int zOffs) {
        Volume newVol = new Volume(coordinateSet.getVolumeType(), spatCal);

        // CoordinateSet newCoordinateSet = coordinateSet.createEmptyCoordinateSet();
        for (Point<Integer> point : coordinateSet) {
            try {
                newVol.add(new Point<>(point.getX() + xOffs, point.getY() + yOffs, point.getZ() + zOffs));
            } catch (PointOutOfRangeException e) {
                // Do nothing
            }
        }
        newVol.finalise();

        // Replacing old coordinate set with the transposed one
        this.coordinateSet = newVol.getCoordinateSet();

    }

    public void finalise() {
        coordinateSet.finalise();
    }

    public void finalise(int z) {
        coordinateSet.finalise(z);
    }

    @Deprecated
    public TreeSet<Point<Integer>> getPoints() {
        return new TreeSet<>(coordinateSet);

    }

    public boolean hasCalculatedSurface() {
        return surface != null;
    }

    synchronized public Volume getSurface() {
        return getSurface(false, false);
    }

    synchronized public Volume getSurface(boolean ignoreEdgesXY, boolean ignoreEdgesZ) {
        // If ignoring edges, we want to create a new surface as it's not the "proper"
        // surface. We also don't want to retain this surface, so we return it directly.
        if (ignoreEdgesXY || ignoreEdgesZ) {
            Volume tempSurface = new Volume(VolumeType.POINTLIST, getSpatialCalibration());
            CoordinateSet tempSurfaceCoords = new PointCoordinates();
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
            surface = new Volume(VolumeType.POINTLIST, getSpatialCalibration());
            surface.setCoordinateSet(coordinateSet.calculateSurface(is2D()));
        }

        return surface;

    }

    public Volume getProjected() {
        if (projection == null) {
            VolumeType outputType;
            // Octree is best represented by quadtree. Pointlist can stay as pointlist.
            switch (getVolumeType()) {
                case OCTREE:
                case QUADTREE:
                    outputType = VolumeType.QUADTREE;
                    break;
                case POINTLIST:
                default:
                    outputType = VolumeType.POINTLIST;
                    break;
            }

            projection = new Volume(outputType, spatCal.width, spatCal.height, 1, spatCal.dppXY, spatCal.dppZ,
                    spatCal.units);
            projection.setCoordinateSet(coordinateSet.calculateProjected());

        }

        return projection;

    }

    public boolean hasCalculatedProjection() {
        return projection != null;
    }

    public double getProjectedArea(boolean pixelDistances) {
        int size = getProjected().size();
        return pixelDistances ? size : size * spatCal.dppXY * spatCal.dppXY;

    }

    public Volume getSlice(int slice) {
        VolumeType outputType;
        // Octree is best represented by quadtree. Pointlist can stay as pointlist.
        switch (getVolumeType()) {
            case OCTREE:
            case QUADTREE:
                outputType = VolumeType.QUADTREE;
                break;
            case POINTLIST:
            default:
                outputType = VolumeType.POINTLIST;
                break;
        }

        Volume sliceVol = new Volume(outputType, spatCal.width, spatCal.height, spatCal.nSlices, spatCal.dppXY,
                spatCal.dppZ,
                spatCal.units);
        sliceVol.setCoordinateSet(coordinateSet.getSlice(slice));

        return sliceVol;

    }

    public void setPoints(TreeSet<Point<Integer>> points) throws PointOutOfRangeException {
        for (Point<Integer> point : points)
            add(point);
    }

    public boolean hasCalculatedCentroid() {
        return meanCentroidPx != null;
    }

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

    public Point<Double> getMeanCentroid() {
        return getMeanCentroid(true, false);

    }

    /**
     * Returns true if the current point is on the XY image edge
     */
    public boolean isOnEdgeXY(Point<Integer> p) {
        return (p.x == 0 || p.y == 0 || p.x == getWidth() - 1 || p.y == getHeight() - 1);

    }

    /**
     * Returns true if the current point is on the Z image edge
     */
    public boolean isOnEdgeZ(Point<Integer> p) {
        return (p.z == 0 || p.z == getNSlices() - 1);

    }

    public int size() {
        return coordinateSet.size();
    }

    public boolean contains(Point<Integer> point1) {
        return coordinateSet.contains(point1);
    }

    public long getNumberOfElements() {
        return coordinateSet.getNumberOfElements();
    }

    // PUBLIC METHODS

    public boolean is2D() {
        return spatCal.nSlices == 1;
    }

    @Deprecated
    public ArrayList<Integer> getXCoords() {
        return getPoints().stream().map(Point::getX).collect(Collectors.toCollection(ArrayList::new));

    }

    @Deprecated
    public ArrayList<Integer> getYCoords() {
        return getPoints().stream().map(Point::getY).collect(Collectors.toCollection(ArrayList::new));
    }

    @Deprecated
    public ArrayList<Integer> getZCoords() {
        return getPoints().stream().map(Point::getZ).collect(Collectors.toCollection(ArrayList::new));

    }

    @Deprecated
    public double[] getX(boolean pixelDistances) {
        if (pixelDistances)
            return getPoints().stream().map(Point::getX).mapToDouble(Integer::doubleValue).toArray();
        else
            return getPoints().stream().map(Point::getX).mapToDouble(Integer::doubleValue).map(v -> v * spatCal.dppXY)
                    .toArray();

    }

    @Deprecated
    public double[] getY(boolean pixelDistances) {
        if (pixelDistances)
            return getPoints().stream().map(Point::getY).mapToDouble(Integer::doubleValue).toArray();
        else
            return getPoints().stream().map(Point::getY).mapToDouble(Integer::doubleValue).map(v -> v * spatCal.dppXY)
                    .toArray();

    }

    @Deprecated
    public double[] getZ(boolean pixelDistances, boolean matchXY) {
        if (pixelDistances)
            if (matchXY)
                return getPoints().stream().map(Point::getZ).mapToDouble(Integer::doubleValue)
                        .map(v -> v * spatCal.dppZ / spatCal.dppXY).toArray();

            else
                return getPoints().stream().map(Point::getZ).mapToDouble(Integer::doubleValue).toArray();

        else
            return getPoints().stream().map(Point::getZ).mapToDouble(Integer::doubleValue).map(v -> v * spatCal.dppZ)
                    .toArray();

    }

    @Deprecated
    public ArrayList<Integer> getSurfaceXCoords() {
        return getSurface().getPoints().stream().map(Point::getX).collect(Collectors.toCollection(ArrayList::new));

    }

    @Deprecated
    public ArrayList<Integer> getSurfaceYCoords() {
        return getSurface().getPoints().stream().map(Point::getY).collect(Collectors.toCollection(ArrayList::new));

    }

    @Deprecated
    public ArrayList<Integer> getSurfaceZCoords() {
        return getSurface().getPoints().stream().map(Point::getZ).collect(Collectors.toCollection(ArrayList::new));

    }

    @Deprecated
    public double[] getSurfaceX(boolean pixelDistances) {
        if (pixelDistances)
            return getSurface().getPoints().stream().map(Point::getX).mapToDouble(Integer::doubleValue).toArray();
        else
            return getSurface().getPoints().stream().map(Point::getX).mapToDouble(Integer::doubleValue)
                    .map(v -> v * spatCal.dppXY).toArray();

    }

    @Deprecated
    public double[] getSurfaceY(boolean pixelDistances) {
        if (pixelDistances)
            return getSurface().getPoints().stream().map(Point::getY).mapToDouble(Integer::doubleValue).toArray();
        else
            return getSurface().getPoints().stream().map(Point::getY).mapToDouble(Integer::doubleValue)
                    .map(v -> v * spatCal.dppXY).toArray();
    }

    @Deprecated
    public double[] getSurfaceZ(boolean pixelDistances, boolean matchXY) {
        if (pixelDistances)
            if (matchXY)
                return getSurface().getPoints().stream().map(Point::getZ).mapToDouble(Integer::doubleValue)
                        .map(v -> v * spatCal.dppZ / spatCal.dppXY).toArray();

            else
                return getSurface().getPoints().stream().map(Point::getZ).mapToDouble(Integer::doubleValue).toArray();

        else
            return getSurface().getPoints().stream().map(Point::getZ).mapToDouble(Integer::doubleValue)
                    .map(v -> v * spatCal.dppZ).toArray();

    }

    public double getCalibratedX(Point<Integer> point) {
        return point.getX() * spatCal.dppXY;
    }

    public double getCalibratedY(Point<Integer> point) {
        return point.getY() * spatCal.dppXY;
    }

    public double getXYScaledZ(double z) {
        return z * spatCal.dppZ / spatCal.dppXY;
    }

    public double getXYScaledZ(Point<Integer> point) {
        return point.getZ() * spatCal.dppZ / spatCal.dppXY;
    }

    public double getCalibratedZ(Point<Integer> point, boolean matchXY) {
        if (matchXY)
            return point.getZ() * spatCal.dppZ / spatCal.dppXY;
        else
            return point.getZ() * spatCal.dppZ;
    }

    public double calculatePointPointSeparation(Point<Integer> point1, Point<Integer> point2, boolean pixelDistances) {
        try {
            Volume volume1 = new Volume(VolumeType.POINTLIST, spatCal.duplicate());
            volume1.add(point1.getX(), point1.getY(), point1.getZ());

            Volume volume2 = new Volume(VolumeType.POINTLIST, spatCal.duplicate());
            volume2.add(point2.getX(), point2.getY(), point2.getZ());

            return volume1.getCentroidSeparation(volume2, pixelDistances);

        } catch (IntegerOverflowException | PointOutOfRangeException e) {
            return Double.NaN;
        }
    }

    // public double getSurfaceSeparation(Volume volume2, boolean pixelDistances) {
    // return getSurfaceSeparation(volume2, pixelDistances, false, false);
    // }

    public double getSurfaceSeparation(Volume volume2, boolean pixelDistances, boolean ignoreEdgesXY,
            boolean ignoreEdgesZ) {
        SurfaceSeparationCalculator calculator = new SurfaceSeparationCalculator(this, volume2, false, ignoreEdgesXY,
                ignoreEdgesZ);
        return calculator.getMinDist(pixelDistances);
    }

    // public double getSurfaceSeparation(Volume volume2, boolean pixelDistances,
    // boolean force2D) {
    // return getSurfaceSeparation(volume2, pixelDistances, force2D, false, false);
    // }

    public double getSurfaceSeparation(Volume volume2, boolean pixelDistances, boolean force2D, boolean ignoreEdgesXY,
            boolean ignoreEdgesZ) {
        SurfaceSeparationCalculator calculator = new SurfaceSeparationCalculator(this, volume2, force2D, ignoreEdgesXY,
                ignoreEdgesZ);
        return calculator.getMinDist(pixelDistances);
    }

    // public double getPointSurfaceSeparation(Point<Double> point, boolean
    // pixelDistances) {
    // return getPointSurfaceSeparation(point, pixelDistances, false, false, false);
    // }

    public double getPointSurfaceSeparation(Point<Double> point, boolean pixelDistances, boolean ignoreEdgesXY,
            boolean ignoreEdgesZ) {
        return getPointSurfaceSeparation(point, pixelDistances, false, ignoreEdgesXY, ignoreEdgesZ);
    }

    // public double getPointSurfaceSeparation(Point<Double> point, boolean
    // pixelDistances, boolean force2D) {
    // return getPointSurfaceSeparation(point, pixelDistances, force2D, false,
    // false);
    // }

    public double getPointSurfaceSeparation(Point<Double> point, boolean pixelDistances, boolean force2D,
            boolean ignoreEdgesXY, boolean ignoreEdgesZ) {
        // If this object is only 2D, ensure the Z-position of the point is also zero
        if (is2D() || force2D)
            point = new Point<>(point.x, point.y, 0d);

        PointSurfaceSeparatorCalculator calculator = new PointSurfaceSeparatorCalculator(this, point, ignoreEdgesXY,
                ignoreEdgesZ);
        return calculator.getMinDist(pixelDistances);
    }

    public double getXMean(boolean pixelDistances) {
        return getMeanCentroid(pixelDistances, true).getX();

    }

    public double getYMean(boolean pixelDistances) {
        return getMeanCentroid(pixelDistances, true).getY();

    }

    public double getZMean(boolean pixelDistances, boolean matchXY) {
        return getMeanCentroid(pixelDistances, matchXY).getZ();

    }

    public double getHeight(boolean pixelDistances, boolean matchXY) {
        double minZ = Double.MAX_VALUE;
        double maxZ = Double.MIN_VALUE;

        // Getting XY ranges
        for (Point<Integer> point : coordinateSet) {
            minZ = Math.min(minZ, point.z);
            maxZ = Math.max(maxZ, point.z);
        }

        double height = maxZ - minZ;

        if (pixelDistances)
            return matchXY ? getXYScaledZ(height) : height;
        return height * spatCal.dppZ;

    }

    public double[][] getExtents(boolean pixelDistances, boolean matchXY) {
        if (size() == 0)
            return new double[][] { { 0, 0 }, { 0, 0 }, { 0, 0 } };

        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxZ = Double.MIN_VALUE;

        // Getting XY ranges
        for (Point<Integer> point : coordinateSet) {
            minX = Math.min(minX, point.x);
            maxX = Math.max(maxX, point.x);
            minY = Math.min(minY, point.y);
            maxY = Math.max(maxY, point.y);
            minZ = Math.min(minZ, point.z);
            maxZ = Math.max(maxZ, point.z);
        }

        // Getting XY ranges
        if (pixelDistances) {
            if (matchXY) {
                minZ = getXYScaledZ(minZ);
                maxZ = getXYScaledZ(maxZ);
            }
        } else {
            minX = minX * spatCal.dppXY;
            maxX = maxX * spatCal.dppXY;
            minY = minY * spatCal.dppXY;
            maxY = maxY * spatCal.dppXY;
            minZ = minZ * spatCal.dppZ;
            maxZ = maxZ * spatCal.dppZ;
        }

        if (is2D()) {
            minZ = 0;
            maxZ = 0;
        }

        return new double[][] { { minX, maxX }, { minY, maxY }, { minZ, maxZ } };

    }

    public boolean hasVolume() {
        // True if all dimension (x,y,z) are > 0

        double[][] extents = getExtents(true, false);

        boolean hasvol = false;

        if (extents[0][1] - extents[0][0] > 0 & extents[1][1] - extents[1][0] > 0 & extents[2][1] - extents[2][0] > 0) {
            hasvol = true;
        }

        return hasvol;

    } // Copied

    public boolean hasArea() {
        // True if all dimensions (x,y) are > 0

        double[][] extents = getExtents(true, false);

        boolean hasarea = false;

        if (extents[0][1] - extents[0][0] > 0 & extents[1][1] - extents[1][0] > 0) {
            hasarea = true;
        }

        return hasarea;

    } // Copied

    public double getCentroidSeparation(Volume volume2, boolean pixelDistances) {
        return getCentroidSeparation(volume2, pixelDistances, false);
    }

    public double getCentroidSeparation(Volume volume2, boolean pixelDistances, boolean force2D) {
        double x1 = getXMean(pixelDistances);
        double y1 = getYMean(pixelDistances);
        double x2 = volume2.getXMean(pixelDistances);
        double y2 = volume2.getYMean(pixelDistances);

        // If one or both of the volumes are 2D, only calculate separation in XY
        if (is2D() || volume2.is2D() || force2D) {
            return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));

        } else {
            double z1 = getZMean(pixelDistances, true);
            double z2 = volume2.getZMean(pixelDistances, true);

            return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1) + (z2 - z1) * (z2 - z1));

        }
    }

    // public double getSurfaceSeparation(Volume volume2, boolean pixelDistances) {
    // SurfaceSeparationCalculator calculator = new
    // SurfaceSeparationCalculator(this, volume2, false);
    // return calculator.getMinDist(pixelDistances);
    // }

    // public double getSurfaceSeparation(Volume volume2, boolean pixelDistances,
    // boolean force2D) {
    // SurfaceSeparationCalculator calculator = new
    // SurfaceSeparationCalculator(this, volume2, force2D);
    // return calculator.getMinDist(pixelDistances);
    // }

    // public double getPointSurfaceSeparation(Point<Double> point, boolean
    // pixelDistances) {
    // return getPointSurfaceSeparation(point, pixelDistances, false);
    // }

    // public double getPointSurfaceSeparation(Point<Double> point, boolean
    // pixelDistances, boolean force2D) {
    // // If this object is only 2D, ensure the Z-position of the point is also zero
    // if (is2D() || force2D)
    // point = new Point<>(point.x, point.y, 0d);

    // PointSurfaceSeparatorCalculator calculator = new
    // PointSurfaceSeparatorCalculator(this, point);
    // return calculator.getMinDist(pixelDistances);
    // }

    public double calculateAngle2D(Volume volume2) {
        Point<Double> p1 = new Point<>(getXMean(true), getYMean(true), 0d);
        Point<Double> p2 = new Point<>(volume2.getXMean(true), volume2.getYMean(true), 0d);

        return p1.calculateAngle2D(p2);

    }

    public double calculateAngle2D(Point<Double> point) {
        Point<Double> p1 = new Point<>(getXMean(true), getYMean(true), 0d);

        return p1.calculateAngle2D(point);

    }

    public Volume getOverlappingPoints(Volume volume2) {
        Volume overlapping = new Volume(getVolumeType(), getSpatialCalibration());

        try {
            if (size() < volume2.size()) {
                for (Point<Integer> p1 : coordinateSet)
                    if (volume2.contains(p1))
                        overlapping.add(p1);
            } else {
                for (Point<Integer> p2 : volume2.coordinateSet)
                    if (contains(p2))
                        overlapping.add(p2);
            }
        } catch (IntegerOverflowException | PointOutOfRangeException e) {
            // These points are a subset of the input Volume objects, so if they don't
            // overflow these can't either.
            // Similarly, they can't be out of range.
        }

        return overlapping;

    } // Copied

    public int getOverlap(Volume volume2) {
        int count = 0;

        if (size() < volume2.size()) {
            for (Point<Integer> p1 : coordinateSet)
                if (volume2.contains(p1))
                    count++;
        } else {
            for (Point<Integer> p2 : volume2.coordinateSet)
                if (contains(p2))
                    count++;
        }

        return count;

    } // Copied

    public double getContainedVolume(boolean pixelDistances) {
        if (pixelDistances) {
            return size() * spatCal.dppZ / spatCal.dppXY;
        } else {
            return size() * spatCal.dppXY * spatCal.dppXY * spatCal.dppZ;
        }
    }

    public void clearAllCoordinates() {
        coordinateSet.clear();
        surface = null;
        projection = null;
        meanCentroidPx = null;
    }

    public void clearSurface() {
        surface = null;
    }

    public void clearPoints() {
        coordinateSet.clear();
    }

    public void clearProjected() {
        projection = null;
    }

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

    // GETTERS AND SETTERS

    public SpatCal getSpatialCalibration() {
        return spatCal;
    }

    public void setSpatialCalibration(SpatCal spatCal) {
        this.spatCal = spatCal;
        if (surface != null)
            surface.setSpatialCalibration(spatCal);
        if (projection != null)
            projection.setSpatialCalibration(spatCal);
    }

    public double getDppXY() {
        return spatCal.dppXY;
    }

    public double getDppZ() {
        return spatCal.dppZ;
    }

    public String getUnits() {
        return spatCal.units;
    }

    public int getWidth() {
        return spatCal.width;
    }

    public int getHeight() {
        return spatCal.height;
    }

    public int getNSlices() {
        return spatCal.nSlices;
    }

    public CoordinateSet getCoordinateSet() {
        return coordinateSet;
    }

    public VolumeType getVolumeType() {
        return coordinateSet.getVolumeType();
    }

    public void setCoordinateSet(CoordinateSet coordinateSet) {
        this.coordinateSet = coordinateSet;

        // Calculated properties are now invalid
        clearSurface();
        clearProjected();
        clearCentroid();

    }

    public Roi getRoi(int slice) {
        // Getting the image corresponding to this slice
        Volume sliceVol = getSlice(slice);

        ImageProcessor ipr = sliceVol.getAsTightImage("Crop").getImagePlus().getProcessor();
        ipr.setThreshold(0, 0, ImageProcessor.NO_LUT_UPDATE);
        ipr.invert();

        Prefs.blackBackground = true;

        Roi roi = new ThresholdToSelection().convert(ipr);
        if (roi == null)
            return null;

        double[][] extents = sliceVol.getExtents(true, false);
        roi.translate(extents[0][0], extents[1][0]);

        return roi;

    }

    public Image getAsTightImage(String imageName) {
        int[][] borderWidths = new int[][] { { 0, 0 }, { 0, 0 }, { 0, 0 } };

        return getAsTightImage(imageName, borderWidths);

    }

    public Image getAsTightImage(String imageName, @Nullable int[][] borderWidths) {
        if (borderWidths == null)
            return getAsImage(imageName, 0, 1);

        double[][] extents = getExtents(true, false);
        int xOffs = (int) Math.round(extents[0][0]) - borderWidths[0][0];
        int yOffs = (int) Math.round(extents[1][0]) - borderWidths[1][0];
        int zOffs = (int) Math.round(extents[2][0]) - borderWidths[2][0];

        int width = (int) Math.round(extents[0][1]) - (int) Math.round(extents[0][0]) + borderWidths[0][0]
                + borderWidths[0][1] + 1;
        int height = (int) Math.round(extents[1][1]) - (int) Math.round(extents[1][0]) + borderWidths[1][0]
                + borderWidths[1][1] + 1;
        int nSlices = (int) Math.round(extents[2][1]) - (int) Math.round(extents[2][0]) + borderWidths[2][0]
                + borderWidths[2][1] + 1;

        ImagePlus ipl = IJ.createImage(imageName, width, height, nSlices, 8);
        spatCal.setImageCalibration(ipl);

        // Populating ipl
        for (Point<Integer> point : getCoordinateSet()) {
            ipl.setPosition(point.z - zOffs + 1);
            ipl.getProcessor().putPixel(point.x - xOffs, point.y - yOffs, 255);
        }

        return ImageFactory.createImage("Tight", ipl);

    }

    public Image getAsImage(String imageName, int t, int nFrames) {
        ImagePlus ipl = IJ.createHyperStack(imageName, spatCal.width, spatCal.height, 1, spatCal.nSlices, nFrames, 8);
        spatCal.setImageCalibration(ipl);

        for (Point<Integer> point : getCoordinateSet()) {
            int idx = ipl.getStackIndex(1, point.getZ() + 1, t + 1);
            ipl.getStack().getProcessor(idx).set(point.getX(), point.getY(), 255);
            // ipl.setPosition(point.getZ() + 1);
            // ipl.getProcessor().putPixel(point.getX(), point.getY(), 255);
        }

        return ImageFactory.createImage(imageName, ipl);

    }

    public Iterator<Point<Double>> getCalibratedIterator(boolean pixelDistances, boolean matchXY) {
        return new VolumeIterator(pixelDistances, matchXY);
    }

    public Iterator<Point<Integer>> getCoordinateIterator() {
        return coordinateSet.iterator();
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
