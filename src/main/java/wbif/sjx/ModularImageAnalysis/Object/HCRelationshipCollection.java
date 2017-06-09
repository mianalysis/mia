package wbif.sjx.ModularImageAnalysis.Object;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Extension of a LinkedHashMap, which contains parents (keys) and their children (values).  As there can be multiple
 * different types of children these are stored in an ArrayList.
 */
public class HCRelationshipCollection extends LinkedHashMap<HCName,ArrayList<HCName>> {

    public void addRelationship(HCName parent, HCName child) {
        computeIfAbsent(parent,k -> new ArrayList<>());
        get(parent).add(child);
    }

    public HCName[] getChildNames(HCName parentName) {
        if (get(parentName) == null) {
            return null;
        }

        HCName[] childNames = new HCName[get(parentName).size()];
        int iter = 0;
        for (HCName childName:get(parentName)) {
            childNames[iter++] = childName;
        }

        return childNames;

    }
}
