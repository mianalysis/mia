package wbif.sjx.ModularImageAnalysis.Object;


import javax.annotation.Nullable;
import java.util.*;

/**
 * Extension of a LinkedHashMap, which contains parents (keys) and their children (values).  As there can be multiple
 * different types of children these are stored in an ArrayList.
 */
public class RelationshipCollection {
    private LinkedHashMap<String,TreeSet<String>> parents = new LinkedHashMap<>();
    private LinkedHashMap<String,TreeSet<String>> children = new LinkedHashMap<>();

    public void addRelationship(String parent, String child) {
        parents.computeIfAbsent(child,k -> new TreeSet<>());
        parents.get(child).add(parent);

        children.computeIfAbsent(parent, k -> new TreeSet<>());
        children.get(parent).add(child);

    }

    private TreeSet<String> getChildNames(String parentName, boolean useHierarchy, @Nullable String rootName) {
        if (rootName == null) rootName = "";

        // Adding each parent and then the parent of that
        TreeSet<String> childNames = children.get(parentName);
        if (childNames == null) return new TreeSet<>();

        // Appending root name
        TreeSet<String> newChildNames = new TreeSet<>();
        for (String childName:childNames) newChildNames.add(rootName+childName);

        if (!useHierarchy) return newChildNames;

        // Adding parent names from parents
        for (String childName:childNames) {
            TreeSet<String> currentParentNames = getChildNames(childName,true,rootName+childName+" // ");
            newChildNames.addAll(currentParentNames);
        }

        return newChildNames;

    }

    public String[] getChildNames(String parentName, boolean useHierarchy) {
        TreeSet<String> childNames = getChildNames(parentName,useHierarchy,"");

        return childNames.toArray(new String[0]);

    }

    private TreeSet<String> getParentNames(String childName, boolean useHierarchy, @Nullable String rootName) {
        if (rootName == null) rootName = "";

        // Adding each parent and then the parent of that
        TreeSet<String> parentNames = parents.get(childName);
        if (parentNames == null) return new TreeSet<>();

        // Appending root name
        TreeSet<String> newParentNames = new TreeSet<>();
        for (String parentName:parentNames) newParentNames.add(rootName+parentName);

        if (!useHierarchy) return newParentNames;

        // Adding parent names from parents
        for (String parentName:parentNames) {
            TreeSet<String> currentParentNames = getParentNames(parentName,true,rootName+parentName+" // ");
            newParentNames.addAll(currentParentNames);
        }

        return newParentNames;

    }

    public String[] getParentNames(String childName, boolean useHierarchy) {
        TreeSet<String> parentNames = getParentNames(childName,useHierarchy,"");

        return parentNames.toArray(new String[0]);

    }
}
