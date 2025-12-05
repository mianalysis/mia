package io.github.mianalysis.mia.object.refs.collections;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.ObjMetadata;
import io.github.mianalysis.mia.object.refs.ObjMetadataRef;
import io.github.mianalysis.mia.object.units.SpatialUnit;
import io.github.mianalysis.mia.object.units.TemporalUnit;

public class ObjMetadataRefs extends HashSet<ObjMetadataRef> implements Refs<ObjMetadataRef> {
    public String[] getMetadataNames() {
        return stream().map((v) -> v.getName()).sorted().toArray(size -> new String[size]);
    }

    public ObjMetadataRef getOrPut(String metadataName) {
        // Stripping placeholders for units
        metadataName = SpatialUnit.replace(metadataName);
        metadataName = TemporalUnit.replace(metadataName);

        String finalMetadataName = metadataName;

        Optional<ObjMetadataRef> matches = stream().filter((v) -> v.getName().equals(finalMetadataName)).findFirst();
        if (matches.isPresent())
            return matches.get();

        ObjMetadataRef ref = new ObjMetadataRef((String) metadataName);
        add(ref);

        return ref;

    }

    public boolean hasExportedMetadata() {
        return size() >= 1;
    }

    public boolean containsMetadata(String metadataName) {
        return stream().anyMatch(v -> v.getName().endsWith(metadataName));
    }

    public void addBlankMetadata(Obj obj) {
        for (ObjMetadataRef ref : values())
            obj.addMetadataItem(new ObjMetadata(ref.getName(), ""));
    }

    @Override
    public Collection<ObjMetadataRef> values() {
        return this;
    }
}
