package ch.epfl.biop.wrappers.cellpose.ij2commands;

import java.io.File;

import ij.ImagePlus;

public class CellposeWrapper extends Cellpose {
    protected double cellProbThreshold = 0;
    protected double flowThreshold = 0.4;
    protected double stitchThreshold = 0;
    protected double anisotropy = 1;
    protected boolean useGPU = true;
    protected boolean do3D = false;
    protected String additionalFlags = "";
    
    public CellposeWrapper() {
        this.additional_flags = "";
    }

    public ImagePlus getLabels() {
        return this.cellpose_imp;
    }

    public void setImagePlus(ImagePlus ipl) {
        this.imp = ipl;
    }

    public void setEnvPath(File envPath) {
        this.env_path = envPath;
    }

    public void setEnvType(String envType) {
        this.env_type = envType;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setModelPath(File modelPath) {
        this.model_path = modelPath;
    }

    public void setUseGPU(boolean useGPU) {
        this.useGPU = useGPU;
    }

    public void setDo3D(boolean do3D) {
        this.do3D = do3D;
    }

    public void setDiameter(double diameter) {
        this.diameter = (float) diameter;
    }

    public void setFlowThreshold(double flowThreshold) {
        this.flowThreshold = flowThreshold;

    }

    public void setCellProbabilityThreshold(double cellProbThreshold) {
        this.cellProbThreshold = cellProbThreshold;
    }

    public void setStitchThreshold(double stitchThreshold) {
        this.stitchThreshold = stitchThreshold;
    }

    public void setAnisotropy(double anisotropy) {
        this.anisotropy = anisotropy;
    }

    public void setAdditionalFlags(String additionalFlags) {
        this.additionalFlags = additionalFlags;
    }

    public void compileAdditionalFlags() {
        this.additional_flags = this.additional_flags + " --cellprob_threshold, " + cellProbThreshold+",";
        this.additional_flags = this.additional_flags + " --flow_threshold, " + flowThreshold+",";
        this.additional_flags = this.additional_flags + " --stitch_threshold, " + stitchThreshold+",";
        this.additional_flags = this.additional_flags + " --anisotropy, " + anisotropy+",";

        if (do3D)
            this.additional_flags = this.additional_flags + " --do_3D,";

        if (useGPU)
            this.additional_flags = this.additional_flags + " --use_gpu,";

        this.additional_flags = this.additional_flags + additionalFlags;

    }
}