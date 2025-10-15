package io.github.mianalysis.mia.object.refs.collections;

import java.util.TreeMap;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.refs.ObjMetadataRef;
import io.github.mianalysis.mia.object.units.SpatialUnit;
import io.github.mianalysis.mia.object.units.TemporalUnit;

public class ObjMetadataRefs extends TreeMap<String, ObjMetadataRef>
        implements Refs<ObjMetadataRef> {
    /**
     *
     */
    private static final long serialVersionUID = 225316245096553320L;

    public void updateImageObjectName(String metadataName, String objectsName) {
        get(metadataName).setObjectsName(objectsName);
    }

    public String[] getMetadataNames() {
        return keySet().toArray(new String[0]);
    }

    public ObjMetadataRef getOrPut(String key) {
        // Stripping placeholders for units
        key = SpatialUnit.replace(key);
        key = TemporalUnit.replace(key);

        putIfAbsent((String) key, new ObjMetadataRef((String) key));

        return get(key);

    }

    public boolean hasExportedMetadata() {
        return size() >= 1;

    }

    public boolean add(ObjMetadataRef ref) {
        put(ref.getName(), ref);
        return true;
    }

    public void addBlankMetadata(ObjI obj) {
        MIA.log.writeError("To implement - ObjMetadataRefs");
        // for (ObjMetadataRef ref : values())
        //     obj.addMeasurement(new Measurement(ref.getName(), Double.NaN));
    }
}
