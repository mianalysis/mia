package wbif.sjx.ModularImageAnalysis.Object;

/**
 * Created by sc13967 on 01/12/2017.
 */
public class MeasurementReference extends Reference {
    private boolean calculated = true;
    private boolean exportable = true;
    private String imageObjName = "";
    private String nickName;


    public MeasurementReference(String name) {
        super(name);

        this.nickName = name;

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

    public void setNickName(String nickName) {
        this.nickName = nickName;

    }

    public String getNickName() {
        return nickName;
    }

    @Override
    public String toString() {
        return "Measurement reference ("+name+")";
    }
}