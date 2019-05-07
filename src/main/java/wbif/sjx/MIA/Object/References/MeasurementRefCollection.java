package wbif.sjx.MIA.Object.References;

import java.util.TreeMap;

public class MeasurementRefCollection extends TreeMap<String,MeasurementRef> {
    public void updateImageObjectName(String measurementName, String imageObjectName) {
        get(measurementName).setImageObjName(imageObjectName);
    }

    public String[] getMeasurementNames() {
        return keySet().toArray(new String[0]);
    }

    public void setAllCalculated(boolean calculated) {
        for (MeasurementRef measurementReference:values()) {
            measurementReference.setCalculated(calculated);
        }
    }

    public void add(MeasurementRef measurementReference) {
        put(measurementReference.getName(),measurementReference);
    }

    public MeasurementRef getOrPut(Object key) {
        putIfAbsent((String) key,new MeasurementRef((String) key));
        return super.get(key);
    }

    public boolean hasExportedMeasurements() {
        for (MeasurementRef ref:values()) {
            if (ref.isCalculated()) return true;
        }

        return false;

    }
}
