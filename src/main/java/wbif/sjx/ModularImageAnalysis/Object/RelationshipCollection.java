package wbif.sjx.ModularImageAnalysis.Object;

import javax.annotation.Nullable;
import java.util.*;

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

    public TreeSet<String> getParentNames(String childName, boolean useHierarchy, @Nullable String rootName) {
        if (rootName == null) rootName = "";

        // Adding each parent and then the parent of that
        TreeSet<String> parentNames = new TreeSet<>(parents.get(childName));
        for (String parentName:parentNames) {
            parentName = rootName + parentName;
        }

        if (!useHierarchy) return parentNames;

        // Takes the parentHierarchy HashSet and adds all parents of each object.  As we're using a HashSet we won't get
        // duplicates.  Therefore, this loop terminates when no more objects are added.

        for (String parentName:parentNames) {
            TreeSet<String> currentParentNames = getParentNames(parentName,true,rootName+" // "+parentName);
            parentNames.addAll(currentParentNames);
        }

        return parentNames;

    }
}
