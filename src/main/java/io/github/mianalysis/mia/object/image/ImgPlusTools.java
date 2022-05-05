package io.github.mianalysis.mia.object.image;

import ij.ImagePlus;
import io.github.mianalysis.mia.MIA;
import io.github.sjcross.sjcommon.object.volume.SpatCal;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class ImgPlusTools {
    public static <T> long[] getDimensionsXYCZT(ImgPlus<T> img) {
        long[] dims1 = new long[5];
        dims1[0] = img.dimension(img.dimensionIndex(Axes.X));
        dims1[1] = img.dimension(img.dimensionIndex(Axes.Y));
        dims1[2] = img.dimension(img.dimensionIndex(Axes.CHANNEL));
        dims1[3] = img.dimension(img.dimensionIndex(Axes.Z));
        dims1[4] = img.dimension(img.dimensionIndex(Axes.TIME));

        return dims1;

    }

    public static <T> void copyAxes(ImgPlus<T> sourceImg, ImgPlus<T> targetImg) {
        for (int i = 0; i < sourceImg.numDimensions(); i++) {
            targetImg.setAxis(sourceImg.axis(i), i);
        }
    }

    public static <T> void applyAxes(ImgPlus<T> sourceImg, ImagePlus targetImagePlus) {
        // Setting calibration
        int zIdx = sourceImg.dimensionIndex(Axes.Z);
        if (zIdx != -1)
            targetImagePlus.getCalibration().pixelDepth = sourceImg.axis(zIdx).calibratedValue(1);

        int tIdx = sourceImg.dimensionIndex(Axes.TIME);
        if (tIdx != -1) {
            targetImagePlus.getCalibration().frameInterval = sourceImg.axis(tIdx).calibratedValue(1);
            targetImagePlus.getCalibration().setTimeUnit(sourceImg.axis(tIdx).unit());
        }
    }

    public static <T> void applyDimensions(ImgPlus<T> sourceImg, ImagePlus targetImagePlus) {
        // Setting dimensions
        int nChannels = (int) sourceImg.dimension(sourceImg.dimensionIndex(Axes.CHANNEL));
        int nSlices = (int) sourceImg.dimension(sourceImg.dimensionIndex(Axes.Z));
        int nFrames = (int) sourceImg.dimension(sourceImg.dimensionIndex(Axes.TIME));
        targetImagePlus.setDimensions(nChannels, nSlices, nFrames);
    }

    public static <T extends RealType<T> & NativeType<T>> ImgPlus<T> createNewImgPlus(
            int w, int h, int nChannels, int nSlices, int nFrames, double dppXY, double dppZ, String units, T type) {
        // Counting number of non-zero dimensions
        // Note: Even single pixel X and Y dimensions are retained, whereas others have
        // to have at least 2 pixels (otherwise we get them by default)
        int nDims = 0;
        if (w > 0)
            nDims++;
        if (h > 0)
            nDims++;
        if (nChannels > 0)
            nDims++;
        if (nSlices > 0)
            nDims++;
        if (nFrames > 0)
            nDims++;

        // Creating the output image
        long[] dims = new long[nDims];
        CalibratedAxis[] axes = new CalibratedAxis[nDims];
        int count = 0;
        if (w != 0) {
            dims[count] = w;
            axes[count++] = new DefaultLinearAxis(Axes.X, units, dppXY);
        }
        if (h != 0) {
            dims[count] = h;
            axes[count++] = new DefaultLinearAxis(Axes.Y, units, dppXY);
        }
        if (nChannels > 0) {
            dims[count] = nChannels;
            axes[count++] = new DefaultLinearAxis(Axes.CHANNEL, "", 1);
        }
        if (nSlices > 0) {
            dims[count] = nSlices;
            axes[count++] = new DefaultLinearAxis(Axes.Z, units, dppZ);
        }
        if (nFrames > 0) {
            dims[count] = nFrames;
            axes[count++] = new DefaultLinearAxis(Axes.TIME, "", 1);
        }

        DiskCachedCellImgOptions options = ImgPlusImage.getCellImgOptions();
        ImgPlus<T> outputImg = new ImgPlus<T>(
                new DiskCachedCellImgFactory<T>(type, options).create(dims));

        // Copying calibration
        for (int d = 0; d < nDims; d++)
            outputImg.setAxis(axes[d], d);

        return outputImg;

    }

    public static <T extends RealType<T> & NativeType<T>> ImgPlus<T> createNewImgPlus(SpatCal spatCal, int nChannels,
            int nFrames, T type) {
        int w = spatCal.getWidth();
        int h = spatCal.getHeight();
        int nSlices = spatCal.getNSlices();
        double dppXY = spatCal.getDppXY();
        double dppZ = spatCal.getDppZ();
        String units = spatCal.getUnits();

        return createNewImgPlus(w, h, nChannels, nSlices, nFrames, dppXY, dppZ, units, type);

    }

    public static <T extends RealType<T> & NativeType<T>> ImgPlus<T> createNewImgPlus(ImgPlus<T> inputImg,
            int nChannels, int nSlices,
            int nFrames, T type) {
        int xIdx = inputImg.dimensionIndex(Axes.X);
        int yIdx = inputImg.dimensionIndex(Axes.Y);
        int zIdx = inputImg.dimensionIndex(Axes.Z);

        double dppXY = xIdx == -1 ? 1 : inputImg.axis(xIdx).calibratedValue(1);
        double dppZ = zIdx == -1 ? 1 : inputImg.axis(zIdx).calibratedValue(1);
        String units = xIdx == -1 ? "px" : inputImg.axis(xIdx).unit();

        int w = (int) (xIdx == -1 ? 1 : inputImg.dimension(xIdx));
        int h = (int) (yIdx == -1 ? 1 : inputImg.dimension(yIdx));

        return createNewImgPlus(w, h, nChannels, nSlices, nFrames, dppXY, dppZ, units, type);

    }

    public static <T extends RealType<T> & NativeType<T>, R extends RealType<R> & NativeType<R>> ImgPlus<R> createNewImgPlus(
            ImgPlus<T> inputImg, R type) {
        // Creating the output image
        long[] dims = new long[inputImg.numDimensions()];
        for (int i = 0; i < inputImg.numDimensions(); i++)
            dims[i] = inputImg.dimension(i);

        DiskCachedCellImgOptions options = ImgPlusImage.getCellImgOptions();
        ImgPlus<R> outputImg = new ImgPlus<R>(
                new DiskCachedCellImgFactory<R>(type, options).create(dims));

        // Copying calibration
        for (int d = 0; d < inputImg.numDimensions(); d++) {
            CalibratedAxis axIn = inputImg.axis(d);
            CalibratedAxis axOut = new DefaultLinearAxis(axIn.type(), axIn.unit(), axIn.calibratedValue(1));
            outputImg.setAxis(axOut, d);
        }

        return outputImg;

    }

    public static <T extends RealType<T> & NativeType<T>, R extends RealType<R> & NativeType<R>> ImgPlus<R> createNewSubHyperstackImg(
            ImgPlus<T> img, int[] c,
            int[] z, int[] t, R type) {
        int xIdx = img.dimensionIndex(Axes.X);
        int yIdx = img.dimensionIndex(Axes.Y);
        int cIdx = img.dimensionIndex(Axes.CHANNEL);
        int zIdx = img.dimensionIndex(Axes.Z);
        int tIdx = img.dimensionIndex(Axes.TIME);

        double dppXY = xIdx == -1 ? 1 : img.axis(xIdx).calibratedValue(1);
        double dppZ = zIdx == -1 ? 1 : img.axis(zIdx).calibratedValue(1);
        String units = xIdx == -1 ? "px" : img.axis(xIdx).unit();

        int w = (int) (xIdx == -1 ? 1 : img.dimension(xIdx));
        int h = (int) (yIdx == -1 ? 1 : img.dimension(yIdx));

        int[] cMod = c;
        if (cMod[1] == -1)
            cMod[1] = cIdx == -1 ? 0 : (int) img.dimension(cIdx) - 1;
        int nChannels = cMod[1] - cMod[0] + (cIdx != -1 ? 1 : 0);

        int[] zMod = z;
        if (zMod[1] == -1)
            zMod[1] = zIdx == -1 ? 0 : (int) img.dimension(zIdx) - 1;
        int nSlices = zMod[1] - zMod[0] + (zIdx != -1 ? 1 : 0);

        int[] tMod = t;
        if (tMod[1] == -1)
            tMod[1] = tIdx == -1 ? 0 : (int) img.dimension(tIdx) - 1;
        int nFrames = tMod[1] - tMod[0] + (tIdx != -1 ? 1 : 0);

        return createNewImgPlus(w, h, nChannels, nSlices, nFrames, dppXY, dppZ, units, type);

    }

    public static <T extends RealType<T> & NativeType<T>> long[][] getSubHyperstackInterval(ImgPlus<T> img, int[] c,
            int[] z,
            int[] t) {
        long[][] interval = new long[2][img.numDimensions()];

        for (int i = 0; i < img.numDimensions(); i++) {
            AxisType axisType = img.axis(i).type();
            if (axisType == Axes.X || axisType == Axes.Y) {
                interval[0][i] = 0;
                interval[1][i] = img.dimension(i) - 1;
            } else if (axisType == Axes.CHANNEL) {
                interval[0][i] = c[0];
                if (c[1] == -1)
                    interval[1][i] = img.dimension(i)-1;
                else
                    interval[1][i] = c[1];
            } else if (axisType == Axes.Z) {
                interval[0][i] = z[0];
                if (z[1] == -1)
                    interval[1][i] = img.dimension(i)-1;
                else
                    interval[1][i] = z[1];
            } else if (axisType == Axes.TIME) {
                interval[0][i] = t[0];
                if (t[1] == -1)
                    interval[1][i] = img.dimension(i)-1;
                else
                    interval[1][i] = t[1];
            }
        }

        return interval;

    }

    public static <T extends RealType<T> & NativeType<T>> long[][] getSliceInterval(ImgPlus<T> img, int c, int z,
            int t) {
        long[][] interval = new long[2][img.numDimensions()];

        for (int i = 0; i < img.numDimensions(); i++) {
            AxisType type = img.axis(i).type();
            if (type == Axes.X || type == Axes.Y) {
                interval[0][i] = 0;
                interval[1][i] = img.dimension(i) - 1;
            } else if (type == Axes.CHANNEL) {
                interval[0][i] = c;
                interval[1][i] = c;
            } else if (type == Axes.Z) {
                interval[0][i] = z;
                interval[1][i] = z;
            } else if (type == Axes.TIME) {
                interval[0][i] = t;
                interval[1][i] = t;
            }
        }

        return interval;

    }

    /*
     * The following method is based on that from John Bogovic via the image.sc
     * forum
     * (https://forum.image.sc/t/imglib2-force-wrapped-imageplus-rai-dimensions-to-
     * xyczt/56461/2), accessed 2022-03-30
     */
    public static <T extends RealType<T> & NativeType<T>> RandomAccessibleInterval<T> forceImgPlusToXYCZT(
            ImgPlus<T> imgIn) {
        RandomAccessibleInterval<T> raiOut = imgIn;

        if (imgIn.dimensionIndex(Axes.CHANNEL) == -1) {
            int nd = raiOut.numDimensions();
            raiOut = Views.moveAxis(Views.addDimension(raiOut, 0, 0), nd, 2);
        }

        if (imgIn.dimensionIndex(Axes.Z) == -1) {
            int nd = raiOut.numDimensions();
            raiOut = Views.moveAxis(Views.addDimension(raiOut, 0, 0), nd, 3);
        }

        if (imgIn.dimensionIndex(Axes.TIME) == -1) {
            int nd = raiOut.numDimensions();
            raiOut = Views.moveAxis(Views.addDimension(raiOut, 0, 0), nd, 4);
        }

        return raiOut;

    }

    public static <T extends RealType<T> & NativeType<T>> void reportDimensions(ImgPlus<T> img) {
        for (int i = 0; i < img.numDimensions(); i++) {
            CalibratedAxis axis = img.axis(i);
            MIA.log.writeDebug("Index " + i + ": Type=" + axis.type() + ", length=" + img.dimension(i)
                    + ", calibration=" + axis.calibratedValue(1)
                    + ", units=" + axis.unit());
        }
    }

    public static <T extends RealType<T> & NativeType<T>> void reportDimensions(RandomAccessibleInterval<T> rai) {
        for (int i = 0; i < rai.numDimensions(); i++)
            MIA.log.writeDebug("Index " + i + ": Length=" + rai.dimension(i));
    }
}
