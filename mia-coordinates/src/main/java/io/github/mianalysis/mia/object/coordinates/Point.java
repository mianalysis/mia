package io.github.mianalysis.mia.object.coordinates;

import java.io.Serializable;

public class Point<T extends Number> implements Comparable<Point<T>>, Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 391260737545673548L;
    public T x;
    public T y;
    public T z;

    public Point(T x, T y, T z) {
        this.x = x;
        this.y = y;
        this.z = z;

    }

    public T getX() {
        return x;
    }

    public T getY() {
        return y;
    }

    public T getZ() {
        return z;
    }

    public void setX(T x) {
        this.x = x;

    }

    public void setY(T y) {
        this.y = y;

    }

    public void setZ(T z) {
        this.z = z;

    }

    public double calculateDistanceToPoint(Point<T> point) {
        double x1 = x.doubleValue();
        double x2 = point.getX().doubleValue();
        double y1 = y.doubleValue();
        double y2 = point.getY().doubleValue();
        double z1 = z.doubleValue();
        double z2 = point.getZ().doubleValue();

        return Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1)+(z2-z1)*(z2-z1));

    }

    public double calculateAngle2D(Point<T> point) {
        double x1 = x.doubleValue();
        double x2 = point.getX().doubleValue();
        double y1 = y.doubleValue();
        double y2 = point.getY().doubleValue();

        return Math.atan2((y2-y1),(x2-x1));

    }

    @Override
    public int hashCode() {
        int hash = 1;

        hash = 31*hash + x.hashCode();
        hash = 31*hash + y.hashCode();
        hash = 31*hash + z.hashCode();

        return hash;

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof Point))
            return false;

        Point point = (Point) obj;
        return x.equals(point.x) && y.equals(point.y) && z.equals(point.z);

    }

    public Point<T> duplicate() {
        return new Point<T>(x, y, z);
    }

    @Override
    public int compareTo(Point<T> point) {
        double x1 = x.doubleValue();
        double x2 = point.getX().doubleValue();
        double y1 = y.doubleValue();
        double y2 = point.getY().doubleValue();
        double z1 = z.doubleValue();
        double z2 = point.getZ().doubleValue();

        if (x1 > x2) {
            return 1;
        } else if (x1 < x2) {
            return -1;
        } else {
            if (y1 > y2) {
                return 1;
            } else if (y1 < y2) {
                return -1;
            } else {
                if (z1 > z2) {
                    return 1;
                } else if (z1 < z2){
                    return -1;
                }
            }
        }

        return 0;

    }

    @Override
    public String toString() {
        return getClass().getName() + " xyz = (" + x + ", " + y + ", " + z + ')';
    }
}