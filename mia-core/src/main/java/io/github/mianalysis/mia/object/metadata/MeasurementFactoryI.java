package io.github.mianalysis.mia.object.measurements;

public interface MeasurementFactoryI {
    public String getName();
    public MeasurementI createMeasurement(String name, double value);
    public MeasurementFactoryI duplicate();

}