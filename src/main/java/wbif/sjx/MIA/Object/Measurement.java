package wbif.sjx.MIA.Object;

import wbif.sjx.MIA.Module.Module;

/**
 * Measurement that holds a single value for an object
 */
public class Measurement {
    private final String name;
    private double value = Double.NaN;
    private Module source = null;


    // CONSTRUCTOR

    public Measurement(String name) {
        name = Units.replace(name);
        this.name = name;

    }

    public Measurement(String name, Module source) {
        name = Units.replace(name);
        this.name = name;
        this.source = source;

    }

    public Measurement(String name, double value) {
        name = Units.replace(name);
        this.name = name;
        this.value = value;

    }

    public Measurement(String name, double value, Module source) {
        name = Units.replace(name);
        this.name = name;
        this.value = value;
        this.source = source;

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

    public Module getSource() {
        return source;
    }

    public void setSource(Module source) {
        this.source = source;
    }
}
