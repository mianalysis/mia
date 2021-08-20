package wbif.sjx.MIA.Module.Visualisation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.SubHyperstackMaker;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary.Skeletonise;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.common.Object.Point;
import wbif.sjx.common.Object.Volume.CoordinateSet;

public class MeasureIntensityProfile {
    public static void process(Obj object, Image image) {
        ImagePlus iplMeas = image.getImagePlus().duplicate();

        // If the input stack is a single timepoint and channel, there's no need to
        // create a new ImageStack
        ImageStack timeStack;
        if (iplMeas.getNChannels() == 1 && iplMeas.getNFrames() == 1) {
            timeStack = iplMeas.getStack();
        } else {
            int t = object.getT() + 1;
            int nSlices = iplMeas.getNSlices();
            timeStack = SubHyperstackMaker.makeSubhyperstack(iplMeas, "1-1", "1-" + nSlices, t + "-" + t).getStack();
        }

        CoordinateSet unorderedPoints = getUnorderedPoints(object);
        if (unorderedPoints == null)
            return;

        LinkedHashSet<Point<Integer>> orderedPoints = orderPoints(unorderedPoints);
        if (orderedPoints == null)
            return;

        for (Point<Integer> point:orderedPoints)
            MIA.log.writeDebug(point.getX() + "_" + point.getY() + "_" + point.getZ());
        
        LinkedHashMap<Double, Double> rawIntensities = measureIntensityProfile(orderedPoints, timeStack);
        MIA.log.writeDebug(" ");
        MIA.log.writeDebug("RAW");
        for (double x : rawIntensities.keySet())
            MIA.log.writeDebug(x + "_" + rawIntensities.get(x));

        LinkedHashMap<Integer, Double> spacedIntensities = interpolateProfile(rawIntensities);
        MIA.log.writeDebug(" ");
        MIA.log.writeDebug("INTERPOLATED");
        for (int x : spacedIntensities.keySet())
            MIA.log.writeDebug(x + "_" + spacedIntensities.get(x));

    }

    protected static CoordinateSet getUnorderedPoints(Obj object) {
        Image skeletonImage = object.getAsTightImage("Skeleton");
        
        // Ensuring the input object is a single line
        Skeletonise.process(skeletonImage, true);
        
        ObjCollection skeletons = skeletonImage.convertImageToObjects("Skeleton");
        if (skeletons.size() == 0)
            return null;
        Obj skeleton = skeletons.getFirst();
        skeleton.setSpatialCalibration(object.getSpatialCalibration());
        
        double[][] extents = object.getExtents(true, false);
        int xOffs = (int) Math.round(extents[0][0]);
        int yOffs = (int) Math.round(extents[1][0]);
        int zOffs = (int) Math.round(extents[2][0]);

        skeleton.translateCoords(xOffs, yOffs, zOffs);
        
        return skeleton.getCoordinateSet();

    }

    protected static LinkedHashSet<Point<Integer>> orderPoints(Collection<Point<Integer>> unorderedPoints) {
        // Determining start point
        Point<Integer> startPoint = getStartPoint(unorderedPoints);
        if (startPoint == null)
            return null;

        // Duplicating points, so we can move them from one list to another without
        // affecting the input object
        ArrayList<Point<Integer>> inputPoints = new ArrayList<>();
        for (Point<Integer> point : unorderedPoints)
            inputPoints.add(new Point<Integer>(point.getX(), point.getY(), point.getZ()));

        // Creating sorted list
        LinkedHashSet<Point<Integer>> sortedPoints = new LinkedHashSet<>();
        sortedPoints.add(startPoint);
        inputPoints.remove(startPoint);

        int prevSize = inputPoints.size();
        while (inputPoints.size() > 0) {
            for (int i = 0; i < inputPoints.size(); i++) {
                Point<Integer> testPoint = inputPoints.get(i);
                if (isNeighbourPoint(testPoint, startPoint, 26)) {
                    sortedPoints.add(testPoint);
                    inputPoints.remove(testPoint);
                    startPoint = testPoint;
                    break;
                }
            }

            // If input point collection hasn't changed size this round then the object
            // isn't a single line
            if (inputPoints.size() == prevSize)
                return null;

            prevSize = inputPoints.size();

        }

        return sortedPoints;

    }

