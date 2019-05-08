package wbif.sjx.MIA.Object.References;

import wbif.sjx.MIA.Object.References.Abstract.ExportableRef;

/**
 * Created by sc13967 on 01/12/2017.
 */
public class MeasurementRef extends ExportableRef {
    private String imageObjName = "";


    public MeasurementRef(String name) {
        super(name);
    }

    public MeasurementRef(String name, String imageObjName) {
        super(name);
        this.imageObjName = imageObjName;
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

    public MeasurementRef duplicate() {
        MeasurementRef newRef = new MeasurementRef(name);

        newRef.setAvailable(isAvailable());
        newRef.setImageObjName(imageObjName);
        newRef.setDescription(getDescription());
        newRef.setNickname(getNickname());
        newRef.setExportGlobal(isExportGlobal());
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

}