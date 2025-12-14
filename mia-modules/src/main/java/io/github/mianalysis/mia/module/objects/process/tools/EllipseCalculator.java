package io.github.mianalysis.mia.module.objects.process.tools;

import ij.gui.Roi;
import ij.process.ByteProcessor;
import ij.process.EllipseFitter;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeI;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeFactories;
import math.geom2d.Point2D;
import math.geom2d.conic.Ellipse2D;

public class EllipseCalculator {
    private VolumeI volume;
    private double xCent = Double.NaN;
    private double yCent = Double.NaN;
    private double angleDegs = Double.NaN;
    private double semiMajor = Double.NaN;
    private double semiMinor = Double.NaN;

    public EllipseCalculator(VolumeI volume) throws RuntimeException {
        if (!volume.is2D())
            this.volume = volume.getProjected();
        else
            this.volume = volume;

        fitEllipse(Double.MAX_VALUE, false);

    }

    public EllipseCalculator(VolumeI volume, boolean fitSurface) throws RuntimeException {
        if (!volume.is2D())
            this.volume = volume.getProjected();
        else
            this.volume = volume;

        fitEllipse(Double.MAX_VALUE, fitSurface);

    }

    public EllipseCalculator(VolumeI volume, double maxAxisLength) throws RuntimeException {
        if (!volume.is2D())
            this.volume = volume.getProjected();
        else
            this.volume = volume;

        fitEllipse(maxAxisLength, false);

    }

    public EllipseCalculator(VolumeI volume, double maxAxisLength, boolean fitSurface) throws RuntimeException {
        if (!volume.is2D())
            this.volume = volume.getProjected();
        else
            this.volume = volume;

        fitEllipse(maxAxisLength, fitSurface);

    }

    private void fitEllipse(double maxAxisLength, boolean fitSurface) throws RuntimeException {
        if (volume.size() <= 2)
            return;
        
        // Irrespective of whether the surface is fitted, get the ROI from the full object
        Roi roi = volume.getRoi(0);

        if (fitSurface)
            volume = volume.getSurface(false,false);

        ImageProcessor ipr = new ByteProcessor(volume.getWidth(), volume.getHeight());
        for (Point<Integer> pt:volume.getCoordinateSet())
            ipr.set(pt.x, pt.y, 255);

        ipr.setRoi(roi);

        EllipseFitter fitter = new EllipseFitter();
        fitter.fit(ipr, null);
        
        semiMajor = fitter.major / 2;
        semiMinor = fitter.minor / 2;

        if ((semiMajor > maxAxisLength)
                || (semiMinor <= 0 || semiMajor <= 0)
                || (Double.isNaN(semiMinor) || Double.isNaN(semiMajor))) {
            semiMajor = Double.NaN;
            semiMinor = Double.NaN;

            return;

        } else {
            xCent = fitter.xCenter;
            yCent = fitter.yCenter;
            angleDegs = -fitter.angle;
            
        }
    }

    public double getEllipseThetaRads() {
        return Math.toRadians(angleDegs);
    }

    public double getXCentre() {
        return xCent;
    }

    public double getYCentre() {
        return yCent;
    }

    public double[] getSemiAxes() {
        return new double[] { semiMajor, semiMinor };
    }

    public double getSemiMajorAxis() {
        return semiMajor;
    }

    public double getSemiMinorAxis() {
        return semiMinor;
    }

    public VolumeI getContainedPoints() {
        VolumeI insideEllipse = volume.getFactory().createVolumeFromExample(volume.getCoordinateSetFactory(),volume);

        Point2D cent = new Point2D(xCent, yCent);
        Ellipse2D ell = Ellipse2D.create(cent, semiMajor, semiMinor, getEllipseThetaRads(), true);

        for (int x = (int) Math.floor(xCent - semiMajor); x <= xCent + semiMajor; x++) {
            for (int y = (int) Math.floor(yCent - semiMajor); y <= yCent + semiMajor; y++) {
                if (ell.isInside(new Point2D(x, y))) {
                    try {
                        insideEllipse.addCoord(x, y, 0);
                    } catch (PointOutOfRangeException e) {
                    }
                }
            }
        }

        return insideEllipse;

    }
}
