package wbif.sjx.MIA.Object.Measurements;

import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;

public class ChildCountMeasurement extends Measurement {
    private Obj obj;
    private String childName;

    public ChildCountMeasurement(String name, Obj obj, String childName) {
        super(name);
        this.obj = obj;
        this.childName = childName;
    }
    
    public double getValue() {
        ObjCollection children = obj.getChildren(childName);

        if (children == null)
            return 0;

        return children.size();

    }
}
