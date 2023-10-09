package io.github.mianalysis.mia.process.math;


/**
 * Created by sc13967 on 27/03/2018.
 */
public class GaussianDistribution2D {
    private final double x0; // centroid x
    private final double y0; // centroid y
    private final double sx; // sigma x
    private final double sy; // sigma y
    private final double A0; // peak amplitude
    private final double ABG; // background amplitude
    private final double th; // theta

    public GaussianDistribution2D(double x0, double y0, double sx, double sy, double A0, double ABG, double th) {
        this.x0 = x0;
        this.y0 = y0;
        this.sx = sx;
        this.sy = sy;
        this.A0 = A0;
        this.ABG = ABG;
        this.th = th;
    }

    public double[] getValues(double x, double y) {
        // The value of the function at this location
        double a = (Math.cos(th) * Math.cos(th)) / (2 * sx * sx) + (Math.sin(th) * Math.sin(th)) / (2 * sy * sy);
        double b = Math.sin(2 * th) / (4 * sy * sy) - Math.sin(2 * th) / (4 * sx * sx);
        double c = (Math.cos(th) * Math.cos(th)) / (2 * sy * sy) + (Math.sin(th) * Math.sin(th)) / (2 * sx * sx);
        double val = ABG + A0 * Math.exp(-(a * ((x - x0) * (x - x0)) + 2 * b * (x - x0) * (y - y0) + c * ((y - y0) * (y - y0))));

        return new double[]{val,a,b,c};

    }
}
