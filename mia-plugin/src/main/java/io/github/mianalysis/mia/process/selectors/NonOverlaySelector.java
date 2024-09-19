package io.github.mianalysis.mia.process.selectors;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.gui.Wand;
import ij.plugin.filter.ThresholdToSelection;
import ij.process.ImageProcessor;

public class NonOverlaySelector implements MouseListener {
    private final ImagePlus ipl;
    private final ImagePlus binaryIpl;

    public NonOverlaySelector(ImagePlus ipl) {
        this.ipl = ipl;

        binaryIpl = convertOverlayToBinary(ipl);

        ipl.getWindow().getCanvas().addMouseListener(this);

    }

    public static ImagePlus convertOverlayToBinary(ImagePlus ipl) {
        ImagePlus binaryIpl = IJ.createImage("Binary", ipl.getWidth(), ipl.getHeight(), 1, 8);

        Overlay overlay = ipl.getOverlay();

        // If there is no overlay, return a black image, effectively saying everywhere
        // can be selected
        if (overlay == null)
            return binaryIpl;

        ImageProcessor binaryIpr = binaryIpl.getProcessor();
        Iterator<Roi> iterator = overlay.iterator();
        while (iterator.hasNext()) {
            Roi currRoi = iterator.next();
            if (!(currRoi instanceof TextRoi))
                for (Point point : currRoi.getContainedPoints())
                    binaryIpr.putPixel((int) point.getX(), (int) point.getY(), 255);
        }

        return binaryIpl;

    }

    public static PolygonRoi getRegionAtClick(ImagePlus binaryIpl, int x, int y) {
        // Identify contiguous region centered on selected point
        Wand wand = new Wand(binaryIpl.getProcessor());
        wand.autoOutline(x, y);
        if (wand.npoints > 0)
            return new PolygonRoi(wand.xpoints, wand.ypoints, wand.npoints, PolygonRoi.FREEROI);
        else
            return null;

    }

    public static Roi getRegionInsideRoi(ImagePlus binaryIpl, Roi inputRoi) {
        ImagePlus tempBinaryIpl = binaryIpl.duplicate();
        ImageProcessor binaryIpr = tempBinaryIpl.getProcessor();

        binaryIpr.setBackgroundValue(0);
        binaryIpr.setColor(255);
        binaryIpr.fillOutside(inputRoi);
        binaryIpr.setThreshold(0, 0, ImageProcessor.NO_LUT_UPDATE);

        return ThresholdToSelection.run(tempBinaryIpl);

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        ImageCanvas canvas = ipl.getWindow().getCanvas();
        int x = canvas.offScreenX(e.getX());
        int y = canvas.offScreenX(e.getY());

        // No longer listen for more clicks
        ipl.getWindow().getCanvas().removeMouseListener(this);

        PolygonRoi selectedRoi = getRegionAtClick(binaryIpl, x, y);
        if (selectedRoi != null)
            ipl.setRoi(selectedRoi);

    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
