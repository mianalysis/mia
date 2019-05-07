package wbif.sjx.MIA.Object.References;

/**
 * Created by sc13967 on 01/12/2017.
 */
public class MeasurementRef extends Reference implements ExportableSummary {
    private String imageObjName = "";
    private String description = "";
    private String nickname = "";

    private boolean calculated = true;
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

    public MeasurementRef duplicate() {
        MeasurementRef newRef = new MeasurementRef(name);

        newRef.setCalculated(calculated);
        newRef.setImageObjName(imageObjName);
        newRef.setDescription(description);
        newRef.setNickname(nickname);
        newRef.setExportGlobal(exportGlobal);
        newRef.setExportIndividual(isExportIndividual());
        newRef.setExportMean(isExportMean());
        newRef.setExportMin(isExportMin());
        newRef.setExportMax(isExportMax());
        newRef.setExportSum(isExportSum());
        newRef.setExportStd(isExportStd());

        return newRef;

    }

    @Override
    public String toString() {
        return "Measurement reference ("+name+")";
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

    public void setExportIndividual(boolean exportIndividual) {
        this.exportIndividual = exportIndividual;

    }

    public boolean isExportMean() {
        return exportMean;
    }

    public void setExportMean(boolean exportMean) {
        this.exportMean = exportMean;

    }

    public boolean isExportMin() {
        return exportMin;
    }

    public void setExportMin(boolean exportMin) {
        this.exportMin = exportMin;

    }

    public boolean isExportMax() {
        return exportMax;
    }

    public void setExportMax(boolean exportMax) {
        this.exportMax = exportMax;

    }

    public boolean isExportSum() {
        return exportSum;
    }

    public void setExportSum(boolean exportSum) {
        this.exportSum = exportSum;

    }

    public boolean isExportStd() {
        return exportStd;
    }

    public void setExportStd(boolean exportStd) {
        this.exportStd = exportStd;

    }
}