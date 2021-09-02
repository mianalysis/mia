package io.github.mianalysis.mia.object.measurements;

import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Obj;

public class ParentIDMeasurement extends Measurement {
    private Obj obj;
    private String parentName;

    public ParentIDMeasurement(String name, Obj obj, String parentName) {
        super(name);
        this.obj = obj;
        this.parentName = parentName;
    }
    
    public double getValue() {
        Obj parentObj = obj.getParent(parentName);

        if (parentObj == null)
            return Double.NaN;

        return parentObj.getID();

    }
}
