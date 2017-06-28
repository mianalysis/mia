package wbif.sjx.ModularImageAnalysis.Object;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;

/**
 * Extension of a LinkedHashMap, which contains parents (keys) and their children (values).  As there can be multiple
 * different types of children these are stored in an ArrayList.
 */
public class HCRelationshipCollection extends LinkedHashMap<String,ArrayList<String>> {

    public void addRelationship(String parent, String child) {
        computeIfAbsent(parent,k -> new ArrayList<>());
        get(parent).add(child);
    }

    public String[] getChildNames(String parentName) {
        if (get(parentName) == null) {
            return null;
        }

        String[] childNames = new String[get(parentName).size()];
        int iter = 0;
        for (String childName:get(parentName)) {
            childNames[iter++] = childName;
        }

        return childNames;

    }

    public String[] getParentNames(String childName) {
        // Running through the children associated to parent names.  If the target child is present, the parent is added
        // to a HashSet
        HashSet<String> parentSet = new HashSet<>();
        for (String parentName:keySet()) {
            for (String currChildName:get(parentName)) {
                if (currChildName.equals(childName)) {
                    parentSet.add(parentName);
                }
            }
        }

        String[] parentNames = new String[parentSet.size()];
        int iter = 0;
        for (String parentName : parentSet) {
            parentNames[iter++] = parentName;
        }

        return parentNames;

    }
}
