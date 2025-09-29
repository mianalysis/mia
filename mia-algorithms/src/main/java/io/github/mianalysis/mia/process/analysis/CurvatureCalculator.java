package io.github.mianalysis.mia.process.analysis;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Overlay;
import io.github.mianalysis.mia.object.coordinates.Point;

/**
 * Created by sc13967 on 26/01/2018.
 */
public class CurvatureCalculator {
    private ArrayList<Point<Double>> path;
    private PolynomialSplineFunction[] splines = null;
    private TreeMap<Double, Double> curvature = null;
    private FittingMethod fittingMethod = FittingMethod.STANDARD;
    private boolean isLoop = false;

    public enum FittingMethod {
        STANDARD, LOESS
    }

    private int NNeighbours = 5;
    private double loessBandwidth = 0.04;
    private int loessIterations = 0;
    private double loessAccuracy = 100;
    private boolean is2D = true;

    public CurvatureCalculator(ArrayList<Point<Double>> path, boolean isLoop, boolean is2D) {
        this.path = path;
        this.isLoop = isLoop;
        this.is2D = is2D;
    }

    public void calculateCurvature() {
        // Checking there are enough points for fitting
        int nKnots = path.size();
        if (nKnots < (NNeighbours + 1))
            return;

        if (isLoop)
            nKnots = nKnots + 2 * NNeighbours;

        // Preparing line coordinates for spline fitting
        double[] t = new double[nKnots];
        double[] x = new double[nKnots];
        double[] y = new double[nKnots];
        double[] z = new double[nKnots]; // Always defining this to please the linter

        int count = 0;
        Point<Double> prevVertex = null;

        // If a loop, starting with the final few points from the opposite end
        if (isLoop) {
            int len = path.size();
            for (int i = NNeighbours; i > 0; i--) {
                Point<Double> vertex = path.get((len - i));
                t[count] = count == 0 ? 0 : t[count - 1] + vertex.calculateDistanceToPoint(prevVertex);
                x[count] = vertex.getX();
                y[count] = vertex.getY();

                if (!is2D)
                    z[count] = vertex.getZ();

                count++;

                prevVertex = vertex;

            }
        }

        for (Point<Double> vertex : path) {
            t[count] = count == 0 ? 0 : t[count - 1] + vertex.calculateDistanceToPoint(prevVertex);
            x[count] = vertex.getX();
            y[count] = vertex.getY();

            if (!is2D)
                z[count] = vertex.getZ();

            count++;

            prevVertex = vertex;

        }

        // If a loop, ending with the first few points from the opposite end
        if (isLoop) {
            for (int i = 0; i < NNeighbours; i++) {
                Point<Double> vertex = path.get(i);
                t[count] = count == 0 ? 0 : t[count - 1] + vertex.calculateDistanceToPoint(prevVertex);
                x[count] = vertex.getX();
                y[count] = vertex.getY();

                if (!is2D)
                    z[count] = vertex.getZ();

                count++;

                prevVertex = vertex;

            }
        }

        // Storing splines x, dx, y, dy, z, dz
        splines = is2D ? new PolynomialSplineFunction[4] : new PolynomialSplineFunction[6];

        // Fitting the spline pair
        switch (fittingMethod) {
            case LOESS:
                LoessInterpolator loessInterpolator = new LoessInterpolator(loessBandwidth, loessIterations,
                        loessAccuracy);
                splines[0] = loessInterpolator.interpolate(t, x);
                splines[2] = loessInterpolator.interpolate(t, y);

                if (!is2D)
                    splines[4] = loessInterpolator.interpolate(t, z);
                break;

            case STANDARD:
                SplineInterpolator splineInterpolator = new SplineInterpolator();
                splines[0] = splineInterpolator.interpolate(t, x);
                splines[2] = splineInterpolator.interpolate(t, y);
                if (!is2D)
                    splines[4] = splineInterpolator.interpolate(t, z);
                break;
        }

        // Calculating curvature
        splines[1] = splines[0].polynomialSplineDerivative(); // dx
        splines[3] = splines[2].polynomialSplineDerivative(); // dy
        if (!is2D)
            splines[5] = splines[4].polynomialSplineDerivative(); // dz
        
        // Extracting the gradients as a function of position along the curve
        double[] knots = splines[0].getKnots();

        curvature = new TreeMap<>();
        double w = (double) NNeighbours / 2d;
        int startIdx = isLoop ? NNeighbours : 0;
        int endIdx = isLoop ? knots.length - NNeighbours : knots.length;
        for (int i = startIdx; i < endIdx; i++) {
            double pos = knots[i];

            double minPos = Math.max(knots[0], pos - w);
            double maxPos = Math.min(knots[knots.length - 1], pos + w);
            double width = maxPos - minPos;

            double dx = (splines[0].value(maxPos) - splines[0].value(minPos)) / width;
            double ddx = (splines[1].value(maxPos) - splines[1].value(minPos)) / width;
            double dy = (splines[2].value(maxPos) - splines[2].value(minPos)) / width;
            double ddy = (splines[3].value(maxPos) - splines[3].value(minPos)) / width;

            double k;
            if (is2D) {
                k = (dx * ddy - dy * ddx) / Math.pow((dx * dx + dy * dy), 3d / 2d);
            } else {
                double dz = (splines[4].value(maxPos) - splines[4].value(minPos)) / width;
                double ddz = (splines[5].value(maxPos) - splines[5].value(minPos)) / width;
                double term1 = (ddz * dy - ddy * dz) * (ddz * dy - ddy * dz);
                double term2 = (ddx * dz - ddz * dx) * (ddx * dz - ddz * dx);
                double term3 = (ddy * dx - ddx * dy) * (ddy * dx - ddx * dy);
                k = Math.sqrt(term1 + term2 + term3) / Math.pow((dx * dx + dy * dy + dz * dz), 3d / 2d);
            }

            curvature.put(knots[i], k);

        }
    }

