package io.github.mianalysis.mia.process.houghtransform.pixelarrays;

/**
 * Created by sc13967 on 12/01/2018.
 */
public class ShortPixelArray extends PixelArray {
    short[] pixels;

    public ShortPixelArray(short[] pixels, int[] dim) {
        super(dim);
        this.pixels = pixels;

    }

    @Override
    public int length() {
        return pixels.length;
    }

    @Override
    public double getPixelValue(int i) {
        return pixels[i];
    }
}
