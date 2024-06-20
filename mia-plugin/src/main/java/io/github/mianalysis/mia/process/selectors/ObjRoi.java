package io.github.mianalysis.mia.process.selectors;

import java.awt.Point;
import java.awt.Rectangle;

import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import io.github.mianalysis.mia.MIA;

public class ObjRoi {
    private final int ID;
    private final Roi roi;
    private final int t;
    private final int z;
    private final String assignedClass;

    ObjRoi(int ID, Roi roi, int t, int z) {
        this.ID = ID;
        this.roi = duplicateRoi(roi);
        this.t = t;
        this.z = z;
        this.assignedClass = null;
    }

    ObjRoi(int ID, Roi roi, int t, int z, String assignedClass) {
        this.ID = ID;
        this.roi = duplicateRoi(roi);
        this.t = t;
        this.z = z;
        this.assignedClass = assignedClass;
    }

    public static Roi duplicateRoi(Roi roi) {
        Roi newRoi;

        // Need to processAutomatic Roi depending on its type
        switch (roi.getType()) {
            case Roi.RECTANGLE:
                newRoi = new Roi(roi.getBounds());
                break;

            case Roi.OVAL:
                Rectangle bounds = roi.getBounds();
                newRoi = new OvalRoi(bounds.x, bounds.y, bounds.width, bounds.height);
                break;

            case Roi.FREEROI:
            case Roi.POLYGON:
            case Roi.TRACED_ROI:
                PolygonRoi polyRoi = (PolygonRoi) roi;
                int[] x = polyRoi.getXCoordinates();
                int[] xx = new int[x.length];
                for (int i = 0; i < x.length; i++)
                    xx[i] = x[i] + (int) polyRoi.getXBase();

                int[] y = polyRoi.getYCoordinates();
                int[] yy = new int[x.length];
                for (int i = 0; i < y.length; i++)
                    yy[i] = y[i] + (int) polyRoi.getYBase();

                newRoi = new PolygonRoi(xx, yy, polyRoi.getNCoordinates(), roi.getType());
                break;

            case Roi.FREELINE:
            case Roi.POLYLINE:
                polyRoi = (PolygonRoi) roi;

                x = polyRoi.getXCoordinates();
                xx = new int[x.length];
                for (int i = 0; i < x.length; i++)
                    xx[i] = x[i] + (int) polyRoi.getXBase();

                y = polyRoi.getYCoordinates();
                yy = new int[x.length];
                for (int i = 0; i < y.length; i++)
                    yy[i] = y[i] + (int) polyRoi.getYBase();

                newRoi = new PolygonRoi(xx, yy, polyRoi.getNCoordinates(), roi.getType());
                break;

            case Roi.LINE:
                Line line = (Line) roi;

                newRoi = new Line(line.x1, line.y1, line.x2, line.y2);
                break;

            case Roi.POINT:
                PointRoi pointRoi = (PointRoi) roi;

                Point[] points = pointRoi.getContainedPoints();
                int[] xxx = new int[points.length];
                int[] yyy = new int[points.length];
                for (int i = 0; i < points.length; i++) {
                    xxx[i] = points[i].x;
                    yyy[i] = points[i].y;
                }

                newRoi = new PointRoi(xxx, yyy, points.length);
                break;

            default:
                MIA.log.writeWarning("ROI type unsupported.  Using bounding box for selection.");
                newRoi = new Roi(roi.getBounds());
                break;
        }

        return newRoi;

    }

    public int getID() {
        return ID;
    }

    public Roi getRoi() {
        return roi;
    }

    public int getT() {
        return t;
    }

    public int getZ() {
        return z;
    }
    
    public String getAssignedClass() {
        return assignedClass;
    }

    @Override
    public String toString() {
        String str = "Object " + String.valueOf(ID) + ", T = " + (t + 1) + ", Z = " + (z + 1);
        
        if (assignedClass != null)
        str = str + ", class = "+assignedClass;
        
        return str;

    }

    public String getShortString() {
        String str = "ID" + String.valueOf(ID) + "_TR-1_T" + (t + 1) + "_Z" + (z + 1);

        if (assignedClass != null)
            str = str + "_"+assignedClass;

        return str;
        
    }
}
