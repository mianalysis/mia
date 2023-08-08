package io.github.mianalysis.mia.process.houghtransform.transforms;

import java.util.ArrayList;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.process.houghtransform.accumulators.AbstractAccumulator;
import io.github.mianalysis.mia.process.houghtransform.pixelarrays.BytePixelArray;
import io.github.mianalysis.mia.process.houghtransform.pixelarrays.FloatPixelArray;
import io.github.mianalysis.mia.process.houghtransform.pixelarrays.PixelArray;
import io.github.mianalysis.mia.process.houghtransform.pixelarrays.ShortPixelArray;

/**
 * Created by sc13967 on 12/01/2018.
 */
public abstract class AbstractTransform {
    protected PixelArray pixels;
    protected AbstractAccumulator accumulator;
    int nThreads = 1;

    public AbstractTransform(ImageProcessor ipr) {
        // Getting dimensions of the input image
        int[] dims = new int[] { ipr.getWidth(), ipr.getHeight() };

        // Converting the image to a 1D pixel array
        switch (ipr.getBitDepth()) {
            case 8:
                pixels = new BytePixelArray((byte[]) ipr.getPixels(), dims);
                break;

            case 16:
                pixels = new ShortPixelArray((short[]) ipr.getPixels(), dims);
                break;

            case 32:
                pixels = new FloatPixelArray((float[]) ipr.getPixels(), dims);
                break;
        }
    }

    public AbstractTransform(ImageStack ist) {
        // Getting dimensions of the input image
        int[] dims = new int[] { ist.getWidth(), ist.getHeight(), ist.size() };

        // Converting the image to a 1D pixel array
        int i = 0;
        switch (ist.getBitDepth()) {
            case 8:
                byte[] bytePixels = new byte[ist.getWidth() * ist.getHeight() * ist.size()];
                for (int z = 1; z <= ist.size(); z++)
                    for (byte pixel : (byte[]) ist.getPixels(z))
                        bytePixels[i++] = pixel;
                pixels = new BytePixelArray(bytePixels, dims);
                break;

            case 16:
                short[] shortPixels = new short[ist.getWidth() * ist.getHeight() * ist.size()];
                for (int z = 1; z <= ist.size(); z++)
                    for (short pixel : (short[]) ist.getPixels(z))
                        shortPixels[i++] = pixel;
                pixels = new ShortPixelArray(shortPixels, dims);
                break;

            case 32:
                float[] floatPixels = new float[ist.getWidth() * ist.getHeight() * ist.size()];
                for (int z = 1; z <= ist.size(); z++)
                    for (float pixel : (byte[]) ist.getPixels(z))
                        floatPixels[i++] = pixel;
                pixels = new FloatPixelArray(floatPixels, dims);
                break;
        }
    }

    /**
     * Performs the Hough Transform on the image passed to the constructor. This
     * should be an edge image.
     */
    public abstract void run();

    public void addDetectedObjectsOverlay(ImagePlus ipl, ArrayList<double[]> objects) {
        accumulator.addDetectedObjectsOverlay(ipl, objects);

    }

    /**
     * Normalises scores to the number of points that contribute to that position.
     */
    public void normaliseScores() {
        accumulator.normaliseScores();
    }

    public ArrayList<double[]> getObjects(double minScore, int exclusionR) {
        return accumulator.getObjects(minScore, exclusionR);
    }

    public ArrayList<double[]> getNObjects(int nObjects, int exclusionR) {
        return accumulator.getNObjects(nObjects, exclusionR);
    }

    public ImagePlus getAccumulatorAsImage() {
        return accumulator.getAccumulatorAsImage();

    }

    public int getnThreads() {
        return nThreads;
    }

    public void setnThreads(int nThreads) {
        this.nThreads = nThreads;
    }
}
