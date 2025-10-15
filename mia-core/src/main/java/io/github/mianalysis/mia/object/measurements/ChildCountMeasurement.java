package io.github.mianalysis.mia.object.measurements;

import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.coordinates.ObjI;

public class ChildCountMeasurement extends Measurement {
    private ObjI obj;
    private String childName;

    public ChildCountMeasurement(String name, ObjI obj, String childName) {
        super(name);
        this.obj = obj;
        this.childName = childName;
    }
    
    public double getValue() {
        ObjsI children = obj.getChildren(childName);

        if (children == null)
            return 0;

        return children.size();

    }
}
