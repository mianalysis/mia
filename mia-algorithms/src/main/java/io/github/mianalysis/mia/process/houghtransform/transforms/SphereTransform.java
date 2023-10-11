package io.github.mianalysis.mia.process.houghtransform.transforms;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ij.ImageStack;
import io.github.mianalysis.mia.process.houghtransform.accumulators.SphereAccumulator;
import io.github.mianalysis.mia.process.string.CommaSeparatedStringInterpreter;
import io.github.mianalysis.mia.process.voxel.SphereShell;
import io.github.mianalysis.mia.process.voxel.SphereShell.Connectivity;

/**
 * Created by sc13967 on 12/01/2018.
 */
public class SphereTransform extends AbstractTransform {

    // public static void main(String[] args) {
    //     new ImageJ();
    //     ImagePlus ipl = IJ.openImage("C:/Users/steph/Desktop/TEST_HoughSphere.tif");
    //     ImageStack ist = ipl.getStack();
    //     String[] paramRanges = new String[] { "0-end", "0-end", "0-end", "15-25-1" };
    //     SphereTransform sht = new SphereTransform(ist, paramRanges);
    //     sht.run();
    //     sht.getAccumulatorAsImage().show();
    //     sht.addDetectedObjectsOverlay(ipl, sht.getObjects(10, 100));
    //     ipl.show();
    // }

    public SphereTransform(ImageStack ist, String[] parameterRanges) {
        super(ist);

        String xRange = CommaSeparatedStringInterpreter.removeInterval(parameterRanges[0]);
        String yRange = CommaSeparatedStringInterpreter.removeInterval(parameterRanges[1]);
        String zRange = CommaSeparatedStringInterpreter.removeInterval(parameterRanges[2]);

        int[][] parameters = new int[parameterRanges.length][];
        parameters[0] = CommaSeparatedStringInterpreter.interpretIntegers(xRange, true, ist.getWidth()-1);
        parameters[1] = CommaSeparatedStringInterpreter.interpretIntegers(yRange, true, ist.getHeight()-1);
        parameters[2] = CommaSeparatedStringInterpreter.interpretIntegers(zRange, true, ist.size()-1);
        parameters[3] = CommaSeparatedStringInterpreter.interpretIntegers(parameterRanges[3], true, ist.getWidth() - 1);

        this.accumulator = new SphereAccumulator(parameters);

    }

    @Override
    public void run() {
        int[][] parameters = accumulator.getParameters();

        // Setting up the threading system
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        // Iterating over all radii
        int nX = parameters[0].length;
        int nY = parameters[1].length;
        int nZ = parameters[2].length;
        int nR = parameters[3].length;

        for (int iR = 0; iR < nR; iR++) {
            int finalIR = iR;
            int R = parameters[3][iR];
            Runnable task = () -> {
                // Generating coordinates for the points on the midpoint circle
                SphereShell sphereShell = new SphereShell(R,Connectivity.SIX);
                int[][] sph = sphereShell.getSphere();

                // Iterating over X and Y
                for (int iX = 0; iX < nX; iX++) {                    
                    for (int iY = 0; iY < nY; iY++) {
                        for (int iZ = 0; iZ < nZ; iZ++) {    
                            // Getting current XYZ values
                            int X = parameters[0][iX];
                            int Y = parameters[1][iY];
                            int Z = parameters[2][iZ];

                            double value = pixels.getPixelValue(new int[] { X, Y, Z });
                            accumulator.addPoints(new int[] { iX, iY, iZ, finalIR }, value, sph);

                        }
                    }
                }
            };
            pool.submit(task);
        }

        pool.shutdown();
        try {
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
