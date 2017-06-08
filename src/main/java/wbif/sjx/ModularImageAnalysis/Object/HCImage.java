package wbif.sjx.ModularImageAnalysis.Object;

import ij.ImagePlus;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by steph on 30/04/2017.
 */
public class HCImage {
    private HCName name;
    private ImagePlus imagePlus;
    private LinkedHashMap<String,HCMeasurement> singleMeasurements = new LinkedHashMap<>();
//    private LinkedHashMap<String,HCMultiMeasurement> multiMeasurements = new LinkedHashMap<>();


    // CONSTRUCTORS

    public HCImage(HCName name, ImagePlus imagePlus) {
        this.name = name;
        this.imagePlus = imagePlus;

    }


    // PUBLIC METHODS

    public void addMeasurement(String name, HCMeasurement measurement) {
        singleMeasurements.put(name,measurement);

    }

    public HCMeasurement getMeasurement(String name) {
        return singleMeasurements.get(name);

    }


    // GETTERS AND SETTERS

    public HCName getName() {
        return name;
    }

    public ImagePlus getImagePlus() {
        return imagePlus;
    }

    public void setImagePlus(ImagePlus imagePlus) {
        this.imagePlus = imagePlus;
    }

    public HashMap<String, HCMeasurement> getSingleMeasurements() {
        return singleMeasurements;
    }

    public void setSingleMeasurements(LinkedHashMap<String, HCMeasurement> singleMeasurements) {
        this.singleMeasurements = singleMeasurements;
    }

}