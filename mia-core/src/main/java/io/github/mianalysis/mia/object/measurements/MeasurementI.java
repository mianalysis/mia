package io.github.mianalysis.mia.object.measurements;

/**
 * Measurement that holds a single value for an object
 */
public interface MeasurementI {
    public MeasurementI duplicate();


    // GETTERS AND SETTERS

    public String getName();

    public double getValue();

    public void setValue(double value);

}
