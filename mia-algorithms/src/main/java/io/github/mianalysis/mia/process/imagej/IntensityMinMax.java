package io.github.mianalysis.mia.process.imagej;

import java.util.ArrayList;

import ij.ImagePlus;
import ij.process.ImageStatistics;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetI;

/**
 * Created by Stephen on 15/04/2017.
 */
public class IntensityMinMax {
    public static final int PROCESS_FAST = 0;
    public static final int PROCESS_PRECISE = 1;

    public static void run(ImagePlus ipl, boolean stack) {
        if (stack && ipl.getNDimensions() > 2) processStack(ipl,0,0,PROCESS_FAST);
        else processSingle(ipl,0,0,PROCESS_FAST);

        ipl.updateChannelAndDraw();

    }

    public static void run(ImagePlus ipl, boolean stack, double minClip, double maxClip, int mode) {
        if (stack && ipl.getNDimensions() > 2) {
            processStack(ipl,minClip,maxClip,mode);
        } else {
            processSingle(ipl,minClip,maxClip,mode);
        }

        ipl.updateChannelAndDraw();

    }

    static void processSingle(ImagePlus ipl, double minClip, double maxClip, int mode) {
        // ImageStatistics stats = ImageStatistics.getStatistics(ipl.getProcessor(), ImageStatistics.MIN_MAX, null);

        // Getting the minimum and maximum values
        double[] range = null;
        switch (mode) {
            case PROCESS_FAST:
                range = getWeightedChannelRangeFast(ipl,1,minClip,maxClip);
                break;

            case PROCESS_PRECISE:
            default:
                range = getWeightedChannelRangePrecise(ipl,1,minClip,maxClip);
                break;
        }

        double min = range[0];
        double max = range[1];

        ipl.setDisplayRange(min,max);
        ipl.updateChannelAndDraw();
        ipl.setPosition(1);

    }

    static void processStack(ImagePlus ipl, double minClip, double maxClip, int mode) {
        for (int channel = 0; channel < ipl.getNChannels(); channel++) {
            double[] range;
            if (minClip == 0 && maxClip == 0) {
                range = getAbsoluteRange(ipl,channel);
            } else {
                switch (mode) {
                    case PROCESS_FAST:
                        range = getWeightedChannelRangeFast(ipl,1,minClip,maxClip);
                        break;

                    case PROCESS_PRECISE:
                    default:
                        range = getWeightedChannelRangePrecise(ipl,1,minClip,maxClip);
                        break;
                }
            }

            for (int slice = 0; slice < ipl.getNSlices(); slice++) {
                for (int frame = 0; frame < ipl.getNFrames(); frame++) {
                    ipl.setPosition(channel + 1, slice + 1, frame + 1);
                    ipl.setDisplayRange(range[0],range[1]);
                }
            }

            ipl.updateChannelAndDraw();

        }
    }

    public static double[] getWeightedChannelRangeFast(ImagePlus ipl, int channel, double minClip, double maxClip) {
        return getWeightedChannelRangeFast(ipl,null,channel,0,minClip,maxClip);

    }

    public static double[] getWeightedChannelRangeFast(ImagePlus ipl, CoordinateSetI coordinateSet, int channel, int frame, double minClip, double maxClip) {
        double[] minMax = coordinateSet == null ? getAbsoluteRange(ipl,channel) : getAbsoluteRange(ipl,coordinateSet,channel,frame);
        double range = minMax[1]-minMax[0];

        double minI = minMax[0] + range*minClip;
        double maxI = minMax[1] - range*maxClip;

        return new double[]{minI,maxI};

    }

    public static double[] getWeightedChannelRangePrecise(ImagePlus ipl, int channel, double minClip, double maxClip) {
        return getWeightedChannelRangePrecise(ipl,null,channel,0,minClip,maxClip);

    }

    public static double[] getWeightedChannelRangePrecise(ImagePlus ipl, CoordinateSetI coordinateSet, int channel, int frame, double minClip, double maxClip) {
        ArrayList<Float> pixels = coordinateSet == null ? getPixels(ipl,channel) : getPixels(ipl,coordinateSet,channel,frame);
        pixels.sort(Float::compareTo);

        // Getting the min and max bins
        int sum = pixels.size();
        int minBin = (int) Math.round(sum * minClip);
        int maxBin = (int) Math.round(sum - sum * maxClip)-1;

        // If all the pixels are a single value this might go wrong.  If this is the case, returning the full range.
        if (pixels.size() <= minBin || pixels.size() <= maxBin) {
            switch (ipl.getBitDepth()) {
                case 8:
                    return new double[]{0,255};
                case 16:
                    return new double[]{0,65535};
                case 32:
                    return new double[]{0,1};
            }
        }

        // Getting the minimum and maximum values
        return new double[]{pixels.get(minBin),pixels.get(maxBin)};

    }

    public static double[] getAbsoluteRange(ImagePlus ipl, int channel) {
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;

        for (int slice = 0; slice < ipl.getNSlices(); slice++) {
            for (int frame = 0; frame < ipl.getNFrames(); frame++) {
                ipl.setPosition(channel + 1, slice + 1, frame + 1);
                ImageStatistics stats = ImageStatistics.getStatistics(ipl.getProcessor(), ImageStatistics.MIN_MAX, null);

                if (stats.min < min) min = stats.min;
                if (stats.max > max) max = stats.max;

            }
        }

        return new double[]{min,max};

    }

    public static double[] getAbsoluteRange(ImagePlus ipl, CoordinateSetI coordinateSet, int channel, int frame) {
        float minI = Float.MAX_VALUE;
        float maxI = -Float.MAX_VALUE;

        for (Point<Integer> point:coordinateSet) {
            ipl.setPosition(channel + 1, point.getZ() + 1, frame + 1);
            float val = ipl.getProcessor().getf(point.getX(),point.getY());

            minI = Math.min(minI,val);
            maxI = Math.max(maxI,val);

        }

        return new double[]{minI,maxI};

    }

    public static ArrayList<Float> getPixels(ImagePlus ipl, int channel) {
        // Arranging pixel values into ArrayList, then ordering by value
        ArrayList<Float> pixels = new ArrayList<>();

        for (int slice = 0; slice < ipl.getNSlices(); slice++) {
            for (int frame = 0; frame < ipl.getNFrames(); frame++) {
                ipl.setPosition(channel + 1, slice + 1, frame + 1);
                float[][] vals2D = ipl.getProcessor().getFloatArray();
                for (float[] vals1D:vals2D) {
                    for (float val:vals1D) {
                        if (!Float.isNaN(val)) {
                            pixels.add(val);
                        }
                    }
                }
            }
        }

        return pixels;

    }

    public static ArrayList<Float> getPixels(ImagePlus ipl, CoordinateSetI coordinateSet, int channel, int frame) {
        // Arranging pixel values into ArrayList, then ordering by value
        ArrayList<Float> pixels = new ArrayList<>();

        for (Point<Integer> point:coordinateSet) {
            ipl.setPosition(channel + 1, point.getZ() + 1, frame + 1);
            pixels.add(ipl.getProcessor().getf(point.getX(),point.getY()));
        }

        return pixels;

    }
}
