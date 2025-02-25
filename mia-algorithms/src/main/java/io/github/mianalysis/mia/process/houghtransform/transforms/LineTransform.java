package io.github.mianalysis.mia.process.houghtransform.transforms;

import ij.gui.Plot;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.process.houghtransform.accumulators.LineAccumulator;
import io.github.mianalysis.mia.process.string.CommaSeparatedStringInterpreter;
import io.github.mianalysis.mia.process.voxel.BresenhamLine;

/**
 * Created by sc13967 on 05/02/2025.
 */
public class LineTransform extends AbstractTransform {
    protected int width = 0;
    protected int height = 0;

    public LineTransform(ImageProcessor ipr, String[] parameterRanges) {
        super(ipr);

        String rRange = CommaSeparatedStringInterpreter.removeInterval(parameterRanges[0]);
        String thetaRange = CommaSeparatedStringInterpreter.removeInterval(parameterRanges[1]);

        int[][] parameters = new int[parameterRanges.length][];
        
        parameters[0] = CommaSeparatedStringInterpreter.interpretIntegers(rRange, true,
                (int) Math.ceil(Math.sqrt(ipr.getWidth() * ipr.getWidth() + ipr.getHeight() * ipr.getHeight())));
        parameters[1] = CommaSeparatedStringInterpreter.interpretIntegers(thetaRange, true, 179);

        this.accumulator = new LineAccumulator(parameters);
        this.width = ipr.getWidth();
        this.height = ipr.getHeight();

    }

    @Override
    public void run() {
        int[][] parameters = accumulator.getParameters();

        // Setting up the threading system
        // ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L,
        // TimeUnit.MILLISECONDS,
        // new LinkedBlockingQueue<>());

        // Iterating over all widths, heights and orientations
        int nR = parameters[0].length;
        int nTheta = parameters[1].length;

        // Iterating over R and theta
        for (int iR = 0; iR < nR; iR++) {
            // int finalIR = iR;
            // Runnable task = () -> {
            for (int iTheta = 0; iTheta < nTheta; iTheta++) {
                // Getting current XY values
                int R = parameters[0][iR];
                int thetaD = parameters[1][iTheta];

                // 180 degs causes the system to crash
                if (thetaD == 180)
                    continue;

                double thetaR = Math.toRadians(thetaD);

                // Generating coordinates for the points on the line
                int y0 = (int) Math.round(R / (Math.sin(thetaR)));
                int y1 = (int) Math.round((R - (width-1) * Math.cos(thetaR)) / (Math.sin(thetaR)));
                int[][] line = BresenhamLine.getLine(0, width-1, y0, y1);

                // double[] xx = new double[line.length];
                // double[] yy = new double[line.length];                
                
                // int i=0;
                for (int[] point : line) {     
                    // xx[i] = point[0];
                    //     yy[i] = point[1];                                   
                    if (point[1] >= 0 && point[1] < height) {
                        // xx[i] = point[0];
                        // yy[i] = point[1];
                        double value = pixels.getPixelValue(point);
                        accumulator.addPoints(new int[] { iR, iTheta }, value, new int[][]{new int[]{0,0}});
                    }
                    // i++;
                }
                // Plot pl = new Plot("T", "", "");
                // pl.setLimits(0, 512, 0, 512);
                // pl.add("Line", xx, yy);
                // pl.show();
            }
        }

        // pool.shutdown();
        // try {
        // pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never
        // terminate early
        // } catch (InterruptedException e) {
        // e.printStackTrace();
        // }
    }
}
