package io.github.mianalysis.mia.object.measurements;

import io.github.mianalysis.mia.object.coordinates.ObjI;

public class ParentIDMeasurement extends Measurement {
    private ObjI obj;
    private String parentName;

    public ParentIDMeasurement(String name, ObjI obj, String parentName) {
        super(name);
        this.obj = obj;
        this.parentName = parentName;
    }
    
    public double getValue() {
        ObjI parentObj = obj.getParent(parentName);

        if (parentObj == null)
            return Double.NaN;

        return parentObj.getID();

    }
}
