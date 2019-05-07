package wbif.sjx.MIA.Object.References;

import wbif.sjx.common.Object.HCMetadata;

import java.util.TreeMap;

public class MetadataRefCollection extends TreeMap<String,MetadataRef> {
    public MetadataRefCollection() {
        add(new MetadataRef(HCMetadata.FILE));
        add(new MetadataRef(HCMetadata.FILENAME));
        add(new MetadataRef(HCMetadata.SERIES_NUMBER));
        add(new MetadataRef(HCMetadata.SERIES_NAME));

    }

    public String[] getMetadataNames() {
        return keySet().toArray(new String[0]);
    }

    public void add(MetadataRef metadataRef) {
        put(metadataRef.getName(), metadataRef);

    }
}