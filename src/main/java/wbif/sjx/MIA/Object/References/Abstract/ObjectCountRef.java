package wbif.sjx.MIA.Object.References.Abstract;

import wbif.sjx.MIA.Object.References.MeasurementRef;

public class ObjectCountRef extends MeasurementRef {
    public ObjectCountRef(String name, Type type) {
        super(name, type);
    }

    public ObjectCountRef(String name, Type type, String imageObjName) {
        super(name, type, imageObjName);
    }
}
