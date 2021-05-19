package wbif.sjx.MIA.Object.References.Collections;


import org.eclipse.sisu.Nullable;

import wbif.sjx.MIA.Object.References.ParentChildRef;

import java.util.LinkedHashSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Extension of a TreeMap, which contains parents (keys) and their children (values).  As there can be multiple
 * different types of children these are stored in an ArrayList.
 */
public class ParentChildRefCollection extends TreeMap<String, ParentChildRef> implements RefCollection<ParentChildRef> {
    /**
     *
     */
    private static final long serialVersionUID = 6633110280044918217L;

    public ParentChildRef getOrPut(String parent, String child) {
        String key = parent+" // "+child;
        putIfAbsent((String) key,new ParentChildRef(parent,child));
        return (ParentChildRef) super.get(key);

    }

    public boolean add(ParentChildRef ref) {
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
        for (String childName : childNames) {
            if (childName.equals(parentName))
                return newChildNames;

            TreeSet<String> currentParentNames = getChildNames(childName,true,rootName+childName+" // ");
            newChildNames.addAll(currentParentNames);
        }

        return newChildNames;

    }

    public String[] getChildNames(String parentName, boolean useHierarchy) {
        TreeSet<String> childNames = getChildNames(parentName,useHierarchy,"");

        return childNames.toArray(new String[0]);

    }

    public LinkedHashSet<ParentChildRef> getChildren(String parentName, boolean useHierarchy) {
        TreeSet<String> childNames = getChildNames(parentName,useHierarchy,"");

        LinkedHashSet<ParentChildRef> ParentChildRefs = new LinkedHashSet<>();
        for (String childName:childNames) {
            ParentChildRefs.add(getOrPut(parentName,childName));
        }

        return ParentChildRefs;

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

    private static TreeSet<String> getChildNames(ParentChildRefCollection relationships, String parentName) {
        TreeSet<String> childNames = new TreeSet<>();

        for (ParentChildRef ParentChildRef :relationships.values()) {
            if (ParentChildRef.getParentName().equals(parentName)) childNames.add(ParentChildRef.getChildName());
        }

        return childNames;

    }

    private static TreeSet<String> getParentNames(ParentChildRefCollection relationships, String childName) {
        TreeSet<String> parentNames = new TreeSet<>();

        for (ParentChildRef ParentChildRef :relationships.values()) {
            if (ParentChildRef.getChildName().equals(childName)) parentNames.add(ParentChildRef.getParentName());
        }

        return parentNames;

    }
}
