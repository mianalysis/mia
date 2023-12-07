package ch.epfl.biop.wrappers.cellpose.ij2commands;

import ij.ImagePlus;

public class CellposeFactory {
    private Cellpose_SegmentImgPlusOwnModelAdvanced cellpose = new Cellpose_SegmentImgPlusOwnModelAdvanced();

    public Cellpose_SegmentImgPlusOwnModelAdvanced getCellpose() {
        return cellpose;
    }

    public ImagePlus getImagePlus() {
        return cellpose.cellpose_imp;
    }

    public void setImagePlus(ImagePlus ipl) {
        cellpose.imp = ipl;
    }

    public void setDiameter(int diameter) {
        cellpose.diameter = diameter;
    }

    public void setCellProbabilityThreshold(double cellProbabilityThreshold) {
        cellpose.cellproba_threshold = cellProbabilityThreshold;
    }
}