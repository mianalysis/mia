package io.github.mianalysis.mia.object.coordinates.tracks;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.process.analysis.DirectionalPersistenceCalculator;
import io.github.mianalysis.mia.process.analysis.MSDCalculator;
import io.github.mianalysis.mia.process.math.CumStat;

/**
 * Created by sc13967 on 13/06/2017.
 */
public class TrackCollection extends LinkedHashMap<Integer,Track> {

    // PUBLIC METHODS

    private static final long serialVersionUID = -6015686973069140664L;

    public double[][] getAverageRollingEuclideanDistance(boolean relativeToTrackStart) {
        // Determining the first and last frames
        int firstFrame = Integer.MAX_VALUE;
        int lastFrame = 0;
        for (Track track:values()) {
            int[] f = track.getF();
            if (f[0] < firstFrame) {
                firstFrame = f[0];
            }
            if (f[f.length-1] > lastFrame) {
                lastFrame = f[f.length-1];
            }
        }
        int longestDuration = lastFrame-firstFrame;

        // Creating the CumStat array
        CumStat[] cs = new CumStat[longestDuration+1];
        for (int i=0;i<cs.length;i++) {
            cs[i] = new CumStat();
        }

        for (Track track:values()) {
            int[] f = track.getF();
            TreeMap<Integer,Double> rollingEuclideanDistance = track.getRollingEuclideanDistance();

            for (int i=0;i<rollingEuclideanDistance.size();i++) {
                int pos = relativeToTrackStart ? f[i]-f[0] : f[i]-firstFrame;
                cs[pos].addMeasure(rollingEuclideanDistance.get(f[i]));
            }
        }

        // Getting the frame numbers
        double[] f = new double[longestDuration];
        for (int i=0;i<f.length;i++) {
            f[i] = i;
        }

        // Getting the average and standard deviations
        double[] averageEuclideanDistance = Arrays.stream(cs).mapToDouble(CumStat::getMean).toArray();
        double[] stdevEuclideanDistance = Arrays.stream(cs).mapToDouble(CumStat::getStd).toArray();

        return new double[][]{f,averageEuclideanDistance,stdevEuclideanDistance};

    }

    public double[][] getAverageTotalPathLength(boolean relativeToTrackStart) {
        // Determining the first and last frames
        int firstFrame = Integer.MAX_VALUE;
        int lastFrame = 0;
        for (Track track:values()) {
            int[] f = track.getF();
            if (f[0] < firstFrame) {
                firstFrame = f[0];
            }
            if (f[f.length-1] > lastFrame) {
                lastFrame = f[f.length-1];
            }
        }
        int longestDuration = lastFrame-firstFrame;

        // Creating the CumStat array
        CumStat[] cs = new CumStat[longestDuration+1];
        for (int i=0;i<cs.length;i++) {
            cs[i] = new CumStat();
        }

        for (Track track:values()) {
            int[] f = track.getF();
            TreeMap<Integer,Double> rollingTotalPathLength = track.getRollingTotalPathLength();

            for (int i=0;i<rollingTotalPathLength.size();i++) {
                int pos = relativeToTrackStart ? f[i]-f[0] : f[i]-firstFrame;
                cs[pos].addMeasure(rollingTotalPathLength.get(f[i]));
            }
        }

        // Getting the frame numbers
        double[] f = new double[longestDuration];
        for (int i=0;i<f.length;i++) {
            f[i] = i;
        }

        // Getting the average and standard deviations
        double[] averageTotalPathLength = Arrays.stream(cs).mapToDouble(CumStat::getMean).toArray();
        double[] stdevTotalPathLength = Arrays.stream(cs).mapToDouble(CumStat::getStd).toArray();

        return new double[][]{f,averageTotalPathLength,stdevTotalPathLength};

    }

