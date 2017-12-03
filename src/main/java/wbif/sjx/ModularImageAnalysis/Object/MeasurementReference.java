package wbif.sjx.ModularImageAnalysis.Object;

/**
 * Created by sc13967 on 01/12/2017.
 */
public class MeasurementReference {
    private String measurementName;
    private boolean exportable = true;

    public MeasurementReference(String measurementName) {
        this.measurementName = measurementName;
    }

    public String getMeasurementName() {
        return measurementName;
    }

    public boolean isExportable() {
        return exportable;
    }

    public void setExportable(boolean exportable) {
        this.exportable = exportable;
    }
}
