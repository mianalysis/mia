package io.github.mianalysis.mia.object.measurements;

public class DefaultMeasurementFactory implements MeasurementFactoryI {

    @Override
    public String getName() {
        return "Default";
    }

    @Override
    public MeasurementI createMeasurement(String name, double value) {
        return new DefaultMeasurement(name, value);
    }

    @Override
    public MeasurementFactoryI duplicate() {
        return new DefaultMeasurementFactory();
    }
}
