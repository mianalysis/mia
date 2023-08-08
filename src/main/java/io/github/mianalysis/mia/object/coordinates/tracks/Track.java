package io.github.mianalysis.mia.object.coordinates.tracks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

import ij.ImagePlus;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.process.analysis.AngularPersistenceCalculator;
import io.github.mianalysis.mia.process.analysis.CumulativePathLengthCalculator;
import io.github.mianalysis.mia.process.analysis.DirectionalPersistenceCalculator;
import io.github.mianalysis.mia.process.analysis.DirectionalityRatioCalculator;
import io.github.mianalysis.mia.process.analysis.EuclideanDistanceCalculator;
import io.github.mianalysis.mia.process.analysis.InstantaneousSpeedCalculator;
import io.github.mianalysis.mia.process.analysis.InstantaneousStepSizeCalculator;
import io.github.mianalysis.mia.process.analysis.InstantaneousVelocityCalculator;
import io.github.mianalysis.mia.process.analysis.MSDCalculator;
import io.github.mianalysis.mia.process.analysis.NearestNeighbourCalculator;
import io.github.mianalysis.mia.process.analysis.SpotIntensity;
import io.github.mianalysis.mia.process.math.CumStat;

/**
 * Created by sc13967 on 03/02/2017.
 */
public class Track extends TreeMap<Integer,Timepoint<Double>> {
    private static final long serialVersionUID = -5310406836341487582L;
    private final String units;

    // CONSTRUCTORS
    public Track(String units) {
        this.units = units;
    }

    public Track(double[] x, double[] y, double[] z, int[] f, String units) {
        this.units = units;

        for (int i=0;i<x.length;i++) {
            put(f[i],new Timepoint<Double>(x[i],y[i],z[i],f[i]));
        }
    }

    public Track(ArrayList<Double> x, ArrayList<Double> y, ArrayList<Double> z, ArrayList<Integer> f, String units) {
        this.units = units;

        for (int i=0;i<x.size();i++) {
            put(f.get(i),new Timepoint<>(x.get(i),y.get(i),z.get(i),f.get(i)));

        }
    }


    // PUBLIC METHODS

    public void addTimepoint(double x, double y, double z, int f) {
        put(f,new Timepoint<Double>(x,y,z,f));
    }

    /**
     *
     * @return Mean position as double[][]{meanX,meanY,meanZ}{stdevX,stdevY,stdevZ}
     */
    public double[][] getMeanPosition() {
        CumStat csX = new CumStat(getX());
        CumStat csY = new CumStat(getY());
        CumStat csZ = new CumStat(getZ());

        return new double[][]{{csX.getMean(),csY.getMean(),csZ.getMean()},
                {csX.getStd(),csY.getStd(),csZ.getStd()}};

    }

    public CumStat[] getDirectionalPersistence() {
        return DirectionalPersistenceCalculator.calculate(getF(),getX(),getY(),getZ());

    }

    public TreeMap<Integer, CumStat> getMSD() {
        return MSDCalculator.calculate(getF(),getX(),getY(),getZ());

    }

    public double[] getMSDLinearFit(int nPoints) {
//        CumStat[] cs = MSDCalculator.calculate(getF(),getX(pixelDistances),getY(pixelDistances),getZ(pixelDistances));
//
//        double[] df = new double[cs.length];
//        for (int i=0;i<cs.length;i++) {
//            df[i] = i;
//        }
//        double[] MSD = Arrays.stream(cs).mapToDouble(CumStat::getMean).toArray();

        TreeMap<Integer,CumStat> msd = getMSD();
        int[] df = msd.keySet().stream().mapToInt(v->v).toArray();
        double[] MSD = msd.values().stream().mapToDouble(CumStat::getMean).toArray();

        return MSDCalculator.getLinearFit(df,MSD,nPoints);

    }

    public TreeMap<Integer, Double> getInstantaneousSpeed() {
        return new InstantaneousSpeedCalculator().calculate(this);

    }

    public TreeMap<Integer, Double> getInstantaneousXVelocity() {
        return new InstantaneousVelocityCalculator().calculate(getF(),getX());
    }

    public TreeMap<Integer, Double> getInstantaneousYVelocity() {
        return new InstantaneousVelocityCalculator().calculate(getF(),getY());
    }

    public TreeMap<Integer, Double> getInstantaneousZVelocity() {
        return new InstantaneousVelocityCalculator().calculate(getF(),getZ());
    }

    public TreeMap<Integer, Double> getInstantaneousStepSizes() {
        return new InstantaneousStepSizeCalculator().calculate(this);

    }

    public TreeMap<Integer, Double> getAngularPersistence() {
        return new AngularPersistenceCalculator().calculate(this);
    }

