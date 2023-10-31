package io.github.mianalysis.mia.process.analysis;

import io.github.mianalysis.mia.object.coordinates.tracks.Track;

public interface SummaryCalculator {
    public default double calculate(Track track) {
        return calculate(track.getX(),track.getY(),track.getZ(),track.getF());
    }

    public double calculate(double[] x, double[] y, double[] z, int[] f);
    
}
