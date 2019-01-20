package wbif.sjx.ModularImageAnalysis.Object;

import wbif.sjx.common.Object.HCMetadata;

import java.util.TreeMap;

public class MetadataRefCollection extends TreeMap<String,MetadataReference> {
    public MetadataRefCollection() {
        add(new MetadataReference(HCMetadata.FILE));
        add(new MetadataReference(HCMetadata.FILENAME));
        add(new MetadataReference(HCMetadata.SERIES_NUMBER));
        add(new MetadataReference(HCMetadata.SERIES_NAME));

    }

    public String[] getMetadataNames() {
        return keySet().toArray(new String[0]);
    }

    public void add(MetadataReference metadataReference) {
        put(metadataReference.getName(),metadataReference);

    }
}