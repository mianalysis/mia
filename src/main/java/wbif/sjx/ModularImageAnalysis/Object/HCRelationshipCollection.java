package wbif.sjx.ModularImageAnalysis.Object;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

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

    public HCName[] getParentNames(HCName childName) {
        // Running through the children associated to parent names.  If the target child is present, the parent is added
        // to a HashSet
        HashSet<HCName> parentSet = new HashSet<>();
        for (HCName parentName:keySet()) {
            for (HCName currChildName:get(parentName)) {
                if (currChildName == childName) {
                    parentSet.add(parentName);
                }
            }
        }

        HCName[] parentNames = new HCName[parentSet.size()];
        int iter = 0;
        for (HCName parentName : parentSet) {
            parentNames[iter++] = parentName;
        }

        return parentNames;

    }
}
