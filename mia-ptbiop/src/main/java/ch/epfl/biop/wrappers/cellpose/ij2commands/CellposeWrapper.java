package ch.epfl.biop.wrappers.cellpose.ij2commands;

import java.io.File;

import ij.ImagePlus;

public class CellposeWrapper extends Cellpose_SegmentImgPlusOwnModelAdvanced{
    public ImagePlus getLabels() {
        return this.cellpose_imp;
    }

    public void setImagePlus(ImagePlus ipl) {
        this.imp = ipl;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setModelPath(File modelPath) {
        this.model_path = modelPath;
    }

    public void setNucleiChannel(int nucleiChannel) {
        this.nuclei_channel = nucleiChannel;
    }

    public void setCytoChannel(int cytoChannel) {
        this.cyto_channel = cytoChannel;
    }

    public void setDimensionMode(String dimensionMode) {
        this.dimensionMode = dimensionMode;
    }

    public void setDiameter(int diameter) {
        this.diameter = diameter;
    }

    public void setCellProbabilityThreshold(double cellProbabilityThreshold) {
        this.cellproba_threshold = cellProbabilityThreshold;
    }

    public void setFlowThreshold(double flowThreshold) {
        this.flow_threshold = flowThreshold;
    }

    public void setAnisotropy(double anisotropy) {
        this.anisotropy = anisotropy;
    }

    public void setDiameterThreshold(double diameterThreshold) {
        this.diam_threshold = diameterThreshold;
    }

    public void setStitchThreshold(double stitchThreshold) {
        this.stitch_threshold = stitchThreshold;
    }

    public void setUseOmni(boolean useOmni) {
        this.omni = useOmni;
    }
    
    public void setUseClustering(boolean useClustering) {
        this.cluster = useClustering;
    }

    public void setAdditionalFlags(String additionalFlags) {
        this.additional_flags = additionalFlags;
    }
}