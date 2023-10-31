package io.github.mianalysis.mia.process.math;

// THIS CLASS IS BASED ON THE INCREMENTAL CALCULATION OF WEIGHTED MEAN AND VARIANCE FROM
// http://www-uxsup.csx.cam.ac.uk/~fanf2/hermes/doc/antiforgery/stats.pdf (Accessed 30-06-2016).

import java.util.Collection;

public class CumStat {
    public static final int POPULATION = 1;
    public static final int SAMPLE = 2;

    private double xMean = 0;
    private double xVarPop = 0;
    private double xVarSamp = 0;
    private double n = 0;
    private double wSum = 0;
    private double S = 0;
    private double xSum = 0;
    private double xMin = Double.MAX_VALUE;
    private double xMax = -Double.MAX_VALUE;

    public CumStat() {
    }

    public CumStat(double[] vals) {
        for (double val:vals) addMeasure(val);
    }

    public CumStat(float[] vals) {
        for (float val:vals) addMeasure(val);
    }

    public CumStat(double[] vals, boolean ignoreZeroes) {
        for (double val:vals) addMeasure(val,ignoreZeroes);
    }

    public CumStat(float[] vals, boolean ignoreZeroes) {
        for (float val:vals) addMeasure(val,ignoreZeroes);
    }

    public CumStat(double[] vals, double[] weights) {
        for (int i=0;i<vals.length;i++) addMeasure(vals[i],weights[i]);
    }

    public CumStat(float[] vals, double[] weights) {
        for (int i=0;i<vals.length;i++) addMeasure(vals[i],weights[i]);
    }

    public CumStat(double[] vals, double[] weights, boolean ignoreZeroes) {
        for (int i=0;i<vals.length;i++) addMeasure(vals[i],weights[i],ignoreZeroes);
    }

    public CumStat(float[] vals, double[] weights, boolean ignoreZeroes) {
        for (int i=0;i<vals.length;i++) addMeasure(vals[i],weights[i],ignoreZeroes);
    }

    public CumStat(Collection<Double> vals) {
        for (double val:vals) addMeasure(val);
    }

    public synchronized void addMeasure(double xIn) {
        addMeasure(xIn, 1, false);
    }

    public synchronized void addMeasure(double xIn, boolean ignoreZeroes) {
        addMeasure(xIn, 1, ignoreZeroes);
    }

    public synchronized void addMeasure(double xIn, double w) {
        addMeasure(xIn, w, false);
    }

    public synchronized void addMeasure(double xIn, double w, boolean ignoreZeroes) {
        if(ignoreZeroes & xIn == 0) {
            xIn = 0 / 0.0;
        }

        if(!Double.isNaN(xIn) & w != 0) {
            ++n;
            xSum += xIn;
            wSum += w;
            double x_mean_prev = xMean;
            xMean += w / wSum * (xIn - xMean);
            S += w * (xIn - x_mean_prev) * (xIn - xMean);
            xVarPop = S / wSum;
            xVarSamp = S / (wSum - 1);
            if(xIn < xMin) {
                xMin = xIn;
            }

            if(xIn > xMax) {
                xMax = xIn;
            }
        }

    }

    public synchronized void addMeasures(double[] xIn) {
        for(int i = 0; i < xIn.length; ++i) addMeasure(xIn[i], 1);
    }

    public synchronized void addMeasures(float[] xIn) {
        for(int i = 0; i < xIn.length; ++i) addMeasure(xIn[i], 1);
    }

    public synchronized void addMeasures(double[] xIn, double[] w) {
        for(int i = 0; i < xIn.length; ++i) addMeasure(xIn[i], w[i]);
    }

    public synchronized void addMeasures(float[] xIn, float[] w) {
        for(int i = 0; i < xIn.length; ++i) addMeasure(xIn[i], w[i]);
    }

    public synchronized double getMean() {
        return n == 0 ? Double.NaN : xMean;
    }

    public synchronized double getSum() {
        return n == 0 ? Double.NaN : xSum;
    }

    public synchronized double getVar() {
        return n == 0 ? Double.NaN : xVarSamp;
    }

    public synchronized double getVar(int mode) {
        if (n == 0) return Double.NaN;

        return mode == 2?xVarSamp:(mode == 1?xVarPop:0);

    }

    public synchronized double getStd() {
        return n == 0 ? Double.NaN : getStd(2);
    }

    public synchronized double getStd(int mode) {
        if (n == 0) return Double.NaN;

        return mode == 2?Math.sqrt(xVarSamp):(mode == 1?Math.sqrt(xVarPop):0);

    }

    public synchronized double getN() {
        return n;
    }

    public synchronized double getMin() {
        return n == 0 ? Double.NaN : xMin;
    }

    public synchronized double getMax() {
        return n == 0 ? Double.NaN : xMax;
    }

    public synchronized double getWeight() {
        return n == 0 ? Double.NaN : wSum;
    }
}
