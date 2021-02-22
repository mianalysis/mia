package wbif.sjx.MIA.Object;

import wbif.sjx.MIA.Object.Units.SpatialUnit;
import wbif.sjx.MIA.Object.Units.TemporalUnit;

/**
 * Measurement that holds a single value for an object
 */
public class Measurement {
    private final String name;
    private double value = Double.NaN;


    // CONSTRUCTOR

    public Measurement(String name) {
        name = SpatialUnit.replace(name);
        name = TemporalUnit.replace(name);
        this.name = name;
    }

    public Measurement(String name, double value) {
        name = SpatialUnit.replace(name);
        name = TemporalUnit.replace(name);
        this.name = name;
        this.value = value;
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
