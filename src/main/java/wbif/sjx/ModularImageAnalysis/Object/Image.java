package wbif.sjx.ModularImageAnalysis.Object;

import ij.ImagePlus;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by steph on 30/04/2017.
 */
public class Image {
    private String name;
    private ImagePlus imagePlus;
    private LinkedHashMap<String,Measurement> measurements = new LinkedHashMap<>();


    // CONSTRUCTORS

    public Image(String name, ImagePlus imagePlus) {
        this.name = name;
        this.imagePlus = imagePlus;

    }


    // PUBLIC METHODS

    public void addMeasurement(Measurement measurement) {
        measurements.put(measurement.getName(), measurement);

    }

    public Measurement getMeasurement(String name) {
        return measurements.get(name);

    }


    // GETTERS AND SETTERS

    public String getName() {
        return name;
    }

    public ImagePlus getImagePlus() {
        return imagePlus;
    }

    public void setImagePlus(ImagePlus imagePlus) {
        this.imagePlus = imagePlus;
    }

    public HashMap<String, Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(LinkedHashMap<String, Measurement> singleMeasurements) {
        this.measurements = singleMeasurements;
    }

}