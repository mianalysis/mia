package wbif.sjx.ModularImageAnalysis.Object;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/**
 * Created by sc13967 on 19/05/2017.
 */
public class MeasurementCollection extends LinkedHashMap<String,LinkedHashSet<String>> {
    // PUBLIC METHODS

    public void addMeasurement(String imageObjectName, String measurementName) {
        computeIfAbsent(imageObjectName,k -> new LinkedHashSet<>());
        get(imageObjectName).add(measurementName);

    }

    public LinkedHashMap<String, LinkedHashSet<String>> getImageMeasurements() {
        return imageMeasurements;
    }

    public void addObjectMeasurement(String objectName, String measurementName) {
        objectMeasurements.computeIfAbsent(objectName,k -> new LinkedHashSet<>());
        objectMeasurements.get(objectName).add(measurementName);

    }

    public String[] getObjectMeasurementNames(String objectName) {
        if (objectMeasurements.get(objectName) == null) {
            return new String[]{""};
        } else {
            String[] measurements = new String[objectMeasurements.get(objectName).size()];
            return objectMeasurements.get(objectName).toArray(measurements);
        }
    }

    public LinkedHashMap<String, LinkedHashSet<String>> getObjectMeasurements() {
        return objectMeasurements;
    }
}