    public double[][] getAverageDirectionalityRatio(boolean relativeToTrackStart) {
        // Determining the first and last frames
        int firstFrame = Integer.MAX_VALUE;
        int lastFrame = 0;
        for (Track track:values()) {
            int[] f = track.getF();
            if (f[0] < firstFrame) {
                firstFrame = f[0];
            }
            if (f[f.length-1] > lastFrame) {
                lastFrame = f[f.length-1];
            }
        }
        int longestDuration = lastFrame-firstFrame;

        // Creating the CumStat array
        CumStat[] cs = new CumStat[longestDuration+1];
        for (int i=0;i<cs.length;i++) {
            cs[i] = new CumStat();
        }

        for (Track track:values()) {
            int[] f = track.getF();
            TreeMap<Integer,Double> rollingDirectionalityRatio = track.getRollingDirectionalityRatio();
            for (int i=0;i<rollingDirectionalityRatio.size();i++) {
                int pos = relativeToTrackStart ? f[i]-f[0] : f[i]-firstFrame;
                cs[pos].addMeasure(rollingDirectionalityRatio.get(f[i]));
            }
        }

        // Getting the frame numbers
        double[] f = new double[longestDuration];
        for (int i=0;i<f.length;i++) {
            f[i] = i;
        }

        // Getting the average and standard deviations
        double[] averageDirectionalityRatio = Arrays.stream(cs).mapToDouble(CumStat::getMean).toArray();
        double[] stdDirectionalityRatio = Arrays.stream(cs).mapToDouble(CumStat::getStd).toArray();

        return new double[][]{f,averageDirectionalityRatio,stdDirectionalityRatio};

    }

    /**
     * Average directional persistence.  Values are stored as the average for each frame gap.
     * @return Average directional persistence
     */
    public double[][] getAverageDirectionalPersistence() {
        // Determining the longest duration.  This is also the largest possible frame gap.
        int longestDuration = 0;
        for (Track track:values()) {
            if (track.getDuration() > longestDuration) {
                longestDuration = track.getDuration();
            }
        }

        // Creating the CumStat array
        CumStat[] cs = new CumStat[longestDuration+1];
        for (int i=0;i<cs.length;i++) {
            cs[i] = new CumStat();
        }

        for (Track track:values()) {
            DirectionalPersistenceCalculator.calculate(cs,track.getF(),track.getX(),track.getY(),track.getZ());
        }

        // Getting the frame intervals
        double[] df = new double[longestDuration];
        for (int i=0;i<df.length;i++) {
            df[i] = i;
        }

        // Getting the average and standard deviations
        double[] averageDirectionalPersistence = Arrays.stream(cs).mapToDouble(CumStat::getMean).toArray();
        double[] stdevDirectionalPersistence = Arrays.stream(cs).mapToDouble(CumStat::getStd).toArray();

        return new double[][]{df,averageDirectionalPersistence,stdevDirectionalPersistence};

    }

    /**
     * Average MSD.  Values are stored as the average for each frame gap.
     * @return Average MSD
     */
    public TreeMap<Integer,CumStat> getAverageMSD() {
        // Determining the longest duration.  This is also the largest possible frame gap.
        int longestDuration = 0;
        for (Track track:values()) {
            if (track.getDuration() > longestDuration) {
                longestDuration = track.getDuration();
            }
        }

        // Creating the CumStat array
        TreeMap<Integer,CumStat> msd = new TreeMap<>();
        for (Track track:values()) {
            MSDCalculator.calculate(msd,track.getF(),track.getX(),track.getY(),track.getZ());
        }

        return msd;

    }

    public double[][] getAverageNearestNeighbourDistance() {
        // Determining the first and last frames
        int firstFrame = Integer.MAX_VALUE;
        int lastFrame = 0;
        for (Track track:values()) {
            int[] f = track.getF();
            if (f[0] < firstFrame) {
                firstFrame = f[0];
            }
            if (f[f.length-1] > lastFrame) {
                lastFrame = f[f.length-1];
            }
        }
        int longestDuration = lastFrame-firstFrame;

        // Creating the CumStat array
        CumStat[] cs = new CumStat[longestDuration+1];
        for (int i=0;i<cs.length;i++) {
            cs[i] = new CumStat();
        }

        for (Track track:values()) {
            int[] f = track.getF();
            TreeMap<Integer,double[]> nearestNeighbourDistance = track.getNearestNeighbourDistance(this);

            for (int i=0;i<nearestNeighbourDistance.size();i++) {
                int pos = f[i]-firstFrame;
                cs[pos].addMeasure(nearestNeighbourDistance.get(f[i])[1]);
            }
        }

        // Getting the frame numbers
        double[] f = new double[longestDuration];
        for (int i=0;i<f.length;i++) {
            f[i] = i;
        }

        // Getting the average and standard deviations
        double[] averageNearestNeighbourDistance = Arrays.stream(cs).mapToDouble(CumStat::getMean).toArray();
        double[] stdevNearestNeighbourDistance = Arrays.stream(cs).mapToDouble(CumStat::getStd).toArray();

        return new double[][]{f,averageNearestNeighbourDistance,stdevNearestNeighbourDistance};

    }

