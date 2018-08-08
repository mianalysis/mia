package wbif.sjx.ModularImageAnalysis.Object;

import wbif.sjx.ModularImageAnalysis.Module.Module;

/**
 * Measurement that holds a single value for an object
 */
public class Measurement {
    private final String name;
    private double value = Double.NaN;
    private Module source = null;


    // CONSTRUCTOR

    public Measurement(String name) {
        this.name = name;

    }

    public Measurement(String name, Module source) {
        this.name = name;
        this.source = source;

    }

    public Measurement(String name, double value) {
        this.name = name;
        this.value = value;

    }

    public Measurement(String name, double value, Module source) {
        this.name = name;
        this.value = value;
        this.source = source;

    }


    // GETTERS AND SETTERS

    public String getName() {
        return Units.replace(name);
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
