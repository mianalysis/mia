package wbif.sjx.MIA.Object.References;

import wbif.sjx.MIA.Object.References.Abstract.RefCollection;

public class MetadataRefCollection extends RefCollection<MetadataRef> {
    public MetadataRefCollection() {

    }

    public String[] getMetadataNames() {
        return keySet().toArray(new String[0]);
    }

    public void setAllAvailable(boolean available) {
        for (MetadataRef ref:values()) {
            ref.setAvailable(available);
        }
    }

    public MetadataRef getOrPut(Object key) {
        putIfAbsent((String) key,new MetadataRef((String) key));
        return super.get(key);
    }

    public void add(MetadataRef metadataRef) {
        put(metadataRef.getName(), metadataRef);

    }
}