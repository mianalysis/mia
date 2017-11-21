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

    public String[] getMeasurementNames(String imageObjectName) {
        return get(imageObjectName) == null ? new String[]{""} : get(imageObjectName).toArray(new String[get(imageObjectName).size()]);

    }
}
