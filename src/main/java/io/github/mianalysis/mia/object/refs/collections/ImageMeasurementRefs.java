package io.github.mianalysis.mia.object.refs.collections;

import io.github.mianalysis.mia.object.units.SpatialUnit;
import io.github.mianalysis.mia.object.units.TemporalUnit;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.refs.ImageMeasurementRef;

import java.util.TreeMap;

public class ImageMeasurementRefs extends TreeMap<String, ImageMeasurementRef>
        implements Refs<ImageMeasurementRef> {
    /**
     *
     */
    private static final long serialVersionUID = -6103343888627280588L;

    public void updateImageObjectName(String measurementName, String imageObjectName) {
        get(measurementName).setImageName(imageObjectName);
    }

    public String[] getMeasurementNames() {
        return keySet().toArray(new String[0]);
    }

    public ImageMeasurementRef getOrPut(String key) {
        // Stripping placeholders for units
        key = SpatialUnit.replace(key);
        key = TemporalUnit.replace(key);

        putIfAbsent((String) key, new ImageMeasurementRef((String) key));

        return get(key);

    }

    public boolean hasExportedMeasurements() {
        return size() >= 1;

    }

    public boolean add(ImageMeasurementRef ref) {
        put(ref.getName(), ref);
        return true;
    }

    public void addBlankMeasurements(Image image) {
        for (ImageMeasurementRef ref : values())
            image.addMeasurement(new Measurement(ref.getName(), Double.NaN));
    }
}
