package io.github.mianalysis.mia.process.analysis;

import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSet;
import io.github.mianalysis.mia.process.math.CumStat;

public class CentroidCalculator {
    public static Point<Double> calculateMeanCentroid(CoordinateSet coordinateSet) {
        CumStat csX = new CumStat();
        CumStat csY = new CumStat();
        CumStat csZ = new CumStat();

        for (Point<Integer> point : coordinateSet) {
            csX.addMeasure(point.x);
            csY.addMeasure(point.y);
            csZ.addMeasure(point.z);
        }

        return new Point<>(csX.getMean(), csY.getMean(), csZ.getMean());

    }
}
