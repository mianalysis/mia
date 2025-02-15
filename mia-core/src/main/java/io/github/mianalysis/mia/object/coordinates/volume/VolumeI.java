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
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.process.coordinates.PointSurfaceSeparatorCalculator;
import io.github.mianalysis.mia.process.coordinates.SurfaceSeparationCalculator;
import io.github.mianalysis.mia.process.exceptions.IntegerOverflowException;

public interface VolumeI {
    public default void initialise(CoordinateSetFactoryI factory, SpatCal spatCal) {
        setSpatialCalibration(spatCal);
        setCoordinateSet(factory.createCoordinateSet());
    }

    public default void initialise(CoordinateSetFactoryI factory, int width, int height, int nSlices, double dppXY,
            double dppZ, String units) {
        initialise(factory, new SpatCal(dppXY, dppZ, units, width, height, nSlices));
    }

    public default Iterator<Point<Integer>> getCoordinateIterator() {
        return getCoordinateSet().iterator();
    }

    public default void add(int x, int y, int z) throws PointOutOfRangeException {
        if (x < 0 || x >= getWidth())
            throw new PointOutOfRangeException("Coordinate out of bounds! (x: " + x + ")");
        if (y < 0 || y >= getHeight())
            throw new PointOutOfRangeException("Coordinate out of bounds! (y: " + y + ")");
        if (z < 0 || z >= getNSlices())
            throw new PointOutOfRangeException("Coordinate out of bounds! (z: " + z + ")");

        getCoordinateSet().add(x, y, z);

    }

    public default void add(Point<Integer> point) throws PointOutOfRangeException {
        add(point.x, point.y, point.z);

    }

    public default void translateCoords(int xOffs, int yOffs, int zOffs) {
        VolumeI newVol = createNewVolume(getFactory(), getSpatialCalibration());

        // CoordinateSet newCoordinateSet = coordinateSet.createEmptyCoordinateSet();
        for (Point<Integer> point : getCoordinateSet()) {
            try {
                newVol.add(point.getX() + xOffs, point.getY() + yOffs, point.getZ() + zOffs);
            } catch (PointOutOfRangeException e) {
                // Do nothing
            }
        }
        newVol.finalise();

        // Replacing old coordinate set with the transposed one
        setCoordinateSet(newVol.getCoordinateSet());

    }

    public default void finalise() {
        getCoordinateSet().finalise();
    }

    public default void finalise(int z) {
        getCoordinateSet().finalise(z);
    }

    @Deprecated
    public default TreeSet<Point<Integer>> getPoints() {
        System.err.println("REMOVE VolumeI.getPoints()");
        return new TreeSet<>(getCoordinateSet());

    }

    public default double getDppXY() {
        return getSpatialCalibration().dppXY;
    }

    public default double getDppZ() {
        return getSpatialCalibration().dppZ;
    }

    public default String getUnits() {
        return getSpatialCalibration().units;
    }

    public default int getWidth() {
        return getSpatialCalibration().getWidth();
    }

    public default int getHeight() {
        return getSpatialCalibration().getHeight();
    }

    public default int getNSlices() {
        return getSpatialCalibration().getNSlices();
    }

    public default VolumeI getSurface() {
        return getSurface(false, false);
    }

    public default double getProjectedArea(boolean pixelDistances) {
        int size = getProjected().size();
        return pixelDistances ? size : size * getSpatialCalibration().dppXY * getSpatialCalibration().dppXY;

    }

    public default int size() {
        return getCoordinateSet().size();
    }

    public default void setPoints(TreeSet<Point<Integer>> points) throws PointOutOfRangeException {
        for (Point<Integer> point : points)
            add(point.getX(),point.getY(),point.getZ());
    }

    public default Point<Double> getMeanCentroid() {
        return getMeanCentroid(true, false);

    }

    /**
     * Returns true if the current point is on the XY image edge
     */
    public default boolean isOnEdgeXY(Point<Integer> p) {
        return (p.x == 0 || p.y == 0 || p.x == getWidth() - 1 || p.y == getHeight() - 1);

    }

    /**
     * Returns true if the current point is on the Z image edge
     */
    public default boolean isOnEdgeZ(Point<Integer> p) {
        return (p.z == 0 || p.z == getNSlices() - 1);

    }

