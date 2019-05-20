package wbif.sjx.MIA.Object.References;

import wbif.sjx.MIA.Object.References.Abstract.RefCollection;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetadataRefCollection extends RefCollection<MetadataRef> {
    public MetadataRefCollection() {

    }

    public String[] getMetadataNames() {
        return keySet().toArray(new String[0]);
    }

    public MetadataRef getOrPut(Object key) {
        putIfAbsent((String) key, new MetadataRef((String) key));
        return super.get(key);
    }

    public void add(MetadataRef metadataRef) {
        put(metadataRef.getName(), metadataRef);

    }
}