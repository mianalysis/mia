package io.github.mianalysis.mia.process.houghtransform.accumulators;

import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Overlay;
import io.github.mianalysis.mia.process.voxel.MidpointCircle;

public class LineAccumulator extends AbstractAccumulator {

    public LineAccumulator(int[][] parameters) {
        super(parameters);
    }

    @Override
    public void addDetectedObjectsOverlay(ImagePlus ipl, ArrayList<double[]> objects) {
        // Adding overlay showing detected circles
        Overlay overlay = ipl.getOverlay();
        if (overlay == null)
            overlay = new Overlay();

        for (int i = 0; i < objects.size(); i++) {
            double R = objects.get(i)[0];
            double thetaD = objects.get(i)[1];
            double thetaR = Math.toRadians(thetaD);
            int y0 = (int) Math.round(R / (Math.sin(thetaR)));
            int y1 = (int) Math.round((R - ipl.getWidth() * Math.cos(thetaR)) / (Math.sin(thetaR)));

            Line roi = new Line(0, y0, ipl.getWidth(), y1);

            overlay.add(roi);

        }

        ipl.setOverlay(overlay);

    }

    @Override
    public void addPoints(int[] indices, double value, int[][] points) {
        for (int i = 0; i < points.length; i++) {
            int iR = parameters[0][indices[0]] + points[i][0] - parameters[0][0];
            int iThetaD = parameters[1][indices[1]] + points[i][1] - parameters[1][0];

            int idx = indexer.getIndex(new int[] { iR, iThetaD });

            if (idx == -1)
                continue;

            // Adding the current value and incrementing the count
            accumulator[idx] = accumulator[idx] + value;
            counts[idx]++;

        }
    }

    @Override
    public ArrayList<double[]> getObjects(double minScore, int exclusionR) {
        // Creating an ArrayList to store the points
        ArrayList<double[]> objects = new ArrayList<>();

        // Getting relative coordinates for exclusion zone
        MidpointCircle midpointCircle = new MidpointCircle(exclusionR);
        int[] x = midpointCircle.getXCircleFill();
        int[] y = midpointCircle.getYCircleFill();

        // Identifying the brightest point in the accumulator
        int maxIdx = getLargestScorePixelIndex();
        if (maxIdx == -1)
            return objects;

        double maxVal = accumulator[maxIdx];

        // Extracting all points
        while (maxVal >= minScore) {
            addBrightestObject(maxIdx, maxVal, objects, x, y);

            // Updating the brightest point
            maxIdx = getLargestScorePixelIndex();
            if (maxIdx == -1)
                break;

            maxVal = accumulator[maxIdx];

        }

        return objects;

    }

    @Override
    public ArrayList<double[]> getNObjects(int nObjects, int exclusionR) {
        // Creating an ArrayList to store the points
        ArrayList<double[]> objects = new ArrayList<>();

        // Getting relative coordinates for exclusion zone
        MidpointCircle midpointCircle = new MidpointCircle(exclusionR);
        int[] x = midpointCircle.getXCircleFill();
        int[] y = midpointCircle.getYCircleFill();

        // Extracting all points
        for (int count = 0; count < nObjects; count++) {
            // Identifying the brightest point in the accumulator
            int maxIdx = getLargestScorePixelIndex();
            if (maxIdx == -1)
                return objects;

            double maxVal = accumulator[maxIdx];

            addBrightestObject(maxIdx, maxVal, objects, x, y);

        }

        return objects;

    }

    void addBrightestObject(int maxIdx, double maxVal, ArrayList<double[]> objects, int[] x, int[] y) {
        // Getting parameters for brightest current spot and adding to ArrayList
        int[] currIndices = indexer.getCoord(maxIdx);
        int RR = parameters[0][currIndices[0]];
        int thetaTheta = parameters[1][currIndices[1]];

        objects.add(new double[] { RR, thetaTheta, maxVal });

        // Setting all pixels within exclusion radius to zero. This is repeated for all
        // slices.
        for (int i = 0; i < x.length; i++) {
            int iR = RR + x[i] - parameters[0][0];
            int iTheta = thetaTheta + y[i] - parameters[1][0];
            int idx = indexer.getIndex(new int[] { iR, iTheta });

            if (idx == -1)
                continue;

            accumulator[idx] = 0;

        }
    }

    @Override
    public ImagePlus getAccumulatorAsImage() {
        int[] dim = indexer.getDim();
        ImagePlus ipl = IJ.createHyperStack("Line_Accumulator", dim[0], dim[1], 1, 1, 1, 32);

        for (int idx = 0; idx < indexer.getLength(); idx++) {
            int[] coord = indexer.getCoord(idx);

            double value = accumulator[idx];

            ipl.getProcessor().putPixelValue(coord[0], coord[1], value);

        }

        return ipl;

    }

    /**
     * Returns the Indexer index for the pixel in the Accumulator with the largest
     * score.
     * 
     * @return
     */
    public int getLargestScorePixelIndex() {
        double maxVal = Double.MIN_VALUE;
        int maxIdx = -1;

        // Iterating over all pixels in the Accumulator. Identifying the brightest.
        for (int i = 0; i < accumulator.length; i++) {
            if (accumulator[i] > maxVal) {
                maxVal = accumulator[i];
                maxIdx = i;
            }
        }

        return maxIdx;

    }

}
