package wbif.sjx.ModularImageAnalysis.Object;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;

/**
 * Measurement that holds a single value for an object
 */
public class Measurement {
    // Spatial measures
    public static final String DIRECTIONALITY_RATIO = "Directionality ratio";
    public static final String DURATION = "Duration";
    public static final String TOTAL_PATH_LENGTH = "Total path length";
    public static final String EUCLIDEAN_DISTANCE = "Euclidean distance";

    private String name;
    private double value = Double.NaN;
    private HCModule source = null;


    // CONSTRUCTOR

    public Measurement(String name) {
        this.name = name;

    }

    public Measurement(String name, HCModule source) {
        this.name = name;
        this.source = source;

    }

    public Measurement(String name, double value) {
        this.name = name;
        this.value = value;

    }

    public Measurement(String name, double value, HCModule source) {
        this.name = name;
        this.value = value;
        this.source = source;

    }


    // GETTERS AND SETTERS

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public HCModule getSource() {
        return source;
    }

    public void setSource(HCModule source) {
        this.source = source;
    }
}
