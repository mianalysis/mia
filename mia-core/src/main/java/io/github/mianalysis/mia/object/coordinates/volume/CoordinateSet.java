package io.github.mianalysis.mia.object.coordinates.volume;

import java.util.AbstractSet;


import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.process.math.CumStat;

public abstract class CoordinateSet extends AbstractSet<Point<Integer>> {
    public abstract boolean add(int x, int y, int z);

    public abstract void finalise();

    public abstract void finalise(int z);
    
    public abstract CoordinateSet duplicate();

    public abstract long getNumberOfElements();

    public abstract VolumeType getVolumeType();

    public abstract CoordinateSet createEmptyCoordinateSet();
    
    protected abstract CoordinateSet calculateProjected();

    public CoordinateSet calculateSurface(boolean is2D) {
        return is2D ? calculateSurface2D() : calculateSurface3D();
    }

    protected CoordinateSet calculateSurface2D() {
        CoordinateSet surface = new PointCoordinates();

        // Iterating over each Point, adding it if it has fewer than 4 neighbours
        for (Point<Integer> point : this) {
                if (!contains(new Point<>(point.x - 1, point.y, 0))) {
                    surface.add(point);
                    continue;
                }
                    
                if (!contains(new Point<>(point.x + 1, point.y, 0))) {
                    surface.add(point);
                    continue;
                }
                    
                if (!contains(new Point<>(point.x, point.y - 1, 0))) {
                    surface.add(point);
                    continue;
                }
                    
                if (!contains(new Point<>(point.x, point.y + 1, 0))) {
                    surface.add(point);
                    continue;
                }
                
        }

        return surface;

    }

    protected CoordinateSet calculateSurface3D() {
        CoordinateSet surface = new PointCoordinates();

        // Iterating over each Point, adding it if it has fewer than 6 neighbours
        for (Point<Integer> point : this) {
            if (!contains(new Point<>(point.x - 1, point.y, point.z))) {
                surface.add(point);
                continue;
            }

            if (!contains(new Point<>(point.x + 1, point.y, point.z))) {
                surface.add(point);
                continue;
            }

            if (!contains(new Point<>(point.x, point.y - 1, point.z))) {
                surface.add(point);
                continue;
            }

            if (!contains(new Point<>(point.x, point.y + 1, point.z))) {
                surface.add(point);
                continue;
            }

            if (!contains(new Point<>(point.x, point.y, point.z - 1))) {
                surface.add(point);
                continue;
            }

            if (!contains(new Point<>(point.x, point.y, point.z + 1))) {
                surface.add(point);
                continue;
            }

        }

        return surface;

    }

    public abstract CoordinateSet getSlice(int slice);

    public Point<Double> calculateMeanCentroid() {
        CumStat csX = new CumStat();
        CumStat csY = new CumStat();
        CumStat csZ = new CumStat();

        for (Point<Integer> point : this) {
            csX.addMeasure(point.x);
            csY.addMeasure(point.y);
            csZ.addMeasure(point.z);
        }

        return new Point<>(csX.getMean(), csY.getMean(), csZ.getMean());

    }
}
