package wbif.sjx.ModularImageAnalysis.Object;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;

/**
 * Measurement that holds a single value for an object
 */
public class MIAMeasurement {
    // Spatial measures
    public static final String DIRECTIONALITY_RATIO = "Directionality ratio";
    public static final String DURATION = "Duration";
    public static final String TOTAL_PATH_LENGTH = "Total path length";
    public static final String EUCLIDEAN_DISTANCE = "Euclidean distance";
    public static final String X_CENTROID_MEAN_PX = "X-Centroid Mean (px)";
    public static final String Y_CENTROID_MEAN_PX = "Y-Centroid Mean (px)";
    public static final String Z_CENTROID_MEAN_SLICE = "Z-Centroid Mean (slice)";
    public static final String X_CENTROID_MEDIAN_PX = "X-Centroid Median (px)";
    public static final String Y_CENTROID_MEDIAN_PX = "Y-Centroid Median (px)";
    public static final String Z_CENTROID_MEDIAN_SLICE = "Z-Centroid Median (slice)";

    // Other measures
    public static final String CLASS = "Class";

    private String name;
    private double value = Double.NaN;
    private HCModule source = null;


    // CONSTRUCTOR

    public MIAMeasurement(String name) {
        this.name = name;

    }

    public MIAMeasurement(String name, HCModule source) {
        this.name = name;
        this.source = source;

    }

    public MIAMeasurement(String name, double value) {
        this.name = name;
        this.value = value;

    }

    public MIAMeasurement(String name, double value, HCModule source) {
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
