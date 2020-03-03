package wbif.sjx.MIA.Object.References;


import wbif.sjx.MIA.Object.References.Abstract.RefCollection;

import javax.annotation.Nullable;

import java.util.LinkedHashSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Extension of a LinkedHashMap, which contains parents (keys) and their children (values).  As there can be multiple
 * different types of children these are stored in an ArrayList.
 */
public class RelationshipRefCollection extends TreeMap<String, RelationshipRef> implements RefCollection<RelationshipRef> {
    /**
     *
     */
    private static final long serialVersionUID = 6633110280044918217L;

    public RelationshipRef getOrPut(String parent, String child) {
        String key = parent+" // "+child;
        putIfAbsent((String) key,new RelationshipRef(parent,child));
        return (RelationshipRef) super.get(key);

    }

    public boolean add(RelationshipRef ref) {
        put(ref.getParentName()+" // "+ref.getChildName(),ref);
        return true;
    }

    private TreeSet<String> getChildNames(String parentName, boolean useHierarchy, @Nullable String rootName) {
        if (rootName == null) rootName = "";

        // Adding each child and then the child of that
        TreeSet<String> childNames = getChildNames(this,parentName);
        if (childNames.size() == 0) return childNames;
        
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

    public LinkedHashSet<RelationshipRef> getChildren(String parentName, boolean useHierarchy) {
        TreeSet<String> childNames = getChildNames(parentName,useHierarchy,"");

        LinkedHashSet<RelationshipRef> relationshipRefs = new LinkedHashSet<>();
        for (String childName:childNames) {
            relationshipRefs.add(getOrPut(parentName,childName));
        }

        return relationshipRefs;

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

        for (RelationshipRef relationshipRef :relationships.values()) {
            if (relationshipRef.getParentName().equals(parentName)) childNames.add(relationshipRef.getChildName());
        }

        return childNames;

    }

    private static TreeSet<String> getParentNames(RelationshipRefCollection relationships, String childName) {
        TreeSet<String> parentNames = new TreeSet<>();

        for (RelationshipRef relationshipRef :relationships.values()) {
            if (relationshipRef.getChildName().equals(childName)) parentNames.add(relationshipRef.getParentName());
        }

        return parentNames;

    }
}
