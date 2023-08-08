package io.github.mianalysis.mia.process.houghtransform.pixelarrays;

/**
 * Created by sc13967 on 12/01/2018.
 */
public class BytePixelArray extends PixelArray {
    byte[] pixels;

    public BytePixelArray(byte[] pixels, int[] dim) {
        super(dim);
        this.pixels = pixels;
    }

    @Override
    public int length() {
        return pixels.length;
    }

    @Override
    public double getPixelValue(int i) {        
        return (pixels[i] & 0xff);
    }
}
