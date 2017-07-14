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
    private LinkedHashMap<String,MIAMeasurement> singleMeasurements = new LinkedHashMap<>();
//    private LinkedHashMap<String,HCMultiMeasurement> multiMeasurements = new LinkedHashMap<>();


    // CONSTRUCTORS

    public Image(String name, ImagePlus imagePlus) {
        this.name = name;
        this.imagePlus = imagePlus;

    }


    // PUBLIC METHODS

    public void addMeasurement(String name, MIAMeasurement measurement) {
        singleMeasurements.put(name,measurement);

    }

    public MIAMeasurement getMeasurement(String name) {
        return singleMeasurements.get(name);

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

    public HashMap<String, MIAMeasurement> getSingleMeasurements() {
        return singleMeasurements;
    }

    public void setSingleMeasurements(LinkedHashMap<String, MIAMeasurement> singleMeasurements) {
        this.singleMeasurements = singleMeasurements;
    }

}