    public int[][] getNumberOfObjects(boolean relativeToTrackStart) {
        // Determining the first and last frames
        int firstFrame = Integer.MAX_VALUE;
        int lastFrame = 0;
        for (Track track:values()) {
            int[] f = track.getF();
            if (f[0] < firstFrame) {
                firstFrame = f[0];
            }
            if (f[f.length-1] > lastFrame) {
                lastFrame = f[f.length-1];
            }
        }

        // Counting the number of objects present in each frame
        int[] n = new int[lastFrame-firstFrame+1];
        for (Track track:values()) {
            int[] f = track.getF();
            for (int ff:f) {
                int pos = relativeToTrackStart ? ff-f[0] : ff-firstFrame;
                n[pos] = n[pos] + 1;
            }
        }

        // Creating an array of frame numbers
        int[] f = new int[lastFrame-firstFrame+1];
        for (int i=0;i<f.length;i++) {
            f[i] = i+firstFrame;
        }

        return new int[][]{f,n};

    }

    /**
     * Returns the largest frame number of any track
     * @return The largest frame number of any track
     */
    public int getHighestFrame() {
        int maxFr = 0;
        for (Track track:values()) {
            for (int fr:track.keySet()) {
                maxFr = Math.max(maxFr,fr);
            }
        }

        return maxFr;

    }

    public double[][] getSpatialLimits(boolean pixelDistances) {
        double[][] limits = new double[][]{{Double.MAX_VALUE,Double.MIN_VALUE},{Double.MAX_VALUE,Double.MIN_VALUE},{Double.MAX_VALUE,Double.MIN_VALUE}};

        for (Track track:values()) {
            double[] x = track.getX();
            double[] y = track.getY();
            double[] z = track.getZ();

            for (int i=0;i<x.length;i++) {
                limits[0][0] = Math.min(limits[0][0],x[i]);
                limits[0][1] = Math.max(limits[0][1],x[i]);
                limits[1][0] = Math.min(limits[1][0],y[i]);
                limits[1][1] = Math.max(limits[1][1],y[i]);
                limits[2][0] = Math.min(limits[2][0],z[i]);
                limits[2][1] = Math.max(limits[2][1],z[i]);

            }
        }

        return limits;

    }

    public Point getMeanPoint(int frame) {
        CumStat[] cs = new CumStat[3];

        for (int i=0;i<3;i++) cs[i] = new CumStat();

        for (Track track:values()) {
            if (track.hasFrame(frame)) {
                cs[0].addMeasure(track.get(frame).getX());
                cs[1].addMeasure(track.get(frame).getY());
                cs[2].addMeasure(track.get(frame).getZ());

            }
        }

        return new Timepoint(cs[0].getMean(),cs[1].getMean(),cs[2].getMean(),frame);

    }

    public double getMaximumInstantaneousSpeed() {
        double maxVelocity = 0;

        for (Track track:values()) {
            TreeMap<Integer,Double> velocities = track.getInstantaneousSpeed();

            for (double velocity:velocities.values()) {
                maxVelocity = Math.max(maxVelocity,velocity);
            }
        }

        return maxVelocity;

    }

    public double getMaximumTotalPathLength() {
        double maxTotalPathLength = 0;

        for (Track track:values()) {
            TreeMap<Integer,Double> lengths = track.getRollingTotalPathLength();

            maxTotalPathLength = Math.max(maxTotalPathLength, lengths.lastKey().doubleValue());

        }

        return maxTotalPathLength;

    }
}
