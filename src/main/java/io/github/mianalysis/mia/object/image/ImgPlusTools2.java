package io.github.mianalysis.mia.object.image;

import io.github.mianalysis.mia.MIA;
import io.github.sjcross.common.object.volume.SpatCal;
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

public class ImgPlusTools2 {
    public static <T extends RealType<T> & NativeType<T>> ImgPlus<T> createNewImgPlus(
            int w, int h, int nChannels, int nSlices, int nFrames, double dppXY, double dppZ, String units, T type) {
        // Counting number of non-zero dimensions
        // Note: Even single pixel X and Y dimensions are retained, whereas others have to have at least 2 pixels (otherwise we get them by default)
        int nDims = 0;
        if (w > 0)
            nDims++;
        if (h > 0)
            nDims++;
        if (nChannels > 1)
            nDims++;
        if (nSlices > 1)
            nDims++;
        if (nFrames > 1)
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
        if (nChannels > 1) {
            dims[count] = nChannels;
            axes[count++] = new DefaultLinearAxis(Axes.CHANNEL, "", 1);
        }
        if (nSlices > 1) {
            dims[count] = nSlices;
            axes[count++] = new DefaultLinearAxis(Axes.Z, units, dppZ);
        }
        if (nFrames > 1) {
            dims[count] = nFrames;
            axes[count++] = new DefaultLinearAxis(Axes.TIME, "", 1);
        }

        DiskCachedCellImgOptions options = ImgPlusImage.getCellImgOptions();
        ImgPlus<T> outputImg = new ImgPlus<>(
                new DiskCachedCellImgFactory(type, options).create(dims));

        // Copying calibration
        for (int d = 0; d < nDims; d++)
            outputImg.setAxis(axes[d], d);

        return outputImg;

    }
    
    public static <T extends RealType<T> & NativeType<T>> ImgPlus<T> createNewImgPlus(SpatCal spatCal, int nChannels, int nFrames, T type) {
        int w = spatCal.getWidth();
        int h = spatCal.getHeight();
        int nSlices = spatCal.getNSlices();
        double dppXY = spatCal.getDppXY();
        double dppZ = spatCal.getDppZ();
        String units = spatCal.getUnits();
        
        return createNewImgPlus(w, h, nChannels, nSlices, nFrames, dppXY, dppZ, units, type);

    } 

    public static <T extends RealType<T> & NativeType<T>, R extends RealType<R> & NativeType<R>> ImgPlus<R> createNewImgPlus(
            ImgPlus<T> inputImg, R type) {
        // Creating the output image
        long[] dims = new long[inputImg.numDimensions()];
        for (int i = 0; i < inputImg.numDimensions(); i++)
            dims[i] = inputImg.dimension(i);

        DiskCachedCellImgOptions options = ImgPlusImage.getCellImgOptions();
        ImgPlus<R> outputImg = new ImgPlus<>(
                new DiskCachedCellImgFactory(type, options).create(dims));

        // Copying calibration
        for (int d = 0; d < inputImg.numDimensions(); d++) {
            CalibratedAxis axIn = inputImg.axis(d);
            CalibratedAxis axOut = new DefaultLinearAxis(axIn.type(), axIn.unit(), axIn.calibratedValue(1));
            outputImg.setAxis(axOut, d);
        }

        return outputImg;

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
     * The following method is from John Bogovic via the image.sc forum
     * (https://forum.image.sc/t/imglib2-force-wrapped-imageplus-rai-dimensions-to-
     * xyczt/56461/2), accessed 2022-03-30
     */
    public static <T extends RealType<T> & NativeType<T>> RandomAccessibleInterval<T> forceImgPlusToXYCZT(
            ImgPlus<T> imgIn) {
        RandomAccessibleInterval<T> raiOut = imgIn;

        if (imgIn.dimensionIndex(Axes.CHANNEL) == -1) {
            int nd = raiOut.numDimensions();
            raiOut = Views.permute(Views.addDimension(raiOut, 0, 0), 2, nd);
        }

        if (imgIn.dimensionIndex(Axes.Z) == -1) {
            int nd = raiOut.numDimensions();
            raiOut = Views.permute(Views.addDimension(raiOut, 0, 0), 3, nd);
        }

        if (imgIn.dimensionIndex(Axes.TIME) == -1) {
            int nd = raiOut.numDimensions();
            raiOut = Views.permute(Views.addDimension(raiOut, 0, 0), 4, nd);
        }

        return raiOut;

    }

    public static <T extends RealType<T> & NativeType<T>> void reportDimensions(ImgPlus<T> img) {
        for (int i = 0; i < img.numDimensions(); i++) {
            CalibratedAxis axis = img.axis(i);
            MIA.log.writeDebug("Index " + i + ": Type=" + axis.type() + ", length="+img.dimension(i)+", calibration=" + axis.calibratedValue(1)
                    + ", units=" + axis.unit());
        }
    }
}
