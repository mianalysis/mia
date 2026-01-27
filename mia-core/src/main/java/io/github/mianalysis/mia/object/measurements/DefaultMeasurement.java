package io.github.mianalysis.mia.object.measurements;

import io.github.mianalysis.mia.object.units.SpatialUnit;
import io.github.mianalysis.mia.object.units.TemporalUnit;

public class DefaultMeasurement implements MeasurementI {
    private final String name;
    private double value = Double.NaN;


    // CONSTRUCTOR

    public DefaultMeasurement(String name, double value) {
        name = SpatialUnit.replace(name);
        name = TemporalUnit.replace(name);
        this.name = name;
        this.value = value;
    }

    public MeasurementI duplicate() {
        return new DefaultMeasurement(getName(), getValue());
    }


    // GETTERS AND SETTERS

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
