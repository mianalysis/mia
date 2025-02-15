package io.github.mianalysis.mia.object;

import java.util.LinkedHashMap;

import io.github.mianalysis.mia.object.coordinates.volume.VolumeI;
import io.github.mianalysis.mia.object.measurements.MeasurementProvider;

public interface ObjI extends MeasurementProvider, VolumeI {
    public default LinkedHashMap<String, ObjI> getParents(boolean useFullHierarchy) {
        if (!useFullHierarchy)
            return getParents();

        // Adding each parent and then the parent of that
        LinkedHashMap<String, ObjI> parentHierarchy = new LinkedHashMap<>(getParents());

        // Going through each parent, adding the parents of that.
        for (ObjI parent : getParents().values()) {
            if (parent == null)
                continue;

            LinkedHashMap<String, ObjI> currentParents = parent.getParents(true);
            if (currentParents == null)
                continue;

            parentHierarchy.putAll(currentParents);

        }

        return parentHierarchy;

    }

    public default ObjI getParent(String name) {
        // Split name down by " // " tokenizer
        String[] elements = name.split(" // ");

        // Getting the first parent
        ObjI parent = getParents().get(elements[0]);

        // If the first parent was the only one listed, returning this
        if (elements.length == 1)
            return parent;

        // If there are additional parents listed, re-constructing the string and
        // running this method on the parent
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i < elements.length; i++) {
            stringBuilder.append(elements[i]);
            if (i != elements.length - 1)
                stringBuilder.append(" // ");
        }

        if (parent == null)
            return null;

        return parent.getParent(stringBuilder.toString());

    }

    public default void addParent(ObjI parent) {
        getParents().put(parent.getName(), parent);
    }

    public default void addParent(String name, Obj parent) {
        getParents().put(name, parent);
    }

    public default void removeParent(String name) {
        getParents().remove(name);
    }

    public default void removeParent(Obj parent) {
        getParents().remove(parent.getName());
    }

    public void addMetadataItem(ObjMetadata metadataItem);
    public ObjMetadata getMetadataItem(String name);
    public void removeMetadataItem(String name);
    public Objs getObjectCollection();
    public void setObjectCollection(Objs objCollection);
    public String getName();
    public int getID();
    public ObjI setID(int ID);
    public int getT();
    public ObjI setT(int t);
    public LinkedHashMap<String, ObjI> getParents();
    public void setParents(LinkedHashMap<String, ObjI> parents);
    public LinkedHashMap<String, Objs> getChildren();

}
