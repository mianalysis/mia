package wbif.sjx.ModularImageAnalysis.Object;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/**
 * Created by sc13967 on 19/05/2017.
 */
public class MeasurementCollection extends LinkedHashMap<String,LinkedHashSet<String>> {
    // PUBLIC METHODS

    public void addMeasurement(String objectName, String measurementName) {
        computeIfAbsent(objectName,k -> new LinkedHashSet<>());
        get(objectName).add(measurementName);

    }

    public String[] getMeasurementNames(String objectName) {
        return get(objectName) == null ? new String[]{""} : get(objectName).toArray(new String[get(objectName).size()]);

    }
}
