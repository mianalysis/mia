package io.github.mianalysis.mia.process.activecontour.visualisation;

import java.awt.Color;
import java.util.Iterator;

import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import io.github.mianalysis.mia.process.activecontour.physicalmodel.NodeCollection;
import io.github.mianalysis.mia.process.activecontour.physicalmodel.Vertex;

/**
 * Created by Stephen on 08/09/2016.
 */
public class GridOverlay {
    Color line_col = Color.MAGENTA;
    Color node_col = Color.CYAN;
    double node_r = 1;

    public void drawOverlay(NodeCollection nodes, ImagePlus ipl) {
        Overlay ovl = new Overlay();

        Iterator<Vertex> iterator = nodes.iterator();

        while (iterator.hasNext()) {

            Vertex node = iterator.next();

            //Adding current node
            OvalRoi ovr = drawNode(node);
            ovr.setFillColor(Color.getHSBColor(1f,1f,(float) node.getEnergy()/3000));
            ovl.addElement(ovr);

            //Adding neighbour links
            Line left_line = drawLine(node, node.getLeftNeighbour());
            if (left_line != null) {ovl.add(left_line); }
            Line right_line = drawLine(node, node.getRightNeighbour());
            if (right_line != null) {ovl.add(right_line); }

        }

        ipl.setOverlay(ovl);

    }

    private OvalRoi drawNode(Vertex node) {
        double x = node.getX();
        double y = node.getY();

        OvalRoi ovr = new OvalRoi(x-node_r/2+0.5,y-node_r/2+0.5,node_r,node_r);
        ovr.setStrokeColor(node_col);

        return ovr;

    }

    private Line drawLine(Vertex node1, Vertex node2) {
        if (node2 == null) {
            return null;

        }

        double x1 = node1.getX();
        double y1 = node1.getY();
        double x2 = node2.getX();
        double y2 = node2.getY();

        Line line = new Line(x1+0.5,y1+0.5,x2+0.5,y2+0.5);
        line.setStrokeColor(line_col);

        return line;

    }

    public void setLineCol(Color col) {
        this.line_col = col;

    }

    public void setNodeCol(Color col) {
        this.node_col = col;

    }

    public void setNodeRadius(double val) {
        this.node_r = val;

    }
}
