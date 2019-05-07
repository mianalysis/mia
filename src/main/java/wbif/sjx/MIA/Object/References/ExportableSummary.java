package wbif.sjx.MIA.Object.References;

public interface ExportableSummary {
    public boolean isExportIndividual();
    public void setExportIndividual(boolean exportIndividual);

    public boolean isExportMean();
    public void setExportMean(boolean exportMean);

    public boolean isExportMin();
    public void setExportMin(boolean exportMin);

    public boolean isExportMax();
    public void setExportMax(boolean exportMax);

    public boolean isExportSum();
    public void setExportSum(boolean exportSum);

    public boolean isExportStd();
    public void setExportStd(boolean exportStd);

}
