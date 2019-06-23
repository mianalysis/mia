package wbif.sjx.MIA.Object.References;

import wbif.sjx.MIA.Object.References.Abstract.RefCollection;

import java.util.TreeMap;

public class MetadataRefCollection extends TreeMap<String,MetadataRef> implements RefCollection<MetadataRef> {
    public String[] getMetadataNames() {
        return keySet().toArray(new String[0]);
    }

    public MetadataRef getOrPut(Object key) {
        putIfAbsent((String) key, new MetadataRef((String) key));
        return super.get(key);
    }

    public boolean add(MetadataRef metadataRef) {
        put(metadataRef.getName(), metadataRef);
        return true;
    }
}