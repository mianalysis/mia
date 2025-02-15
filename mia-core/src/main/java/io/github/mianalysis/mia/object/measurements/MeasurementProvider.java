package io.github.mianalysis.mia.object.measurements;

import java.util.HashMap;
import java.util.LinkedHashMap;

public interface MeasurementProvider {
    public void addMeasurement(Measurement measurement);

    public Measurement getMeasurement(String name);

    public void removeMeasurement(String name);
    
    public HashMap<String, Measurement> getMeasurements();

    public void setMeasurements(LinkedHashMap<String, Measurement> measurements);
    
}
