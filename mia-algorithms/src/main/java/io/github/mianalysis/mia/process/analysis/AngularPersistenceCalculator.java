package io.github.mianalysis.mia.process.analysis;

import java.util.TreeMap;

public class AngularPersistenceCalculator implements SpatialCalculator {

    @Override
    public TreeMap<Integer, Double> calculate(double[] x, double[] y, double[] z, int[] f) {
        // Persistence values are between 0 and 1, where 0 corresponds to a track going back on itself and 1 is motion
        // in the same direction.
        TreeMap<Integer,Double> angularPersistence = new TreeMap<>();
        angularPersistence.put(f[0],Double.NaN);
        angularPersistence.put(f[f.length-1],Double.NaN);

        for (int i=1;i<(x.length-1);i++) {
            int ff = f[i];
            double x1 = (x[i]-x[i-1]);
            double y1 = (y[i]-y[i-1]);
            double z1 = (z[i]-z[i-1]);
            double x2 = (x[i+1]-x[i]);
            double y2 = (y[i+1]-y[i]);
            double z2 = (z[i+1]-z[i]);

            double crossX = y1 * z2 - z1 * y2;
            double crossY = z1 * x2 - x1 * z2;
            double crossZ = x1 * y2 - y1 * x2;
            double cross = Math.sqrt(crossX * crossX +
                    crossY * crossY + crossZ * crossZ);
            double dot = x1 * x2 + y1 * y2 + z1 + z2;

            double angle = Math.atan2(cross, dot);

            // Normalising the angle
            angle = 1-(angle/Math.PI);

            angularPersistence.put(ff,angle);

        }

        return angularPersistence;

    }
}
