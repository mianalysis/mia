package io.github.mianalysis.mia.process.coordinates;

import java.util.Iterator;

import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeI;

public class SurfaceSeparationCalculator {
    private double dppXY;
    private double minDist;
    private Point<Integer> p1;
    private Point<Integer> p2;

    // public SurfaceSeparationCalculator(Volume v1, Volume v2) {
    //     calculate(v1, v2, false, false, false);
    // }

    // public SurfaceSeparationCalculator(Volume v1, Volume v2, boolean force2D) {
    //     calculate(v1, v2, force2D, false, false);
    // }

    public SurfaceSeparationCalculator(VolumeI v1, VolumeI v2, boolean ignoreEdgesXY, boolean ignoreEdgesZ) {
        calculate(v1, v2, false, ignoreEdgesXY, ignoreEdgesZ);
    }

    public SurfaceSeparationCalculator(VolumeI v1, VolumeI v2, boolean force2D, boolean ignoreEdgesXY,
            boolean ignoreEdgesZ) {
        calculate(v1, v2, force2D, ignoreEdgesXY, ignoreEdgesZ);
    }

    protected void calculate(VolumeI v1, VolumeI v2, boolean force2D, boolean ignoreEdgesXY, boolean ignoreEdgesZ) {
        this.dppXY = v1.getDppXY();
        double minDist = Double.MAX_VALUE;
        Point<Integer> p1 = null;
        Point<Integer> p2 = null;

        Iterator<Point<Integer>> iterator2 = v2.getSurface(false,false).getCoordinateIterator();

        // If one or both of the volumes are 2D, only calculate separation in XY
        boolean only2D = v1.is2D() || v2.is2D() || force2D;

        while (iterator2.hasNext()) {
            Point<Integer> pp2 = iterator2.next();

            if (ignoreEdgesXY && v2.isOnEdgeXY(pp2))
                continue;

            if (ignoreEdgesZ && v2.isOnEdgeZ(pp2))
                continue;

            boolean isInside = false;

            Iterator<Point<Integer>> iterator1 = v1.getSurface(false,false).getCoordinateIterator();
            while (iterator1.hasNext()) {
                Point<Integer> pp1 = iterator1.next();

                if (ignoreEdgesXY && v1.isOnEdgeXY(pp1))
                    continue;

                if (ignoreEdgesZ && v1.isOnEdgeZ(pp1))
                    continue;

                double xDist = pp2.x - pp1.x;
                double yDist = pp2.y - pp1.y;

                double dist;
                if (only2D) {
                    dist = Math.sqrt(xDist * xDist + yDist * yDist);

                    if (dist < Math.abs(minDist)) {
                        minDist = dist;
                        p1 = pp1;
                        p2 = pp2;

                        isInside = v1.contains(new Point<>(pp2.x, pp2.y, 0));
                        if (!isInside)
                            isInside = v2.contains(new Point<>(pp1.x, pp1.y, 0));

                    }

                } else {
                    double zDist = v2.getXYScaledZ(pp2.z) - v1.getXYScaledZ(pp1.z);
                    dist = Math.sqrt(xDist * xDist + yDist * yDist + zDist * zDist);

                    if (dist < Math.abs(minDist)) {
                        minDist = dist;
                        p1 = pp1;
                        p2 = pp2;

                        isInside = v1.contains(pp2);
                        if (!isInside)
                            isInside = v2.contains(pp1);

                    }
                }
            }

            // If this point is inside the parent the distance should be negative
            if (isInside)
                minDist = -minDist;

        }

        // Setting the optimal values
        this.minDist = minDist;
        this.p1 = p1;
        this.p2 = p2;

    }

    public double getMinDist(boolean pixelDistances) {
        return pixelDistances ? minDist : minDist * dppXY;
    }

    public Point<Integer> getP1() {
        return p1;
    }

    public Point<Integer> getP2() {
        return p2;
    }
}
