package wbif.sjx.ModularImageAnalysis.Object;

import java.util.LinkedHashMap;
import java.util.TreeMap;

public class MeasurementReferenceCollection extends TreeMap<String,MeasurementReference> {
    public void updateImageObjectName(String measurementName, String imageObjectName) {
        get(measurementName).setImageObjName(imageObjectName);
    }

    public String[] getMeasurementNames() {
        return keySet().toArray(new String[0]);
    }

    public void setAllCalculated(boolean calculated) {
        for (MeasurementReference measurementReference:values()) {
            measurementReference.setCalculated(calculated);
        }
    }

    public void add(MeasurementReference measurementReference) {
        put(measurementReference.getName(),measurementReference);
    }

    public MeasurementReference getOrPut(Object key) {
        putIfAbsent((String) key,new MeasurementReference((String) key));
        return super.get(key);
    }
}
