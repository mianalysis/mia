package io.github.mianalysis.mia.process.houghtransform.accumulators;

import java.util.ArrayList;

import ij.ImagePlus;
import io.github.mianalysis.mia.process.math.Indexer;

/**
 * Created by sc13967 on 12/01/2018.
 */
public abstract class AbstractAccumulator {
    protected Indexer indexer;
    protected double[] accumulator;
    protected int[] counts;
    protected int[][] parameters;


    // CONSTRUCTOR

    public AbstractAccumulator(int[][] parameters) {
        // Keeping track of the parameter ranges.  These will be needed to access the correct index
        this.parameters = parameters;

        // Calculating the number of elements in each dimension
        int[] dims = new int[parameters.length];
        for (int i=0;i<parameters.length;i++)
            dims[i] = parameters[i].length;
        
        // Initialising the indexer
        indexer = new Indexer(dims);

        // Creating the accumulator to occupy the whole space
        int size = indexer.getLength();
        accumulator = new double[size];
        counts = new int[size];

    }

    public void normaliseScores() {
        for (int i=0;i<counts.length;i++) {
            accumulator[i] = accumulator[i]/counts[i];
        }
    }

    /**
     * Returns the Indexer index for the pixel in the Accumulator with the largest score.
     * @return
     */
    public int getLargestScorePixelIndex() {
        double maxVal = Double.MIN_VALUE;
        int maxIdx = -1;

        // Iterating over all pixels in the Accumulator.  Identifying the brightest.
        for (int i=0;i<accumulator.length;i++) {
            if (accumulator[i] > maxVal) {
                maxVal = accumulator[i];
                maxIdx = i;
            }
        }

        return maxIdx;

    }

    public int[][] getParameters() {
        return parameters;
    }


    // ABSTRACT METHODS

    public abstract void addDetectedObjectsOverlay(ImagePlus ipl, ArrayList<double[]> objects);

    public abstract void addPoints(int[] parameters, double value, int[][] points);

    public abstract ArrayList<double[]> getObjects(double minScore, int exclusionR);

    public abstract ArrayList<double[]> getNObjects(int nObjects, int exclusionR);

    public abstract ImagePlus getAccumulatorAsImage();

}
