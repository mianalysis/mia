package wbif.sjx.MIA.Object.References;

import wbif.sjx.MIA.Object.References.Abstract.ObjectCountRef;
import wbif.sjx.MIA.Object.References.Abstract.RefCollection;

public class MeasurementRefCollection extends RefCollection<MeasurementRef> {
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

    public MeasurementRef getOrPut(Object key, MeasurementRef.Type type) {
        switch (type) {
            case OBJECT:
            case IMAGE:
                putIfAbsent((String) key,new MeasurementRef((String) key,type));
                return super.get(key);
            default:
                return null;
        }
    }

    public boolean hasExportedMeasurements() {
        for (MeasurementRef ref:values()) {
            if (ref.isAvailable()) return true;
        }

        return false;

    }
}
