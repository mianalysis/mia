package io.github.mianalysis.mia.process.activecontour.physicalmodel;

import java.util.HashSet;
import java.util.Iterator;

import ij.gui.PolygonRoi;
import ij.gui.Roi;
import io.github.mianalysis.mia.process.math.CumStat;

/**
 * Created by Stephen on 07/09/2016.
 */
public class NodeCollection extends HashSet<Vertex> {

    private static final long serialVersionUID = -4038010312282727225L;

    public void setFixedX(boolean fixed_x) {
        Iterator<Vertex> itor = iterator();
        while (itor.hasNext()) {
            itor.next().setFixedX(fixed_x);

        }
    }

    public void setFixedY(boolean fixed_y) {
        Iterator<Vertex> itor = iterator();
        while (itor.hasNext()) {
            itor.next().setFixedY(fixed_y);

        }
    }

    public Vertex getSpecialNode(int special) {
        Vertex node = null;

        Iterator<Vertex> itor = iterator();
        while (itor.hasNext()) {
            node = itor.next();
            if (node.getSpecial() == special) {
                return node;
            }
        }

        return node;

    }

    public Vertex getRandomNode() {
        Iterator<Vertex> iterator = iterator();

        return iterator.next();

    }

    /**
     * Returns the coordinates of all nodes in the NodeCollection.  Works in a clockwise manner from a randomly selected
     * node (only works for contours, not grids).
     * @return 2D array of coordinates.  First column are X-values, second column are Y-values.
     */
    public double[][] getNodeCoordinates() {
        double[][] coords = new double[size()][2];

        Vertex node = getRandomNode();
        int ID = node.getID();
        coords[0][0] = node.getX();
        coords[0][1] = node.getY();
        node = node.getRightNeighbour();

        int i=1;
        while (node.getID() != ID) {
            coords[i][0] = node.getX();
            coords[i][1] = node.getY();
            node = node.getRightNeighbour();
            i++;

        }

        return coords;

    }

    public Roi getROI() {
        double[][] coords = getNodeCoordinates();
        int[] newXCoords = new int[coords.length];
        int[] newYCoords = new int[coords.length];

        for (int i=0;i<coords.length;i++) {
            newXCoords[i] = (int) coords[i][0];
            newYCoords[i] = (int) coords[i][1];
        }

        return new PolygonRoi(newXCoords,newYCoords,newXCoords.length,Roi.POLYGON);

    }

    public double getAverageDistanceMoved() {
        CumStat cumStat = new CumStat();

        for (Vertex vertex:this) cumStat.addMeasure(vertex.getDistanceMoved());

        return cumStat.getMean();

    }
}