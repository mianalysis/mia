package io.github.mianalysis.mia.object.measurements;

import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.coordinates.Obj;

public class ChildCountMeasurement extends Measurement {
    private Obj obj;
    private String childName;

    public ChildCountMeasurement(String name, Obj obj, String childName) {
        super(name);
        this.obj = obj;
        this.childName = childName;
    }
    
    public double getValue() {
        Objs children = obj.getChildren(childName);

        if (children == null)
            return 0;

        return children.size();

    }
}
