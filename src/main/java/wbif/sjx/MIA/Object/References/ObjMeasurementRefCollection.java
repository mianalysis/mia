package wbif.sjx.MIA.Object.References;

import wbif.sjx.MIA.Object.References.Abstract.RefCollection;

import java.util.TreeMap;

public class ObjMeasurementRefCollection extends TreeMap<String,ObjMeasurementRef> implements RefCollection<ObjMeasurementRef> {
    public void updateImageObjectName(String measurementName, String objectsName) {
        get(measurementName).setObjectsName(objectsName);
    }

    public String[] getMeasurementNames() {
        return keySet().toArray(new String[0]);
    }

    public ObjMeasurementRef getOrPut(Object key) {
        putIfAbsent((String) key,new ObjMeasurementRef((String) key));
        return get(key);
    }

    public boolean hasExportedMeasurements() {
        return size() >= 1;

    }

    public boolean add(ObjMeasurementRef ref) {
        put(ref.getName(),ref);
        return true;
    }
}
