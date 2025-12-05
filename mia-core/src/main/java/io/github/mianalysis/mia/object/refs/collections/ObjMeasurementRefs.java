package io.github.mianalysis.mia.object.refs.collections;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.units.SpatialUnit;
import io.github.mianalysis.mia.object.units.TemporalUnit;

public class ObjMeasurementRefs extends HashSet<ObjMeasurementRef> implements Refs<ObjMeasurementRef> {

    public String[] getMeasurementNames() {
        return stream().map((v) -> v.getName()).sorted().toArray(size -> new String[size]);
    }

    public ObjMeasurementRef getOrPut(String measurementName) {
        // Stripping placeholders for units
        measurementName = SpatialUnit.replace(measurementName);
        measurementName = TemporalUnit.replace(measurementName);

        String finalMeasurementName = measurementName;
        Optional<ObjMeasurementRef> matches = stream().filter((v) -> v.getName().equals(finalMeasurementName)).findFirst();
        if (matches.isPresent())
            return matches.get();

        ObjMeasurementRef ref = new ObjMeasurementRef((String) measurementName);
        add(ref);
        
        return ref;

    }

    public boolean hasExportedMeasurements() {
        return size() >= 1;
    }

    public boolean containsMeasurement(String measurementName) {
        return stream().anyMatch(v -> v.getName().endsWith(measurementName));
    }

    public void addBlankMeasurements(Obj obj) {
        for (ObjMeasurementRef ref : values())
            obj.addMeasurement(new Measurement(ref.getName(), Double.NaN));
    }

    @Override
    public Collection<ObjMeasurementRef> values() {
        return this;
    }
}
