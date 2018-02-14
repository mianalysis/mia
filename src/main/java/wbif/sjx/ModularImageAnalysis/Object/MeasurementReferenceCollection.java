package wbif.sjx.ModularImageAnalysis.Object;

import java.util.LinkedHashSet;

public class MeasurementReferenceCollection extends LinkedHashSet<MeasurementReference> {
    public MeasurementReference get(String name) {
        for (MeasurementReference measurementReference:this) {
            if (measurementReference.getName().equals(name)) return measurementReference;
        }

        return null;
    }

    public void updateImageObjectName(String measurementName, String imageObjectName) {
        for (MeasurementReference measurementReference : this) {
            if (measurementReference.getName().equals(measurementName)) {
                measurementReference.setImageObjName(imageObjectName);
            }
        }
    }

    public String[] getMeasurementNickNames() {
        String[] measurementNames = new String[size()];

        int i = 0;
        for (MeasurementReference measurementReference:this) {
            measurementNames[i++] = measurementReference.getNickName();
        }

        return measurementNames;

    }
}
