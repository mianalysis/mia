package io.github.mianalysis.mia.process.analysis;

import java.util.TreeMap;

public class TotalPathLengthCalculator implements SummaryCalculator {

    @Override
    public double calculate(double[] x, double[] y, double[] z, int[] f) {
        TreeMap<Integer,Double> steps = new InstantaneousStepSizeCalculator().calculate(x, y, z, f);

        double totalPathLength = 0;
        for (double value:steps.values()) totalPathLength += value;

        return totalPathLength;
        
    }    
}
