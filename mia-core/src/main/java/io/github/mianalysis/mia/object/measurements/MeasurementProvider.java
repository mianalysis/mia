package io.github.mianalysis.mia.object.measurements;

import java.util.LinkedHashMap;

public interface MeasurementProvider {
    public void addMeasurement(MeasurementI measurement);

    public MeasurementI getMeasurement(String name);

    public void removeMeasurement(String name);
    
    public LinkedHashMap<String, MeasurementI> getMeasurements();

    public void setMeasurements(LinkedHashMap<String, MeasurementI> measurements);
    
}
