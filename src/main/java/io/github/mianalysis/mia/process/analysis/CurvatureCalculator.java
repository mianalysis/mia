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
    private ArrayList<Point<Integer>> path;
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

    public CurvatureCalculator(ArrayList<Point<Integer>> path, boolean isLoop) {
        this.path = path;
        this.isLoop = isLoop;

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

        int count = 0;
        Point<Integer> prevVertex = null;

        // If a loop, starting with the final few points from the opposite end
        if (isLoop) {
            int len = path.size();
            for (int i = NNeighbours; i > 0; i--) {
                Point<Integer> vertex = path.get((len - i));
                t[count] = count == 0 ? 0 : t[count - 1] + vertex.calculateDistanceToPoint(prevVertex);
                x[count] = vertex.getX();
                y[count++] = vertex.getY();

                prevVertex = vertex;

            }
        }

        for (Point<Integer> vertex : path) {
            t[count] = count == 0 ? 0 : t[count - 1] + vertex.calculateDistanceToPoint(prevVertex);
            x[count] = vertex.getX();
            y[count++] = vertex.getY();

            prevVertex = vertex;

        }
        
        // If a loop, ending with the first few points from the opposite end
        if (isLoop) {
            for (int i = 0; i < NNeighbours; i++) {
                Point<Integer> vertex = path.get(i);
                t[count] = count == 0 ? 0 : t[count - 1] + vertex.calculateDistanceToPoint(prevVertex);
                x[count] = vertex.getX();
                y[count++] = vertex.getY();

                prevVertex = vertex;

            }
        }

        // Storing splines
        splines = new PolynomialSplineFunction[6];

        // Fitting the spline pair
        switch (fittingMethod) {
            case LOESS:
                LoessInterpolator loessInterpolator = new LoessInterpolator(loessBandwidth, loessIterations,
                        loessAccuracy);
                splines[0] = loessInterpolator.interpolate(t, x);
                splines[1] = loessInterpolator.interpolate(t, y);
                break;

            case STANDARD:
                SplineInterpolator splineInterpolator = new SplineInterpolator();
                splines[0] = splineInterpolator.interpolate(t, x);
                splines[1] = splineInterpolator.interpolate(t, y);
                break;
        }

        // Calculating curvature
        splines[2] = splines[0].polynomialSplineDerivative(); // dx
        splines[3] = splines[2].polynomialSplineDerivative(); // ddx
        splines[4] = splines[1].polynomialSplineDerivative(); // dy
        splines[5] = splines[4].polynomialSplineDerivative(); // ddy

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
            double dy = (splines[1].value(maxPos) - splines[1].value(minPos)) / width;
            double ddx = (splines[2].value(maxPos) - splines[2].value(minPos)) / (0.5 * width);
            double ddy = (splines[3].value(maxPos) - splines[3].value(minPos)) / (0.5 * width);

            double k = (dx * ddy - dy * ddx) / Math.pow((dx * dx + dy * dy), 3d / 2d);

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
            double y1 = splines[1].value(p1);
            double x2 = splines[0].value(p2);
            double y2 = splines[1].value(p2);

            double k1 = Math.abs(curvature.get(p1));
            double k2 = Math.abs(curvature.get(p2));
            double k = (k1 + k2) / 2;
            double b = k / (maxCurvature * 1.5);
            Color color = Color.getHSBColor((float) b, 1f, 1f);

            Line line = new Line(x1, y1, x2, y2);
            line.setStrokeWidth(lineWidth);
            line.setStrokeColor(color);
            line.setPosition(position[2]);
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

    public ArrayList<Point<Integer>> getSpline() {
        if (splines == null)
            calculateCurvature();

        ArrayList<Point<Integer>> spline = new ArrayList<>();

        double[] knots = splines[0].getKnots();
        int startIdx = isLoop ? NNeighbours : 0;
        int endIdx = isLoop ? knots.length - NNeighbours : knots.length;
        for (int i = startIdx; i < endIdx; i++) {
            double t = knots[i];
        
            int x = (int) Math.round(splines[0].value(t));
            int y = (int) Math.round(splines[1].value(t));

            spline.add(new Point<Integer>(x, y, 0));

        }

        return spline;

    }
}
