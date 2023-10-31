//TODO: Add math for getEnergy
//TODO: Check if the energy here wants to be the squared pixel value

//Gradient images are converted to generalised 2D double arrays.  This means different constructors could be used down
//the line for other image formats.

package io.github.mianalysis.mia.process.activecontour.energies;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.process.activecontour.physicalmodel.Vertex;

/**
 * Created by Stephen on 16/09/2016.
 */
public class ImageEnergy extends Energy {
    private double[][] im;

    public ImageEnergy(double weight, double[][] im) {
        super(weight);

        this.im = im;

    }

    public ImageEnergy(double weight, ImagePlus ipl) {
        super(weight);

        int w = ipl.getWidth();
        int h = ipl.getHeight();

        im = new double[w][h];

        ImageProcessor ipr = ipl.getProcessor();

        for (int r=0;r<h;r++) {
            for (int c=0;c<w;c++) {
                im[c][r] = ipr.getPixelValue(c,r);
            }
        }
    }

    public double getEnergy(Vertex node) {
        int x = Math.toIntExact(Math.round(node.getX()));
        int y = Math.toIntExact(Math.round(node.getY()));
        double w = im.length;
        double h = im[0].length;

        double term;

        if (x >= 0 & x < w & y >= 0 & y < h) term = Math.pow(im[x][y],2);
        else term = Double.POSITIVE_INFINITY;

        return weight*term;

    }
}