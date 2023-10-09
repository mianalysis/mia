package io.github.mianalysis.mia.process.analysis;

import java.util.TreeMap;

/**
 * Created by steph on 15/04/2017.
 */
public class EuclideanDistanceCalculator implements SpatialCalculator {
    public TreeMap<Integer,Double> calculate(double[] x, double[] y, double[] z, int[] f) {
        TreeMap<Integer,Double> dist = new TreeMap<>();

        dist.put(f[0],0d);
        for (int i=0;i<x.length;i++) {
            double dx = (double) x[i]-(double) x[0];
            double dy = (double) y[i]-(double) y[0];
            double dz = (double) z[i]-(double) z[0];

            dist.put(f[i],Math.sqrt(dx*dx + dy*dy + dz*dz));
        }

        return dist;

    }
}
