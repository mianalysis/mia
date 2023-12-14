package io.github.mianalysis.mia.process.analysis;

import ij.ImagePlus;
import ij.ImageStack;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSet;
import io.github.mianalysis.mia.process.math.CumStat;

/**
 * Created by sc13967 on 12/05/2017.
 */
public class IntensityCalculator {

    // ANALYSIS OVER A SPECIFIED VOLUME

    public static CumStat calculate(ImageStack image, CoordinateSet coordinateSet) {
        // Initialising the pixel value store
        CumStat cs = new CumStat();

        calculate(image,cs,coordinateSet);

        return cs;

    }

    public static void calculate(ImageStack image, CumStat cs, CoordinateSet coordinateSet) {
        // Running through all pixels in the volume, adding them to the CumStat object
        for (Point<Integer> point:coordinateSet) {
            cs.addMeasure(image.getVoxel(point.getX(),point.getY(),point.getZ()));
        }
    }


    // ANALYSIS OVER PIXELS SPECIFIED BY BINARY MASK

    public static CumStat calculate(ImageStack image, ImageStack mask) {
        // Initialising the pixel value store
        CumStat cs = new CumStat();

        calculate(image,cs,mask);

        return cs;

    }

    public static void calculate(ImageStack image, CumStat cs, ImageStack mask) {
        // Running through all pixels in the volume, adding them to the CumStat object
        for (int z = 0; z < image.size(); z++) {
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    if (mask.getVoxel(x,y,z) == 0) {
                        cs.addMeasure(image.getVoxel(x,y,z));
                    }
                }
            }
        }
    }


    // ANALYSIS OVER THE ENTIRE IMAGE

    public static CumStat calculate(ImageStack image) {
        // Initialising the pixel value store
        CumStat cs = new CumStat();

        calculate(image,cs);

        return cs;

    }

    public static void calculate(ImageStack image, CumStat cs) {
        // Running through all pixels in the image, adding them to the CumStat object
        for (int z = 0; z < image.size(); z++) {
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    cs.addMeasure(image.getVoxel(x,y,z));
                }
            }
        }
    }

    @Deprecated
    public static CumStat calculate(ImagePlus image) {
        // Initialising the pixel value store
        CumStat cs = new CumStat();

        calculate(image,cs);

        return cs;

    }

    @Deprecated
    public static void calculate(ImagePlus image, CumStat cs) {
        // Running through all pixels in the image, adding them to the CumStat object
        for (int t = 0; t < image.getNFrames(); t++) {
            for (int z = 0; z < image.getNSlices(); z++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    for (int x = 0; x < image.getWidth(); x++) {
                        image.setPosition(1, z + 1, t + 1);
                        cs.addMeasure(image.getProcessor().getPixelValue(x, y));
                    }
                }
            }
        }
    }
}
