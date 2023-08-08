//TODO: Add math for getEnergy

//Gradient images are converted to generalised 2D double arrays.  This means different constructors could be used down
//the line for other image formats.

package io.github.mianalysis.mia.process.activecontour.energies;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.process.activecontour.physicalmodel.Vertex;

/**
 * Created by Stephen on 16/09/2016.
 */
public class ImageGradientEnergy extends Energy {
    private double[][] im_x;
    private double[][] im_y;

    public ImageGradientEnergy(double weight, double[][] im_x, double[][] im_y) {
        super(weight);

        this.im_x = im_x;
        this.im_y = im_y;

    }

    public ImageGradientEnergy(double weight, ImagePlus ipl_x, ImagePlus ipl_y) {
        super(weight);

        int w = ipl_x.getWidth();
        int h = ipl_x.getHeight();

        im_x = new double[w][h];
        im_y = new double[w][h];

        ImageProcessor ipr_x = ipl_x.getProcessor();
        ImageProcessor ipr_y = ipl_y.getProcessor();

        for (int r=0;r<h;r++) {
            for (int c=0;c<w;c++) {
                im_x[c][r] = ipr_x.getPixelValue(c,r);
                im_y[c][r] = ipr_y.getPixelValue(c,r);
            }
        }
    }

    public double getEnergy(Vertex node) {
        int x = Math.toIntExact(Math.round(node.getX()));
        int y = Math.toIntExact(Math.round(node.getY()));
        double w = im_x.length;
        double h = im_x[0].length;

        double term_x = 0;
        double term_y = 0;

        if (x >= 0 & x < w & y >= 0 & y < h) {
            term_x = Math.pow(im_x[x][y],2);
            term_y = Math.pow(im_y[x][y],2);

        }

        double energy = -weight*(term_x+term_y);

        return energy;

    }

}