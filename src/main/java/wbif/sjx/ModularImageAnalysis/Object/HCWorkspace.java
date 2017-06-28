package wbif.sjx.ModularImageAnalysis.Object;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by sc13967 on 02/05/2017.
 */
public class HCWorkspace {
    private LinkedHashMap<String, HCObjectSet> objects = new LinkedHashMap<>();
    private LinkedHashMap<String, HCImage> images = new LinkedHashMap<>();
    private HCMetadata metadata = new HCMetadata();
    private int ID;

    // CONSTRUCTOR

    public HCWorkspace(int ID, File currentFile) {
        this.ID = ID;
        metadata.put(HCMetadata.FILE,currentFile);

    }

    // PUBLIC METHODS

    public void addObjects(HCObjectSet object) {
        objects.put(object.getName(), object);
    }

    public void removeObject(String name) {
        objects.remove(name);
    }

    public void addImage(HCImage image) {
        images.put(image.getName(), image);
    }

    public void removeImage(String name) {
        images.remove(name);
    }

    /**
     * Used to reduce memory of the workspace (particularly for batch processing).
     * @param retainMeasurements Delete image data, but leave measurements
     */
    public void clearAllImages(boolean retainMeasurements) {
        if (retainMeasurements) {
            // Sets the ImagePlus to null, but leaves measurements
            for (HCImage image:images.values()) {
                image.setImagePlus(null);
            }

        } else {
            // Removes all the data
            images = null;
        }
    }

    public HCImage getImage(String name) {
        return images.get(name);

    }

    public HCObjectSet getObjectSet(String name) {
        return objects.get(name);

    }


    // GETTERS AND SETTERS

    public HashMap<String, HCObjectSet> getObjects() {
        return objects;
    }

    public void setObjects(LinkedHashMap<String, HCObjectSet> objects) {
        this.objects = objects;
    }

    public HashMap<String, HCImage> getImages() {
        return images;
    }

    public void setImages(LinkedHashMap<String, HCImage> images) {
        this.images = images;
    }

    public HCMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(HCMetadata metadata) {
        this.metadata = metadata;
    }

    public int getID() {
        return ID;
    }
}
