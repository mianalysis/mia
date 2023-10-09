package io.github.mianalysis.mia.process.houghtransform.accumulators;

import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.RotatedRectRoi;
import io.github.mianalysis.mia.object.coordinates.voxel.MidpointCircle;

public class RectangleAccumulator extends AbstractAccumulator {
    /**
     * Constructor for Accumulator object.
     *
     * @param parameters 2D Integer array containing the dimensions over which the
     *                   accumulator exists.
     */
    public RectangleAccumulator(int[][] parameters) {
        super(parameters);
    }

    @Override
    public void addDetectedObjectsOverlay(ImagePlus ipl, ArrayList<double[]> objects) {
        // Adding overlay showing detected circles
        Overlay overlay = ipl.getOverlay();
        if (overlay == null)
            overlay = new Overlay();

        for (int i = 0; i < objects.size(); i++) {
            double x = objects.get(i)[0];
            double y = objects.get(i)[1];
            double width = objects.get(i)[2];
            double length = objects.get(i)[3];
            double theta = objects.get(i)[4];

            double thetaRads = -Math.toRadians((double) theta);
            int x1 = (int) Math.round((-length / 2) * Math.cos(thetaRads));
            int y1 = (int) Math.round(-(-length / 2) * Math.sin(thetaRads));

            RotatedRectRoi roi = new RotatedRectRoi(x + x1, y + y1, x - x1, y - y1, width);

            overlay.add(roi);

        }

        ipl.setOverlay(overlay);

    }

    @Override
    public void addPoints(int[] indices, double value, int[][] points) {
        for (int i = 0; i < points.length; i++) {
            int iX = parameters[0][indices[0]] + points[i][0] - parameters[0][0];
            int iY = parameters[1][indices[1]] + points[i][1] - parameters[1][0];

            int idx = indexer.getIndex(new int[] { iX, iY, indices[2], indices[3], indices[4] });

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
        int xx = parameters[0][currIndices[0]];
        int yy = parameters[1][currIndices[1]];
        int ww = parameters[2][currIndices[2]];
        int ll = parameters[3][currIndices[3]];
        int tt = parameters[4][currIndices[4]];

        objects.add(new double[] { xx, yy, ww, ll, tt, maxVal });

        // Setting all pixels within exclusionR to zero. This is repeated for all
        // slices.
        for (int iW = 0; iW < indexer.getDim()[2]; iW++) {
            for (int iL = 0; iL < indexer.getDim()[3]; iL++) {
                for (int iT = 0; iT < indexer.getDim()[4]; iT++) {
                    for (int i = 0; i < x.length; i++) {
                        int iX = xx + x[i] - parameters[0][0];
                        int iY = yy + y[i] - parameters[1][0];
                        int idx = indexer.getIndex(new int[] { iX, iY, iW, iL, iT });

                        if (idx == -1)
                            continue;

                        accumulator[idx] = 0;
                    }
                }
            }
        }
    }

    @Override
    public ImagePlus getAccumulatorAsImage() {
        int[] dim = indexer.getDim();
        ImagePlus ipl = IJ.createHyperStack("Rectangle_Accumulator", dim[0], dim[1], dim[2], dim[3], dim[4], 32);

        for (int idx = 0; idx < indexer.getLength(); idx++) {
            int[] coord = indexer.getCoord(idx);

            double value = accumulator[idx];

            ipl.setPosition(coord[2] + 1, coord[3] + 1, coord[4] + 1);
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
