package io.github.mianalysis.mia.process.activecontour.energies;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.object.coordinates.voxel.BresenhamLine;
import io.github.mianalysis.mia.process.activecontour.physicalmodel.Vertex;
import io.github.mianalysis.mia.process.math.CumStat;

/**
 * Created by sc13967 on 23/09/2016.
 */
public class PathEnergy extends Energy{
    private double[][] im;

    public PathEnergy(double weight, double[][] im) {
        super(weight);
        this.im = im;

    }

    public PathEnergy(double weight, ImagePlus ipl) {
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
        double path_energy_1 = 0;
        double path_energy_2 = 0;
        double path_energy_3 = 0;
        double path_energy_4 = 0;

        if (node.getLeftNeighbour() != null) {
            path_energy_1 = energyAlongPath(node, node.getLeftNeighbour());

        }

        if (node.getRightNeighbour() != null) {
            path_energy_2 = energyAlongPath(node, node.getRightNeighbour());
        }

        if (node.getTopNeighbour() != null) {
            path_energy_3 = energyAlongPath(node, node.getTopNeighbour());
        }

        if (node.getBottomNeighbour() != null) {
            path_energy_4 = energyAlongPath(node, node.getBottomNeighbour());
        }

        double term = path_energy_1 + path_energy_2 + path_energy_3 + path_energy_4;

        return weight*term;

    }

    private double energyAlongPath(Vertex node1, Vertex node2) {
        int x1 = (int) Math.round(node1.getX());
        int y1 = (int) Math.round(node1.getY());
        int x2 = (int) Math.round(node2.getX());
        int y2 = (int) Math.round(node2.getY());

        // If the node point is outside the image window, apply a massive penalty
        if (x1<0 || y1<0 || x1>im.length || y1>im[0].length) return Double.MAX_VALUE;

        int[][] line = BresenhamLine.getLine(x1,x2,y1,y2);

        CumStat cs = new CumStat();

        for (int i=0;i<line.length;i++) {
            int x = line[i][0];
            int y = line[i][1];

            if (x < 0 || x >= im.length || y < 0 || y >= im[0].length) cs.addMeasure(Double.POSITIVE_INFINITY);
            else cs.addMeasure(Math.pow(im[x][y],2));
        }

        return cs.getMean();

    }
}
