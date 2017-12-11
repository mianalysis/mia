package wbif.sjx.ModularImageAnalysis.Object;

import java.util.LinkedHashMap;

public class MeasurementReferenceCollection extends LinkedHashMap<String,MeasurementReference> {
    public void add(String name, MeasurementReference measurementReference) {
        put(name,measurementReference);

    }

    public void add(MeasurementReference measurementReference) {
        put(measurementReference.getName(),measurementReference);

    }

    public void updateImageObjectName(String measurementName, String imageObjectName) {
        get(measurementName).setImageObjName(imageObjectName);

    }

    public String[] getMeasurementNickNames() {
        String[] measurementNames = new String[size()];

        int i = 0;
        for (MeasurementReference measurementReference:this.values()) {
            measurementNames[i++] = measurementReference.getNickName();
        }

        return measurementNames;

    }
}
