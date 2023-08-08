package io.github.mianalysis.mia.process.analysis;

import java.util.TreeMap;

/**
 * Created by Stephen on 15/04/2017.
 */
public class InstantaneousStepSizeCalculator implements SpatialCalculator {
    public TreeMap<Integer,Double> calculate(double[] x, double[] y, double[] z, int[] f) {
        TreeMap<Integer,Double> steps = new TreeMap<>();

        steps.put(f[0],0d);
        for (int i = 1; i < x.length; i++) {
            double dx = x[i] - x[i - 1];
            double dy = y[i] - y[i - 1];
            double dz = z[i] - z[i - 1];

            double currentStep = Math.sqrt(dx * dx + dy * dy + dz * dz);
            steps.put(f[i],currentStep);
        }

        return steps;
    }
}