    public TreeMap<Double, Double> getCurvature() {
        if (curvature == null)
            calculateCurvature();

        return curvature;

    }

    public void showOverlay(ImagePlus ipl, int[] position, int lineWidth) {
        if (curvature == null)
            calculateCurvature();

        // If fitting failed, curvature will still be null
        if (curvature == null)
            return;

        // Calculating maximum curvature
        double maxCurvature = Double.MIN_VALUE;
        for (double currentCurvature : curvature.values()) {
            maxCurvature = Math.max(Math.abs(currentCurvature), maxCurvature);
        }

        showOverlay(ipl, maxCurvature, position, lineWidth);

    }

    public void showOverlay(ImagePlus ipl, double maxCurvature, int[] position, double lineWidth) {
        if (curvature == null)
            calculateCurvature();

        Overlay ovl = ipl.getOverlay();
        if (ovl == null)
            ovl = new Overlay();

        if (curvature.size() < 2) {
            ipl.setOverlay(ovl);
            return;
        }

        Iterator<Double> iterator = curvature.keySet().iterator();
        double p2 = iterator.next();

        while (iterator.hasNext()) {
            double p1 = p2;
            p2 = iterator.next();

            double x1 = splines[0].value(p1);
            double y1 = splines[2].value(p1);
            double x2 = splines[0].value(p2);
            double y2 = splines[2].value(p2);

            double k1 = Math.abs(curvature.get(p1));
            double k2 = Math.abs(curvature.get(p2));
            double k = (k1 + k2) / 2;
            double b = k / (maxCurvature * 1.5);
            Color color = Color.getHSBColor((float) b, 1f, 1f);

            Line line = new Line(x1, y1, x2, y2);
            line.setStrokeWidth(lineWidth);
            line.setStrokeColor(color);
            line.setPosition(position[0], position[1], position[2]);
            ovl.addElement(line);

        }

        ipl.setOverlay(ovl);

    }

    public int getNNeighbours() {
        return NNeighbours;
    }

    public void setNNeighbours(int NNeighbours) {
        this.NNeighbours = NNeighbours;

        // Calculating the bandwidth
        loessBandwidth = (double) NNeighbours / (double) path.size();

        // Keeping the bandwidth within limits
        if (loessBandwidth < 0)
            loessBandwidth = 0;
        if (loessBandwidth > 1)
            loessBandwidth = 1;

    }

    public double getLoessBandwidth() {
        return loessBandwidth;
    }

    public int getLoessIterations() {
        return loessIterations;
    }

    public void setLoessIterations(int loessIterations) {
        this.loessIterations = loessIterations;
    }

    public double getLoessAccuracy() {
        return loessAccuracy;
    }

    public void setLoessAccuracy(double loessAccuracy) {
        this.loessAccuracy = loessAccuracy;
    }

    public FittingMethod getFittingMethod() {
        return fittingMethod;
    }

    public void setFittingMethod(FittingMethod fittingMethod) {
        this.fittingMethod = fittingMethod;
    }

    public TreeMap<Point<Double>,Double> getSpline() {
        if (splines == null)
            calculateCurvature();

        TreeMap<Point<Double>,Double> spline = new TreeMap<>();

        double[] knots = splines[0].getKnots();
        int startIdx = isLoop ? NNeighbours : 0;
        int endIdx = isLoop ? knots.length - NNeighbours : knots.length;
        for (int i = startIdx; i < endIdx; i++) {
            double t = knots[i];

            double x = splines[0].value(t);
            double y = splines[2].value(t);
            double z = is2D ? 0d : splines[4].value(t);

            spline.put(new Point<Double>(x, y, z),curvature.get(t));

        }

        return spline;

    }
}
