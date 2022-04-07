package io.github.mianalysis.mia.object.image;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class ImgPlusTools2 {
    public static <T extends RealType<T> & NativeType<T>, R extends RealType<R> & NativeType<R>> ImgPlus<R> createNewImgPlus(ImgPlus<T> inputImg, R type) {
        // Creating the output image
        long[] dims = new long[inputImg.numDimensions()];
        for (int i = 0; i < inputImg.numDimensions(); i++)
            dims[i] = inputImg.dimension(i);
        
        DiskCachedCellImgOptions options = ImgPlusImage.getCellImgOptions();
        ImgPlus<R> outputImg = new ImgPlus<>(
                new DiskCachedCellImgFactory(type, options).create(dims));

        return outputImg;

    }

    public static <T extends RealType<T> & NativeType<T>> long[][] getSliceInterval(ImgPlus<T> img, int c, int z, int t) {
        long[][] interval = new long[2][img.numDimensions()];

        for (int i = 0; i < img.numDimensions(); i++) {
            AxisType type = img.axis(i).type();
            if (type == Axes.X || type == Axes.Y) {
                interval[0][i] = 0;
                interval[1][i] = img.dimension(i)-1;
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
}
