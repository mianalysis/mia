package wbif.sjx.ModularImageAnalysis.Object;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by sc13967 on 19/05/2017.
 */
public class MeasurementCollection {
    private LinkedHashMap<String, LinkedHashSet<MeasurementReference>> imageMeasurements = new LinkedHashMap<>();
    private LinkedHashMap<String, LinkedHashSet<MeasurementReference>> objectMeasurements = new LinkedHashMap<>();

    // PUBLIC METHODS
    public void addImageMeasurement(String imageName, String measurementName) {
        MeasurementReference measurementReference = new MeasurementReference(measurementName);
        imageMeasurements.computeIfAbsent(imageName,k -> new LinkedHashSet<>());
        imageMeasurements.get(imageName).add(measurementReference);

    }

    public String[] getImageMeasurementNames(String imageName) {
        if (imageMeasurements.get(imageName) == null) {
            return null;
        } else {
            String[] measurements = new String[imageMeasurements.get(imageName).size()];
            return imageMeasurements.get(imageName).stream().map(MeasurementReference::getMeasurementName)
                    .collect(Collectors.toList()).toArray(measurements);
        }
    }

    public LinkedHashSet<MeasurementReference> getImageMeasurements(String imageName) {
        return imageMeasurements.get(imageName);
    }

    public LinkedHashMap<String, LinkedHashSet<MeasurementReference>> getImageMeasurements() {
        return imageMeasurements;
    }

    public void addObjectMeasurement(String objectName, String measurementName) {
        MeasurementReference measurementReference = new MeasurementReference(measurementName);
        objectMeasurements.computeIfAbsent(objectName,k -> new LinkedHashSet<>());
        objectMeasurements.get(objectName).add(measurementReference);

    }

    public String[] getObjectMeasurementNames(String objectName) {
        if (imageMeasurements.get(objectName) == null) {
            return null;
        } else {
            String[] measurements = new String[objectMeasurements.get(objectName).size()];
            return imageMeasurements.get(objectName).stream().map(MeasurementReference::getMeasurementName)
                    .collect(Collectors.toList()).toArray(measurements);
        }
    }

    public LinkedHashSet<MeasurementReference> getObjectMeasurements(String objectName) {
        return objectMeasurements.get(objectName);
    }

    public LinkedHashMap<String, LinkedHashSet<MeasurementReference>> getObjectMeasurements() {
        return objectMeasurements;
    }
}
