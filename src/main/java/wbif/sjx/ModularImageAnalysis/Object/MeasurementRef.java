package wbif.sjx.ModularImageAnalysis.Object;

/**
 * Created by sc13967 on 01/12/2017.
 */
public class MeasurementRef extends Reference {
    private boolean calculated = true;
    private String imageObjName = "";
    private String description = "";
    private String nickname = "";
    private boolean exportGlobal = true; // This is mainly for the GUI
    private boolean exportIndividual = true;
    private boolean exportMean = true;
    private boolean exportMin = true;
    private boolean exportMax = true;
    private boolean exportSum = true;
    private boolean exportStd = true;


    public MeasurementRef(String name) {
        super(name);
        this.nickname = name;
    }

    public MeasurementRef(String name, String imageObjName) {
        super(name);
        this.imageObjName = imageObjName;
        this.nickname = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getFinalName() {
        int idx = name.lastIndexOf("//");

        return name.substring(idx+2);

    }

    public boolean isCalculated() {
        return calculated;
    }

    public MeasurementRef setCalculated(boolean calculated) {
        this.calculated = calculated;
        return this;
    }

    public void setExportGlobal(boolean exportGlobal) {
        this.exportGlobal = exportGlobal;
    }

    public boolean isExportGlobal() {
        return exportGlobal;
    }

    public boolean isExportIndividual() {
        return exportIndividual;
    }

    public MeasurementRef setExportIndividual(boolean exportIndividual) {
        this.exportIndividual = exportIndividual;
        return this;
    }

    public boolean isExportMean() {
        return exportMean;
    }

    public MeasurementRef setExportMean(boolean exportMean) {
        this.exportMean = exportMean;
        return this;
    }

    public boolean isExportMin() {
        return exportMin;
    }

    public MeasurementRef setExportMin(boolean exportMin) {
        this.exportMin = exportMin;
        return this;
    }

    public boolean isExportMax() {
        return exportMax;
    }

    public MeasurementRef setExportMax(boolean exportMax) {
        this.exportMax = exportMax;
        return this;
    }

    public boolean isExportSum() {
        return exportSum;
    }

    public MeasurementRef setExportSum(boolean exportSum) {
        this.exportSum = exportSum;
        return this;
    }

    public boolean isExportStd() {
        return exportStd;
    }

    public MeasurementRef setExportStd(boolean exportStd) {
        this.exportStd = exportStd;
        return this;
    }

    public String getImageObjName() {
        return imageObjName;
    }

    public MeasurementRef setImageObjName(String imageObjName) {
        this.imageObjName = imageObjName;
        return this;

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public String toString() {
        return "Measurement reference ("+name+")";
    }
}