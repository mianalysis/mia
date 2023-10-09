package io.github.mianalysis.mia.process.analysis;

import java.util.TreeMap;

/**
 * Created by Stephen on 15/04/2017.
 */
public class CumulativePathLengthCalculator implements SpatialCalculator {
    public TreeMap<Integer,Double> calculate(double[] x, double[] y, double[] z, int[] f) {
        TreeMap<Integer,Double> steps = new InstantaneousStepSizeCalculator().calculate(x,y,z,f);
        TreeMap<Integer,Double> dist = new TreeMap<>();

        dist.put(f[0],0d);
        for (int i=1;i<x.length;i++) {
            double previousValue = dist.get(f[i-1])+steps.get(f[i]);
            dist.put(f[i],previousValue);
        }

        return dist;

    }
}
