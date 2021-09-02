package io.github.mianalysis.MIA.Object.Measurements;

import io.github.mianalysis.MIA.Object.Measurement;
import io.github.mianalysis.MIA.Object.Obj;
import io.github.mianalysis.MIA.Object.Objs;

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