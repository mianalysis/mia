package wbif.sjx.ModularImageAnalysis.Object;

/**
 * Created by sc13967 on 01/12/2017.
 */
public class MeasurementReference {
    private String measurementName;
    private boolean calculated = true;
    private boolean exportable = true;

    public MeasurementReference(String measurementName) {
        this.measurementName = measurementName;
    }

    public String getMeasurementName() {
        return measurementName;
    }

    public void setMeasurementName(String measurementName) {
        this.measurementName = measurementName;
    }

    public boolean isCalculated() {
        return calculated;
    }

    public void setCalculated(boolean calculated) {
        this.calculated = calculated;
    }

    public boolean isExportable() {
        return exportable;
    }

    public void setExportable(boolean exportable) {
        this.exportable = exportable;
    }
}
