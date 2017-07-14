package wbif.sjx.ModularImageAnalysis.Object;

import java.util.LinkedHashMap;

/**
 * Created by sc13967 on 12/05/2017.
 */
public class ObjSet extends LinkedHashMap<Integer,Obj> {
    private String name;
    private int maxID = 0;

    public ObjSet(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void add(Obj object) {
        put(object.getID(),object);

    }

    public int getNextID() {
        maxID++;
        return maxID;
    }
}
