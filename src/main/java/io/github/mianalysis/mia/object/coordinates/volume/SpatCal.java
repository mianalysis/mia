package io.github.mianalysis.mia.object.coordinates.volume;

import ij.ImagePlus;
import ij.measure.Calibration;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class SpatCal {
    final public double dppXY; // Calibration in xy
    final public double dppZ; // Calibration in z
    final public String units;

    final public int width;
    final public int height;
    final public int nSlices;

    public SpatCal(double dppXY, double dppZ, String units, int width, int height, int nSlices) {
        this.dppXY = dppXY;
        this.dppZ = dppZ;
        this.units = units;
        this.width = width;
        this.height = height;
        this.nSlices = nSlices;
    }

    public SpatCal duplicate() {
        return new SpatCal(dppXY, dppZ, units, width, height, nSlices);
    }

    public static SpatCal getFromImage(ImagePlus ipl) {
        Calibration calibration = ipl.getCalibration();

        int w = ipl.getWidth();
        int h = ipl.getHeight();
        int nSlices = ipl.getNSlices();
        double dppXY = calibration.getX(1);
        double dppZ = calibration.getZ(1);
        String units = calibration.getUnits();

        return new SpatCal(dppXY, dppZ, units, w, h, nSlices);

    }

    public static <T extends RealType<T> & NativeType<T>> SpatCal getFromImage(ImgPlus<T> img) {
        int xIdx = img.dimensionIndex(Axes.X);
        int yIdx = img.dimensionIndex(Axes.Y);
        int zIdx = img.dimensionIndex(Axes.Z);

        double dppXY = xIdx == -1 ? 1 : img.axis(xIdx).calibratedValue(1);
        double dppZ = zIdx == -1 ? 1 : img.axis(zIdx).calibratedValue(1);
        String units = xIdx == -1 ? "px" : img.axis(xIdx).unit();

        int w = (int) (xIdx == -1 ? 1 : img.dimension(xIdx));
        int h = (int) (yIdx == -1 ? 1 : img.dimension(yIdx));
        int nSlices = (int) (zIdx == -1 ? 1 : img.dimension(zIdx));

        return new SpatCal(dppXY, dppZ, units, w, h, nSlices);

    }

    public void setImageCalibration(ImagePlus ipl) {
        ipl.getCalibration().pixelWidth = dppXY;
        ipl.getCalibration().pixelHeight = dppXY;
        ipl.getCalibration().pixelDepth = nSlices == 1 ? 1 : dppZ;
        ipl.getCalibration().setUnit(units);

    }

    public Calibration createImageCalibration() {
        Calibration calibration = new Calibration();

        calibration.pixelWidth = dppXY;
        calibration.pixelHeight = dppXY;
        calibration.pixelDepth = dppZ;
        calibration.setUnit(units);

        return calibration;

    }

    public double getDppXY() {
        return dppXY;
    }

    public double getDppZ() {
        return dppZ;
    }

    public String getUnits() {
        return units;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getNSlices() {
        return nSlices;
    }
}
