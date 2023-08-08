package io.github.mianalysis.mia.object.coordinates.tracks;

import io.github.mianalysis.mia.object.coordinates.Point;

/**
 * Created by sc13967 on 22/09/2017.
 */
public class Timepoint<T extends Number> extends Point<T> {
    /**
     *
     */
    private static final long serialVersionUID = 6896202704428034687L;
    protected int f;

    public Timepoint(T x, T y, T z, int f) {
        super(x, y, z);
        this.f = f;

    }

    public int getF() {
        return f;
    }

    public void setF(int f) {
        this.f = f;
    }

    @Override
    public int hashCode() {
        int hash = 1;

        hash = 31*hash + x.hashCode();
        hash = 31*hash + y.hashCode();
        hash = 31*hash + z.hashCode();
        hash = 31*hash + f;

        return hash;

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Timepoint)) return false;

        Timepoint timepoint = (Timepoint) obj;

        double x1 = x.doubleValue();
        double x2 = timepoint.x.doubleValue();
        double y1 = y.doubleValue();
        double y2 = timepoint.y.doubleValue();
        double z1 = z.doubleValue();
        double z2 = timepoint.z.doubleValue();

        return (Math.abs(x1-x2) < Math.max(1E-5,Math.abs(x1)*1e-5))
                && (Math.abs(y1-y2) < Math.max(1E-5,Math.abs(y1)*1e-5))
                && (Math.abs(z1-z2) < Math.max(1E-5,Math.abs(z1)*1e-5))
                && f==timepoint.f;

    }
}