    public double getEuclideanDistance() {
        double[] x = getX();
        double[] y = getY();
        double[] z = getZ();

        double dx = x[x.length-1]-x[0];
        double dy = y[x.length-1]-y[0];
        double dz = z[x.length-1]-z[0];

        return Math.sqrt(dx * dx + dy * dy + dz * dz);

    }

    public double getTotalPathLength() {
        TreeMap<Integer,Double> steps = getInstantaneousStepSizes();

        double totalPathLength = 0;
        for (double value:steps.values()) totalPathLength += value;

        return totalPathLength;

    }

    public double getDirectionalityRatio() {
        return getEuclideanDistance()/getTotalPathLength();

    }

    /**
     * @return The Euclidean distance at all time steps
     */
    public TreeMap<Integer, Double> getRollingEuclideanDistance() {
        return new EuclideanDistanceCalculator().calculate(this);

    }

    /**
     * @return The total path length up to each time step
     */
    public TreeMap<Integer, Double> getRollingTotalPathLength() {
        return new CumulativePathLengthCalculator().calculate(this);

    }

    public TreeMap<Integer, Double> getRollingDirectionalityRatio() {
        return new DirectionalityRatioCalculator().calculate(this);

    }

    public TreeMap<Integer, double[]> getNearestNeighbourDistance(TrackCollection tracks) {
        return new NearestNeighbourCalculator().calculate(this,tracks);
    }

    public int getDuration() {
        int[] f = getF();

        return f[f.length-1]-f[0];

    }

    public double[][] getLimits(){
        double[] x = getX();
        double[] y = getY();
        double[] z = getZ();
        int[] f = getF();

        double[][] limits = new double[4][2];
        for (double[] row:limits) {
            row[0] = Double.MAX_VALUE;
            row[1] = Double.MIN_VALUE;

        }

        for (int i=0;i<x.length;i++) {
            if (x[i] < limits[0][0]) limits[0][0] = x[i];
            if (x[i] > limits[0][1]) limits[0][1] = x[i];
            if (y[i] < limits[1][0]) limits[1][0] = y[i];
            if (y[i] > limits[1][1]) limits[1][1] = y[i];
            if (z[i] < limits[2][0]) limits[2][0] = z[i];
            if (z[i] > limits[2][1]) limits[2][1] = z[i];
            if (f[i] < limits[3][0]) limits[3][0] = f[i];
            if (f[i] > limits[3][1]) limits[3][1] = f[i];

        }

        return limits;

    }

    public double[] getRollingIntensity(ImagePlus ipl, int radius) {
        double[] x = getX();
        double[] y = getY();
        double[] z = getZ();

        int[] f = getF();
        double[] intensity = new double[x.length];

        for (int i=0;i<x.length;i++) {
            ipl.setPosition(0,(int) Math.round(z[i])+1,f[i]+1);

            SpotIntensity spotIntensity = new SpotIntensity(ipl.getProcessor(),x[i],y[i],radius);

            intensity[i] = spotIntensity.getMeanPointIntensity();

        }

        return intensity;

    }

    public boolean hasFrame(int frame) {
        return containsKey(frame);

    }


    // GETTERS AND SETTERS

    public double[] getX() {
        return values().stream().mapToDouble(Point::getX).toArray();
    }

    public double[] getY() {
        return values().stream().mapToDouble(Point::getY).toArray();
    }

    public double[] getZ() {
        return values().stream().mapToDouble(Point::getZ).toArray();
    }

    public double getX(int f) {
        return get(f).getX();
    }

    public double getY(int f) {
        return get(f).getY();
    }

    public double getZ(int f) {
        return get(f).getZ();
    }

    public int[] getF() {
        return values().stream().mapToInt(Timepoint::getF).toArray();
    }

    public Point<Double> getPointAtFrame(int frame) {
        return get(frame);

    }

    public double[] getFAsDouble() {
        return values().stream().mapToDouble(Timepoint::getF).toArray();
    }

    public String getUnits() {
        return units;
    }


    @Override
    public int hashCode() {
        int hash = 1;

        hash = 31*hash + units.toUpperCase().hashCode();

        for (Timepoint point:values()) hash = 31*hash + point.hashCode();

        return hash;

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Track)) return false;

        Track track2 = (Track) obj;

        if (size() != track2.size()) return false;

        if (!units.toUpperCase().equals(track2.getUnits().toUpperCase())) return false;

        Iterator<Timepoint<Double>> iterator1 = values().iterator();
        Iterator<Timepoint<Double>> iterator2 = track2.values().iterator();

        while (iterator1.hasNext()) {
            Timepoint<Double> timepoint1 = iterator1.next();
            Timepoint<Double> timepoint2 = iterator2.next();

            if (!timepoint1.equals(timepoint2)) return false;

        }

        return true;

    }
}