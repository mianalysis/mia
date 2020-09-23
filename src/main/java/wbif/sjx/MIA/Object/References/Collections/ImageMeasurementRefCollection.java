package wbif.sjx.MIA.Object.References.Collections;

import wbif.sjx.MIA.Object.Units;
import wbif.sjx.MIA.Object.References.ImageMeasurementRef;

import java.util.TreeMap;

public class ImageMeasurementRefCollection extends TreeMap<String,ImageMeasurementRef> implements RefCollection<ImageMeasurementRef> {
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
        // Stripping placeholder for units
        key = Units.replace(key);

        putIfAbsent((String) key,new ImageMeasurementRef((String) key));

        return get(key);

    }

    public boolean hasExportedMeasurements() {
        return size() >= 1;

    }

    public boolean add(ImageMeasurementRef ref) {
        put(ref.getName(),ref);
        return true;
    }
}
