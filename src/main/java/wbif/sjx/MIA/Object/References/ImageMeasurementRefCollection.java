package wbif.sjx.MIA.Object.References;

import wbif.sjx.MIA.Object.References.Abstract.RefCollection;

public class ImageMeasurementRefCollection extends RefCollection<ImageMeasurementRef> {
    public void updateImageObjectName(String measurementName, String imageObjectName) {
        get(measurementName).setImageName(imageObjectName);
    }

    public String[] getMeasurementNames() {
        return keySet().toArray(new String[0]);
    }

    public void setAllAvailable(boolean available) {
        for (ImageMeasurementRef measurementReference:values()) {
            measurementReference.setAvailable(available);
        }
    }

    public ImageMeasurementRef getOrPut(Object key) {
        putIfAbsent((String) key,new ImageMeasurementRef((String) key));
        return get(key);
    }

    public boolean hasExportedMeasurements() {
        for (ImageMeasurementRef ref:values()) {
            if (ref.isAvailable()) return true;
        }

        return false;

    }

    public void add(ImageMeasurementRef ref) {
        put(ref.getName(),ref);
    }
}