    protected static Point<Integer> getStartPoint(Collection<Point<Integer>> unorderedPoints) {
        Point<Integer> startPoint = null;
        for (Point<Integer> point : unorderedPoints) {
            if (getPointConnectivity(unorderedPoints, point, 26) == 1) {
                startPoint = point;
                break;
            }
        }

        return startPoint;

    }

    public static boolean isNeighbourPoint(Point<Integer> testPoint, Point<Integer> point, int connectivity) {
        int x = point.getX();
        int y = point.getY();
        int z = point.getZ();

        // Testing 6-day connectivity
        if (testPoint.equals(new Point<Integer>(x - 1, y, z)))
            return true;

        if (testPoint.equals(new Point<Integer>(x + 1, y, z)))
            return true;

        if (testPoint.equals(new Point<Integer>(x, y - 1, z)))
            return true;

        if (testPoint.equals(new Point<Integer>(x, y + 1, z)))
            return true;

        if (testPoint.equals(new Point<Integer>(x, y, z - 1)))
            return true;

        if (testPoint.equals(new Point<Integer>(x, y, z + 1)))
            return true;

        // If calculating 6-way connectivity, exit here
        if (connectivity == 6)
            return false;

        if (testPoint.equals(new Point<Integer>(x - 1, y - 1, z - 1)))
            return true;
        if (testPoint.equals(new Point<Integer>(x, y - 1, z - 1)))
            return true;
        if (testPoint.equals(new Point<Integer>(x + 1, y - 1, z - 1)))
            return true;
        if (testPoint.equals(new Point<Integer>(x - 1, y, z - 1)))
            return true;
        if (testPoint.equals(new Point<Integer>(x + 1, y, z - 1)))
            return true;
        if (testPoint.equals(new Point<Integer>(x - 1, y + 1, z - 1)))
            return true;
        if (testPoint.equals(new Point<Integer>(x, y + 1, z - 1)))
            return true;
        if (testPoint.equals(new Point<Integer>(x + 1, y + 1, z - 1)))
            return true;

        if (testPoint.equals(new Point<Integer>(x - 1, y - 1, z)))
            return true;
        if (testPoint.equals(new Point<Integer>(x + 1, y - 1, z)))
            return true;
        if (testPoint.equals(new Point<Integer>(x - 1, y + 1, z)))
            return true;
        if (testPoint.equals(new Point<Integer>(x + 1, y + 1, z)))
            return true;

        if (testPoint.equals(new Point<Integer>(x - 1, y - 1, z + 1)))
            return true;
        if (testPoint.equals(new Point<Integer>(x, y - 1, z + 1)))
            return true;
        if (testPoint.equals(new Point<Integer>(x + 1, y - 1, z + 1)))
            return true;
        if (testPoint.equals(new Point<Integer>(x - 1, y, z + 1)))
            return true;
        if (testPoint.equals(new Point<Integer>(x + 1, y, z + 1)))
            return true;
        if (testPoint.equals(new Point<Integer>(x - 1, y + 1, z + 1)))
            return true;
        if (testPoint.equals(new Point<Integer>(x, y + 1, z + 1)))
            return true;
        if (testPoint.equals(new Point<Integer>(x + 1, y + 1, z + 1)))
            return true;

        return false;

    }

