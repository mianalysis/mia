package wbif.sjx.ModularImageAnalysis.Object;

/**
 * Created by sc13967 on 01/12/2017.
 */
public class MeasurementReference extends Reference {
    private boolean calculated = true;
    private boolean exportable = true;
    private String imageObjName = "";
    private String description = "";
    private String nickname = "";


    public MeasurementReference(String name) {
        super(name);
        this.nickname = name;
    }

    public MeasurementReference(String name, String imageObjName) {
        super(name);
        this.imageObjName = imageObjName;
        this.nickname = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean isCalculated() {
        return calculated;
    }

    public MeasurementReference setCalculated(boolean calculated) {
        this.calculated = calculated;
        return this;
    }

    public boolean isExportable() {
        return exportable;
    }

    public MeasurementReference setExportable(boolean exportable) {
        this.exportable = exportable;
        return this;
    }

    public String getImageObjName() {
        return imageObjName;
    }

    public MeasurementReference setImageObjName(String imageObjName) {
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