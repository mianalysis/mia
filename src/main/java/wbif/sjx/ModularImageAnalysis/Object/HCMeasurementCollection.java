package wbif.sjx.ModularImageAnalysis.Object;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/**
 * Created by sc13967 on 19/05/2017.
 */
public class HCMeasurementCollection extends LinkedHashMap<String,LinkedHashSet<String>> {
    // PUBLIC METHODS

    public void addMeasurement(String objectName, String measurementName) {
        computeIfAbsent(objectName,k -> new LinkedHashSet<>());
        get(objectName).add(measurementName);

    }

    public String[] getMeasurementNames(String measurementName) {
        return get(measurementName) == null ? new String[]{""} : get(measurementName).toArray(new String[get(measurementName).size()]);

    }
}
