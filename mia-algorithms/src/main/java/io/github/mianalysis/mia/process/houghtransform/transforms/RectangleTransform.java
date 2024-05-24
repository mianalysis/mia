package io.github.mianalysis.mia.process.houghtransform.transforms;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ij.process.ImageProcessor;
import io.github.mianalysis.mia.process.houghtransform.accumulators.RectangleAccumulator;
import io.github.mianalysis.mia.process.string.CommaSeparatedStringInterpreter;
import io.github.mianalysis.mia.process.voxel.BresenhamRectangle;

/**
 * Created by sc13967 on 12/01/2018.
 */
public class RectangleTransform extends AbstractTransform {
    public RectangleTransform(ImageProcessor ipr, String[] parameterRanges) {
        super(ipr);

        String xRange = CommaSeparatedStringInterpreter.removeInterval(parameterRanges[0]);
        String yRange = CommaSeparatedStringInterpreter.removeInterval(parameterRanges[1]);

        int[][] parameters = new int[parameterRanges.length][];
        parameters[0] = CommaSeparatedStringInterpreter.interpretIntegers(xRange, true, ipr.getWidth()-2);
        parameters[1] = CommaSeparatedStringInterpreter.interpretIntegers(yRange, true, ipr.getHeight()-2);
        
        parameters[2] = CommaSeparatedStringInterpreter.interpretIntegers(parameterRanges[2], true, 0);
        parameters[3] = CommaSeparatedStringInterpreter.interpretIntegers(parameterRanges[3], true, 0);
        parameters[4] = CommaSeparatedStringInterpreter.interpretIntegers(parameterRanges[4], true, 0);        

        this.accumulator = new RectangleAccumulator(parameters);

    }

    @Override
    public void run() {
        int[][] parameters = accumulator.getParameters();
        
        // Setting up the threading system
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        // Iterating over all widths, heights and orientations
        int nX = parameters[0].length;
        int nY = parameters[1].length;
        int nW = parameters[2].length;
        int nL = parameters[3].length;
        int nT = parameters[4].length;
        
        for (int iW = 0; iW < nW; iW++) {
            for (int iL = 0; iL < nL; iL++) {
                for (int iT = 0; iT < nT; iT++) {
                    int finalIW = iW;
                    int finalIL = iL;
                    int finalIT = iT;

                    int W = parameters[2][iW];
                    int L = parameters[3][iL];
                    int T = parameters[4][iT];

                    Runnable task = () -> {
                        // Generating coordinates for the points on the rectangle
                        int[][] rect = BresenhamRectangle.getRectangle(W, L, T);

                        // Iterating over X and Y
                        for (int iX = 0; iX < nX; iX++) {
                            for (int iY = 0; iY < nY; iY++) {
                                // Getting current XY values
                                int X = parameters[0][iX];
                                int Y = parameters[1][iY];

                                double value = pixels.getPixelValue(new int[] { X, Y });
                                accumulator.addPoints(new int[] { iX, iY, finalIW, finalIL, finalIT }, value, rect);

                            }
                        }
                    };
                    pool.submit(task);
                }
            }
        }

        pool.shutdown();
        try {
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