    public static int getPointConnectivity(Collection<Point<Integer>> testPoints, Point<Integer> point, int connectivity) {
        int count = 0;

        int x = point.getX();
        int y = point.getY();
        int z = point.getZ();

        // Testing 6-day connectivity
        if (testPoints.contains(new Point<Integer>(x - 1, y, z)))
            count++;

        if (testPoints.contains(new Point<Integer>(x + 1, y, z)))
            count++;

        if (testPoints.contains(new Point<Integer>(x, y - 1, z)))
            count++;

        if (testPoints.contains(new Point<Integer>(x, y + 1, z)))
            count++;

        if (testPoints.contains(new Point<Integer>(x, y, z - 1)))
            count++;

        if (testPoints.contains(new Point<Integer>(x, y, z + 1)))
            count++;

        // If calculating 6-way connectivity, exit here
        if (connectivity == 6)
            return count;

        if (testPoints.contains(new Point<Integer>(x - 1, y - 1, z - 1)))
            count++;
        if (testPoints.contains(new Point<Integer>(x, y - 1, z - 1)))
            count++;
        if (testPoints.contains(new Point<Integer>(x + 1, y - 1, z - 1)))
            count++;
        if (testPoints.contains(new Point<Integer>(x - 1, y, z - 1)))
            count++;
        if (testPoints.contains(new Point<Integer>(x + 1, y, z - 1)))
            count++;
        if (testPoints.contains(new Point<Integer>(x - 1, y + 1, z - 1)))
            count++;
        if (testPoints.contains(new Point<Integer>(x, y + 1, z - 1)))
            count++;
        if (testPoints.contains(new Point<Integer>(x + 1, y + 1, z - 1)))
            count++;

        if (testPoints.contains(new Point<Integer>(x - 1, y - 1, z)))
            count++;
        if (testPoints.contains(new Point<Integer>(x + 1, y - 1, z)))
            count++;
        if (testPoints.contains(new Point<Integer>(x - 1, y + 1, z)))
            count++;
        if (testPoints.contains(new Point<Integer>(x + 1, y + 1, z)))
            count++;

        if (testPoints.contains(new Point<Integer>(x - 1, y - 1, z + 1)))
            count++;
        if (testPoints.contains(new Point<Integer>(x, y - 1, z + 1)))
            count++;
        if (testPoints.contains(new Point<Integer>(x + 1, y - 1, z + 1)))
            count++;
        if (testPoints.contains(new Point<Integer>(x - 1, y, z + 1)))
            count++;
        if (testPoints.contains(new Point<Integer>(x + 1, y, z + 1)))
            count++;
        if (testPoints.contains(new Point<Integer>(x - 1, y + 1, z + 1)))
            count++;
        if (testPoints.contains(new Point<Integer>(x, y + 1, z + 1)))
            count++;
        if (testPoints.contains(new Point<Integer>(x + 1, y + 1, z + 1)))
            count++;

        return count;

    }

    public static LinkedHashMap<Double, Double> measureIntensityProfile(Collection<Point<Integer>> points,
            ImageStack ist) {
        LinkedHashMap<Double, Double> profile = new LinkedHashMap<>();
        Point<Integer> prevPoint = null;
        double distance = 0;
        for (Point<Integer> point : points) {
            int x = point.getX();
            int y = point.getY();
            int z = point.getZ();

            double intensity = ist.getVoxel(x, y, z);

            if (prevPoint != null)
                distance += prevPoint.calculateDistanceToPoint(point);

            MIA.log.writeDebug(x + "_" + y + "_" + z + "_" + distance + "_" + intensity);

            profile.put(distance, intensity);

            prevPoint = point;

        }

        return profile;

    }

    public static LinkedHashMap<Integer, Double> interpolateProfile(LinkedHashMap<Double, Double> profile) {
        LinkedHashMap<Integer, Double> interpolated = new LinkedHashMap<>();

        // Converting to double arrays
        double[] x = profile.keySet().stream().mapToDouble(Double::doubleValue).toArray();
        double[] y = profile.values().stream().mapToDouble(Double::doubleValue).toArray();

        PolynomialSplineFunction spline = new LinearInterpolator().interpolate(x, y);

        int max = (int) Math.floor(x[x.length - 1]);
        for (int i = 0; i <= max; i++)
            interpolated.put(i, spline.value(i));

        return interpolated;

    }
}