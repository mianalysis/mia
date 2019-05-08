package wbif.sjx.MIA.Object.References.Abstract;

public abstract class ExportableRef extends Ref {
    private boolean exportGlobal = true; // This is mainly for the GUI
    private boolean exportIndividual = true;
    private boolean exportMean = true;
    private boolean exportMin = true;
    private boolean exportMax = true;
    private boolean exportSum = true;
    private boolean exportStd = true;

    private String description = "";
    private String nickname = "";


    public ExportableRef(String name) {
        super(name);
        this.nickname = name;
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

}
