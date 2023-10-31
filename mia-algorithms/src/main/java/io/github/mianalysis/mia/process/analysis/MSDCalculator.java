package io.github.mianalysis.mia.process.analysis;

import java.util.TreeMap;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import io.github.mianalysis.mia.process.math.CumStat;

/**
 * Created by steph on 15/04/2017.
 */
public class MSDCalculator {
    public static TreeMap<Integer,CumStat> calculate(TreeMap<Integer,CumStat> cumStats, int[] f, double[] x, double[] y, double[] z) {
        for (int j = 0; j < f.length; j++) {//Incrementing over all frames with the possibility for that time step
            for (int k = j; k < f.length; k++) {
                int df = f[k]-f[j];
                double dx = x[k] - x[j];
                double dy = y[k] - y[j];
                double dz = z[k] - z[j];

                double val = dx * dx + dy * dy + dz * dz;

                cumStats.putIfAbsent(df,new CumStat());
                cumStats.get(df).addMeasure(val);

            }
        }

        return cumStats;

    }

    public static TreeMap<Integer,CumStat> calculate(int[] f, double[] x, double[] y, double[] z) {
        TreeMap<Integer,CumStat> cumStats = new TreeMap<>();
        return calculate(cumStats,f,x,y,z);

    }

    public static double[] getLinearFit(TreeMap<Integer,CumStat> msd, int nPoints) {
        int[] df = msd.keySet().stream().mapToInt(v->v).toArray();
        double[] MSD = msd.values().stream().mapToDouble(CumStat::getMean).toArray();

        return getLinearFit(df,MSD,nPoints);

    }

    public static double[] getLinearFit(int[] df, double[] msd, int nPoints) {
        msd[0] = 0; //There should be no values with zero time-gap, so this would otherwise be NaN

        if (df.length < nPoints) nPoints = df.length;

        double[][] data = new double[nPoints][2];
        for (int j=0;j<nPoints;j++) {
            data[j][0] = df[j];
            data[j][1] = msd[j];
        }

        SimpleRegression sr = new SimpleRegression();
        sr.addData(data);

        return new double[]{sr.getSlope(),sr.getIntercept(),nPoints};

    }

}
