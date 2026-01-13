package io.github.mianalysis.mia.object.coordinates;

import ij.ImagePlus;

public interface SpatiallyCalibrated {
    public int getWidth();

    public void setWidth(int width);

    public int getHeight();

    public void setHeight(int height);

    public int getNSlices();

    public void setNSlices(int nSlices);

    public double getDppXY();

    public void setDppXY(double dppXY);

    public double getDppZ();

    public void setDppZ(double dppZ);

    public String getSpatialUnits();

    public void setSpatialUnits(String spatialUnits);

    public default void applySpatialCalibrationToImage(ImagePlus ipl) {
        ipl.getCalibration().pixelWidth = getDppXY();
        ipl.getCalibration().pixelHeight = getDppXY();
        ipl.getCalibration().pixelDepth = getNSlices() == 1 ? 1 : getDppZ();
        ipl.getCalibration().setUnit(getSpatialUnits());
        
    }

    public default void setSpatialCalibrationFromExample(SpatiallyCalibrated example) {
        setWidth(example.getWidth());
        setHeight(example.getHeight());
        setNSlices(example.getNSlices());
        setDppXY(example.getDppXY());
        setDppZ(example.getDppZ());
        setSpatialUnits(example.getSpatialUnits());

    }
}
