package io.github.mianalysis.mia.process.skeleton;

import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedHashSet;

import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import io.github.mianalysis.mia.object.coordinates.Vertex;
import io.github.mianalysis.mia.object.coordinates.VertexCollection;

/**
 * Created by sc13967 on 25/01/2018.
 */
public class SkeletonVisualiser {
    Color line_col = Color.BLUE;
    Color node_col = Color.RED;
    double vertexR = 1;

    public void drawOverlay(VertexCollection vertices, ImagePlus ipl) {
        Overlay ovl = new Overlay();

        for (Vertex vertex:vertices) {
            //Adding current vertex
            ovl.addElement(drawNode(vertex));

            for (Vertex neighbour:vertex.getNeighbours()){
                ovl.add(drawLine(vertex, neighbour));
            }
        }

        ipl.setOverlay(ovl);

    }

    public void drawPath(LinkedHashSet<Vertex> vertices, ImagePlus ipl) {
        Overlay ovl = new Overlay();

        Iterator<Vertex> iterator = vertices.iterator();
        Vertex vertex1 = iterator.next();
        Vertex vertex2 = null;

        while (iterator.hasNext()) {
            vertex2 = iterator.next();

            //Adding current vertex
            ovl.addElement(drawNode(vertex1));
            ovl.add(drawLine(vertex1,vertex2));

            vertex1 = vertex2;

        }

        ovl.addElement(drawNode(vertex2));

        ipl.setOverlay(ovl);
    }

    private OvalRoi drawNode(Vertex vertex) {
        double x = vertex.getX();
        double y = vertex.getY();

        OvalRoi ovr = new OvalRoi(x- vertexR /2+0.5,y- vertexR /2+0.5, vertexR, vertexR);
        ovr.setStrokeColor(node_col);

        return ovr;

    }

    private Line drawLine(Vertex vertex1, Vertex vertex2) {
        if (vertex2 == null) {
            return null;
        }

        double x1 = vertex1.getX();
        double y1 = vertex1.getY();
        double x2 = vertex2.getX();
        double y2 = vertex2.getY();

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

    public void setVertexRadius(double val) {
        this.vertexR = val;

    }
}