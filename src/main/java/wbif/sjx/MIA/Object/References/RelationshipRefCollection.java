package wbif.sjx.MIA.Object.References;


import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.TreeSet;

/**
 * Extension of a LinkedHashMap, which contains parents (keys) and their children (values).  As there can be multiple
 * different types of children these are stored in an ArrayList.
 */
public class RelationshipRefCollection extends LinkedHashSet<RelationshipRef> {
//    private LinkedHashMap<String,TreeSet<String>> parents = new LinkedHashMap<>();
//    private LinkedHashMap<String,TreeSet<String>> children = new LinkedHashMap<>();

    public void addRelationship(String parent, String child) {
        RelationshipRef relationshipRef = new RelationshipRef(parent,child);
        add(relationshipRef);

//        parents.computeIfAbsent(child,k -> new TreeSet<>());
//        parents.get(child).add(parent);
//
//        children.computeIfAbsent(parent, k -> new TreeSet<>());
//        children.get(parent).add(child);

    }

    private TreeSet<String> getChildNames(String parentName, boolean useHierarchy, @Nullable String rootName) {
        if (rootName == null) rootName = "";

        // Adding each child and then the child of that
        TreeSet<String> childNames = getChildNames(this,parentName);
        if (childNames.size() == 0) return childNames;

//        // Adding each parent and then the parent of that
//        TreeSet<String> childNames = children.get(parentName);
//        if (childNames == null) return new TreeSet<>();

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
        TreeSet<String> parentNames = getParentNames(this,childName);
        if (parentNames.size() == 0) return parentNames;

        // Appending root name
        TreeSet<String> newParentNames = new TreeSet<>();
        for (String parentName:parentNames) newParentNames.add(rootName+parentName);

        if (!useHierarchy) return newParentNames;

        // Adding parent names from parents
        for (String parentName:parentNames) {
            // Avoid stack overflow if the parent and child have accidentally been called the same thing
            if (parentName.equals(childName)) continue;

            TreeSet<String> currentParentNames = getParentNames(parentName,true,rootName+parentName+" // ");
            newParentNames.addAll(currentParentNames);
        }

        return newParentNames;

    }

    public String[] getParentNames(String childName, boolean useHierarchy) {
        TreeSet<String> parentNames = getParentNames(childName,useHierarchy,"");

        return parentNames.toArray(new String[0]);

    }

    private static TreeSet<String> getChildNames(RelationshipRefCollection relationships, String parentName) {
        TreeSet<String> childNames = new TreeSet<>();

        for (RelationshipRef relationshipRef :relationships) {
            if (relationshipRef.getParentName().equals(parentName)) childNames.add(relationshipRef.getChildName());
        }

        return childNames;

    }

    private static TreeSet<String> getParentNames(RelationshipRefCollection relationships, String childName) {
        TreeSet<String> parentNames = new TreeSet<>();

        for (RelationshipRef relationshipRef :relationships) {
            if (relationshipRef.getChildName().equals(childName)) parentNames.add(relationshipRef.getParentName());
        }

        return parentNames;

    }
}
