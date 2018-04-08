package wbif.sjx.ModularImageAnalysis.Object;

/**
 * Created by sc13967 on 01/12/2017.
 */
public class MeasurementReference extends Reference {
    private boolean calculated = true;
    private boolean exportable = true;
    private String imageObjName = "";


    public MeasurementReference(String name) {
        super(name);
    }

    public MeasurementReference(String name, String imageObjName) {
        super(name);
        this.imageObjName = imageObjName;
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

    public String getImageObjName() {
        return imageObjName;
    }

    public void setImageObjName(String imageObjName) {
        this.imageObjName = imageObjName;
    }

    @Override
    public String toString() {
        return "Measurement reference ("+name+")";
    }
}