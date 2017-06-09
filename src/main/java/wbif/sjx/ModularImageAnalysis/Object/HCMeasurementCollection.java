package wbif.sjx.ModularImageAnalysis.Object;

import java.util.HashSet;
import java.util.LinkedHashMap;

/**
 * Created by sc13967 on 19/05/2017.
 */
public class HCMeasurementCollection extends LinkedHashMap<HCName,HashSet<String>> {
    // PUBLIC METHODS

    public void addMeasurement(HCName objectName, String measurementName) {
        computeIfAbsent(objectName,k -> new HashSet<>());
        get(objectName).add(measurementName);

    }

    public String[] getMeasurementNames(HCName measurementName) {
        return get(measurementName) == null ? new String[]{""} : get(measurementName).toArray(new String[get(measurementName).size()]);

    }
}
