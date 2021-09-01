package io.github.mianalysis.MIA.Object.References.Collections;

import io.github.mianalysis.MIA.Object.Units.SpatialUnit;
import io.github.mianalysis.MIA.Object.Units.TemporalUnit;
import io.github.mianalysis.MIA.Object.Image;
import io.github.mianalysis.MIA.Object.Measurement;
import io.github.mianalysis.MIA.Object.References.ImageMeasurementRef;

import java.util.TreeMap;

public class ImageMeasurementRefCollection extends TreeMap<String, ImageMeasurementRef>
        implements RefCollection<ImageMeasurementRef> {
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
