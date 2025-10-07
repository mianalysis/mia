package io.github.mianalysis.mia.object.coordinates.volume;

import java.util.Iterator;
import java.util.Set;

import io.github.mianalysis.mia.object.coordinates.Point;

public interface CoordinateSetI extends Set<Point<Integer>> {

    public CoordinateSetFactoryI getFactory();

    public boolean addCoord(int x, int y, int z);

    public void finalise();

    public void finaliseSlice(int z);

    public long getNumberOfElements();

    public CoordinateSetI getSlice(int slice);

    public CoordinateSetI createEmptyCoordinateSet();

    public CoordinateSetI duplicate();
    
    public CoordinateSetI calculateProjected();

    public Iterator<Point<Integer>> iterator();

    public boolean contains(Point<Integer> point);

    public default CoordinateSetI calculateSurface(boolean is2D) {
        return is2D ? calculateSurface2D() : calculateSurface3D();
    }

    public default CoordinateSetI calculateSurface2D() {
        CoordinateSetI surface = new PointCoordinates();

        // Iterating over each Point, adding it if it has fewer than 4 neighbours
        Iterator<Point<Integer>> iterator = iterator();

        while (iterator.hasNext()) {
            Point<Integer> point = iterator.next();

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

    public default CoordinateSetI calculateSurface3D() {
        CoordinateSetI surface = new PointCoordinates();

        // Iterating over each Point, adding it if it has fewer than 6 neighbours
        Iterator<Point<Integer>> iterator = iterator();

        while (iterator.hasNext()) {
            Point<Integer> point = iterator.next();

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
}
