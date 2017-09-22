package wbif.sjx.ModularImageAnalysis.Object;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;

/**
 * Extension of a LinkedHashMap, which contains parents (keys) and their children (values).  As there can be multiple
 * different types of children these are stored in an ArrayList.
 */
public class RelationshipCollection {
    private LinkedHashMap<String,ArrayList<String>> parents = new LinkedHashMap<>();
    private LinkedHashMap<String,ArrayList<String>> children = new LinkedHashMap<>();

    public void addRelationship(String parent, String child) {
        parents.computeIfAbsent(child,k -> new ArrayList<>());
        parents.get(child).add(parent);

        children.computeIfAbsent(parent, k -> new ArrayList<>());
        children.get(parent).add(child);

    }

    public String[] getChildNames(String parentName) {
        if (children.get(parentName) == null) {
            return new String[]{""};
        }

        return children.get(parentName).toArray(new String[children.get(parentName).size()]);

    }

    public String[] getParentNames(String childName) {
        if (parents.get(childName) == null) {
            return new String[]{""};
        }

        // Adding each parent and then the parent of that
        HashSet<String> parentHierarchy = new HashSet<>(parents.get(childName));

        // Takes the parentHierarchy HashSet and adds all parents of each object.  As we're using a HashSet we won't get
        // duplicates.  Therefore, this loop terminates when no more objects are added.
        int lastNParents = 0;
        while (parentHierarchy.size() != lastNParents) {
            lastNParents = parentHierarchy.size();

            ArrayList<String> newParents = new ArrayList<>();
            for (String parent:parentHierarchy) {
                if (parents.get(parent) == null) continue;
                newParents.addAll(parents.get(parent));

            }

            parentHierarchy.addAll(newParents);
        }

        return parentHierarchy.toArray(new String[parentHierarchy.size()]);

    }
}
