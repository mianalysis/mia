package io.github.mianalysis.mia.process.analysis;

import java.util.TreeMap;

/**
 * Created by sc13967 on 18/04/2018.
 */
public class InstantaneousVelocityCalculator {
    public TreeMap<Integer,Double> calculate(int[] f, double[] x) {
        TreeMap<Integer,Double> velocity = new TreeMap<>();

        velocity.put(f[0],0d);
        for (int i = 1; i < x.length; i++) {
            double currentVelocity = (x[i] - x[i - 1])/(f[i] - f[i - 1]);
            velocity.put(f[i],currentVelocity);

        }

        return velocity;

    }
}