    public default boolean contains(Point<Integer> point1) {
        return getCoordinateSet().contains(point1);
    }

    public default long getNumberOfElements() {
        return getCoordinateSet().getNumberOfElements();
    }

    public default boolean is2D() {
        return getSpatialCalibration().nSlices == 1;
    }

    public default double getCalibratedX(Point<Integer> point) {
        return point.getX() * getSpatialCalibration().dppXY;
    }

    public default double getCalibratedY(Point<Integer> point) {
        return point.getY() * getSpatialCalibration().dppXY;
    }

    public default double getXYScaledZ(double z) {
        SpatCal spatCal = getSpatialCalibration();

        return z * spatCal.dppZ / spatCal.dppXY;
    }

    public default double getXYScaledZ(Point<Integer> point) {
        SpatCal spatCal = getSpatialCalibration();

        return point.getZ() * spatCal.dppZ / spatCal.dppXY;
    }

    public default double getCalibratedZ(Point<Integer> point, boolean matchXY) {
        SpatCal spatCal = getSpatialCalibration();

        if (matchXY)
            return point.getZ() * spatCal.dppZ / spatCal.dppXY;
        else
            return point.getZ() * spatCal.dppZ;
    }

    public default double[][] getExtents(boolean pixelDistances, boolean matchXY) {
        SpatCal spatCal = getSpatialCalibration();

        if (size() == 0)
            return new double[][] { { 0, 0 }, { 0, 0 }, { 0, 0 } };

        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxZ = Double.MIN_VALUE;

        // Getting XY ranges
        for (Point<Integer> point : getCoordinateSet()) {
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

    public default ImageI getAsImage(String imageName, int t, int nFrames) {
        SpatCal spatCal = getSpatialCalibration();

        ImagePlus ipl = IJ.createHyperStack(imageName, spatCal.width, spatCal.height, 1, spatCal.nSlices, nFrames, 8);
        spatCal.applyImageCalibration(ipl);

        for (Point<Integer> point : getCoordinateSet()) {
            int idx = ipl.getStackIndex(1, point.getZ() + 1, t + 1);
            ipl.getStack().getProcessor(idx).set(point.getX(), point.getY(), 255);
            // ipl.setPosition(point.getZ() + 1);
            // ipl.getProcessor().putPixel(point.getX(), point.getY(), 255);
        }

        return ImageFactory.createImage(imageName, ipl);

    }

    public default ImageI getAsTightImage(String imageName) {
        int[][] borderWidths = new int[][] { { 0, 0 }, { 0, 0 }, { 0, 0 } };

        return getAsTightImage(imageName, borderWidths);

    }

    public default ImageI getAsTightImage(String imageName, @Nullable int[][] borderWidths) {
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
        getSpatialCalibration().applyImageCalibration(ipl);

        // Populating ipl
        for (Point<Integer> point : getCoordinateSet()) {
            ipl.setPosition(point.z - zOffs + 1);
            ipl.getProcessor().putPixel(point.x - xOffs, point.y - yOffs, 255);
        }

        return ImageFactory.createImage("Tight", ipl);

    }

    public default double getContainedVolume(boolean pixelDistances) {
        SpatCal spatCal = getSpatialCalibration();

        if (pixelDistances) {
            return size() * spatCal.dppZ / spatCal.dppXY;
        } else {
            return size() * spatCal.dppXY * spatCal.dppXY * spatCal.dppZ;
        }
    }

    public default int getOverlap(Volume volume2) {
        int count = 0;

        if (size() < volume2.size()) {
            for (Point<Integer> p1 : getCoordinateSet())
                if (volume2.contains(p1))
                    count++;
        } else {
            for (Point<Integer> p2 : volume2.coordinateSet)
                if (contains(p2))
                    count++;
        }

        return count;

    }

    @Deprecated
    public default double[] getX(boolean pixelDistances) {
        if (pixelDistances)
            return getPoints().stream().map(Point::getX).mapToDouble(Integer::doubleValue).toArray();
        else
            return getPoints().stream().map(Point::getX).mapToDouble(Integer::doubleValue).map(v -> v * getSpatialCalibration().dppXY)
                    .toArray();

    }

    @Deprecated
    public default double[] getY(boolean pixelDistances) {
        if (pixelDistances)
            return getPoints().stream().map(Point::getY).mapToDouble(Integer::doubleValue).toArray();
        else
            return getPoints().stream().map(Point::getY).mapToDouble(Integer::doubleValue).map(v -> v * getSpatialCalibration().dppXY)
                    .toArray();

    }

    @Deprecated
    public default double[] getZ(boolean pixelDistances, boolean matchXY) {
        SpatCal spatCal = getSpatialCalibration();

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
    public default ArrayList<Integer> getSurfaceXCoords() {
        return getSurface().getPoints().stream().map(Point::getX).collect(Collectors.toCollection(ArrayList::new));

    }

    @Deprecated
    public default ArrayList<Integer> getSurfaceYCoords() {
        return getSurface().getPoints().stream().map(Point::getY).collect(Collectors.toCollection(ArrayList::new));

    }

    @Deprecated
    public default ArrayList<Integer> getSurfaceZCoords() {
        return getSurface().getPoints().stream().map(Point::getZ).collect(Collectors.toCollection(ArrayList::new));

    }

    @Deprecated
    public default double[] getSurfaceX(boolean pixelDistances) {
        if (pixelDistances)
            return getSurface().getPoints().stream().map(Point::getX).mapToDouble(Integer::doubleValue).toArray();
        else
            return getSurface().getPoints().stream().map(Point::getX).mapToDouble(Integer::doubleValue)
                    .map(v -> v * getSpatialCalibration().dppXY).toArray();

    }

    @Deprecated
    public default double[] getSurfaceY(boolean pixelDistances) {
        if (pixelDistances)
            return getSurface().getPoints().stream().map(Point::getY).mapToDouble(Integer::doubleValue).toArray();
        else
            return getSurface().getPoints().stream().map(Point::getY).mapToDouble(Integer::doubleValue)
                    .map(v -> v * getSpatialCalibration().dppXY).toArray();
    }

    @Deprecated
    public default double[] getSurfaceZ(boolean pixelDistances, boolean matchXY) {
        SpatCal spatCal = getSpatialCalibration();

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

    public default double getXMean(boolean pixelDistances) {
        return getMeanCentroid(pixelDistances, true).getX();

    }

    public default double getYMean(boolean pixelDistances) {
        return getMeanCentroid(pixelDistances, true).getY();

    }

    public default double getZMean(boolean pixelDistances, boolean matchXY) {
        return getMeanCentroid(pixelDistances, matchXY).getZ();

    }

    public default double getHeight(boolean pixelDistances, boolean matchXY) {
        double minZ = Double.MAX_VALUE;
        double maxZ = Double.MIN_VALUE;

        // Getting XY ranges
        for (Point<Integer> point : getCoordinateSet()) {
            minZ = Math.min(minZ, point.z);
            maxZ = Math.max(maxZ, point.z);
        }

        double height = maxZ - minZ;

        if (pixelDistances)
            return matchXY ? getXYScaledZ(height) : height;
            
        return height * getSpatialCalibration().dppZ;

    }

    public default boolean hasVolume() {
        // True if all dimension (x,y,z) are > 0

        double[][] extents = getExtents(true, false);

        return (extents[0][1] - extents[0][0] > 0 & extents[1][1] - extents[1][0] > 0 & extents[2][1] - extents[2][0] > 0);

    }

    public default boolean hasArea() {
        // True if all dimensions (x,y) are > 0

        double[][] extents = getExtents(true, false);

        return (extents[0][1] - extents[0][0] > 0 & extents[1][1] - extents[1][0] > 0);

    }

    public default double calculateAngle2D(Volume volume2) {
        Point<Double> p1 = new Point<>(getXMean(true), getYMean(true), 0d);
        Point<Double> p2 = new Point<>(volume2.getXMean(true), volume2.getYMean(true), 0d);

        return p1.calculateAngle2D(p2);

    }

    public default double calculateAngle2D(Point<Double> point) {
        Point<Double> p1 = new Point<>(getXMean(true), getYMean(true), 0d);

        return p1.calculateAngle2D(point);

    }

    public default double calculatePointPointSeparation(Point<Integer> point1, Point<Integer> point2, boolean pixelDistances) {
        try {
            Volume volume1 = new Volume(new PointListFactory(), getSpatialCalibration().duplicate());
            volume1.add(point1.getX(), point1.getY(), point1.getZ());

            Volume volume2 = new Volume(new PointListFactory(), getSpatialCalibration().duplicate());
            volume2.add(point2.getX(), point2.getY(), point2.getZ());

            return volume1.getCentroidSeparation(volume2, pixelDistances);

        } catch (IntegerOverflowException | PointOutOfRangeException e) {
            return Double.NaN;
        }
    }

    public default double getSurfaceSeparation(Volume volume2, boolean pixelDistances, boolean ignoreEdgesXY,
            boolean ignoreEdgesZ) {
        SurfaceSeparationCalculator calculator = new SurfaceSeparationCalculator(this, volume2, false, ignoreEdgesXY,
                ignoreEdgesZ);
        return calculator.getMinDist(pixelDistances);
    }

    public default double getSurfaceSeparation(Volume volume2, boolean pixelDistances, boolean force2D, boolean ignoreEdgesXY,
            boolean ignoreEdgesZ) {
        SurfaceSeparationCalculator calculator = new SurfaceSeparationCalculator(this, volume2, force2D, ignoreEdgesXY,
                ignoreEdgesZ);
        return calculator.getMinDist(pixelDistances);
    }
    public default double getPointSurfaceSeparation(Point<Double> point, boolean pixelDistances, boolean ignoreEdgesXY,
            boolean ignoreEdgesZ) {
        return getPointSurfaceSeparation(point, pixelDistances, false, ignoreEdgesXY, ignoreEdgesZ);
    }

    public default double getPointSurfaceSeparation(Point<Double> point, boolean pixelDistances, boolean force2D,
            boolean ignoreEdgesXY, boolean ignoreEdgesZ) {
        // If this object is only 2D, ensure the Z-position of the point is also zero
        if (is2D() || force2D)
            point = new Point<>(point.x, point.y, 0d);

        PointSurfaceSeparatorCalculator calculator = new PointSurfaceSeparatorCalculator(this, point, ignoreEdgesXY,
                ignoreEdgesZ);
                
        return calculator.getMinDist(pixelDistances);
    }

    public default double getCentroidSeparation(Volume volume2, boolean pixelDistances) {
        return getCentroidSeparation(volume2, pixelDistances, false);
    }

    public default double getCentroidSeparation(Volume volume2, boolean pixelDistances, boolean force2D) {
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

    public default Volume getOverlappingPoints(Volume volume2) {
        Volume overlapping = new Volume(getCoordinateSet().getFactory(), getSpatialCalibration());

        try {
            if (size() < volume2.size()) {
                for (Point<Integer> p1 : getCoordinateSet())
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

    }

    public default VolumeI getSlice(int slice) {
        SpatCal spatCal = getSpatialCalibration();

        // Octree is best represented by quadtree. Pointlist can stay as pointlist.
        CoordinateSetFactoryI factory = getFactory();
        if (factory instanceof OctreeFactory)
            factory = new QuadtreeFactory();

        VolumeI sliceVol = new Volume(factory, spatCal.width, spatCal.height, spatCal.nSlices, spatCal.dppXY,
                spatCal.dppZ,
                spatCal.units);
        sliceVol.setCoordinateSet(getCoordinateSet().getSlice(slice));

        return sliceVol;

    }

    public default Roi getRoi(int slice) {
        // Getting the image corresponding to this slice
        VolumeI sliceVol = getSlice(slice);

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

    public CoordinateSetFactoryI getFactory();

    public VolumeI createNewVolume(CoordinateSetFactoryI factory, SpatCal spatCal);

    public SpatCal getSpatialCalibration();

    public void setSpatialCalibration(SpatCal spatCal);   

    public CoordinateSetI getCoordinateSet();

    public void setCoordinateSet(CoordinateSetI coordinateSet);

    public VolumeI getSurface(boolean ignoreEdgesXY, boolean ignoreEdgesZ);

    public boolean hasCalculatedSurface();

    public VolumeI getProjected();

    public boolean hasCalculatedProjection();

    public boolean hasCalculatedCentroid();

    public Point<Double> getMeanCentroid(boolean pixelDistances, boolean matchXY);

    public Iterator<Point<Double>> getCalibratedIterator(boolean pixelDistances, boolean matchXY);

    public void clearAllCoordinates();

    public void clearSurface();

    public void clearPoints();

    public void clearProjected();

    public void clearCentroid();

}
