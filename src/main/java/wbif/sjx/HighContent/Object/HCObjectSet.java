package wbif.sjx.HighContent.Object;

import java.util.LinkedHashMap;

/**
 * Created by sc13967 on 12/05/2017.
 */
public class HCObjectSet extends LinkedHashMap<Integer,HCObject> {
    HCName name;

    public HCObjectSet(HCName name) {
        this.name = name;
    }

    public HCName getName() {
        return name;
    }

    public void removeWithRelations(int key) {


    }
}
