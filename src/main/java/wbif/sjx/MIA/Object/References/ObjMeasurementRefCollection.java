package wbif.sjx.MIA.Object.References;

import wbif.sjx.MIA.Object.References.Abstract.MeasurementRef;
import wbif.sjx.MIA.Object.References.Abstract.RefCollection;

public class ObjMeasurementRefCollection extends RefCollection<ObjMeasurementRef> {
    public void updateImageObjectName(String measurementName, String imageObjectName) {
        get(measurementName).setImageObjName(imageObjectName);
    }

    public String[] getMeasurementNames() {
        return keySet().toArray(new String[0]);
    }

    public void setAllAvailable(boolean available) {
        for (MeasurementRef measurementReference:values()) {
            measurementReference.setAvailable(available);
        }
    }

    public MeasurementRef getOrPut(Object key) {
        putIfAbsent((String) key,new ObjMeasurementRef((String) key));
        return get(key);
    }

    public boolean hasExportedMeasurements() {
        for (MeasurementRef ref:values()) {
            if (ref.isAvailable()) return true;
        }

        return false;

    }
}
