package ch.epfl.biop.wrappers.cellpose.ij2commands;

import ij.ImagePlus;

public class CellposeWrapper extends Cellpose_SegmentImgPlusOwnModelAdvanced{
    public ImagePlus getImagePlus() {
        return this.cellpose_imp;
    }

    public void setImagePlus(ImagePlus ipl) {
        this.imp = ipl;
    }

    public void setDiameter(int diameter) {
        this.diameter = diameter;
    }

    public void setCellProbabilityThreshold(double cellProbabilityThreshold) {
        this.cellproba_threshold = cellProbabilityThreshold;
    }
}