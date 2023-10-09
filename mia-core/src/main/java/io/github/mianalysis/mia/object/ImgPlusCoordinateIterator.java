package io.github.mianalysis.mia.object;

import java.util.Iterator;

import io.github.mianalysis.mia.object.coordinates.Point;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class ImgPlusCoordinateIterator<T extends RealType<T> & NativeType<T>> implements Iterator<net.imglib2.Point> {
    private final Iterator<Point<Integer>> iterator;
    private final ImgPlus<T> imgPlus;
    private final int c;
    private final int t;
    private final int nDims;

    public ImgPlusCoordinateIterator(Iterator<Point<Integer>> iterator, ImgPlus<T> imgPlus, int c, int t) {
        this.iterator = iterator;
        this.imgPlus = imgPlus;
        this.c = c;
        this.t = t;

        nDims = imgPlus.numDimensions();

    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public net.imglib2.Point next() {
        // Getting the input point
        Point<Integer> ptIn = iterator.next();

        // Creating the output point
        net.imglib2.Point ptOut = new net.imglib2.Point(nDims);

        for (int dim = 0; dim < nDims; dim++) {
            AxisType axisType = imgPlus.axis(dim).type();
            if (axisType == Axes.X)
                ptOut.setPosition(ptIn.x, dim);
            else if (axisType == Axes.Y)
                ptOut.setPosition(ptIn.y, dim);
            else if (axisType == Axes.CHANNEL)
                ptOut.setPosition(c, dim);
            else if (axisType == Axes.Z)
                ptOut.setPosition(ptIn.z, dim);
            else if (axisType == Axes.TIME)
                ptOut.setPosition(t, dim);

        }

        return ptOut;

    }
}