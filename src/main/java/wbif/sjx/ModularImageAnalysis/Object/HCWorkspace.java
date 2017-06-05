package wbif.sjx.ModularImageAnalysis.Object;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by sc13967 on 02/05/2017.
 */
public class HCWorkspace {
    private LinkedHashMap<HCName, HCObjectSet> objects = new LinkedHashMap<>();
    private LinkedHashMap<HCName, HCImage> images = new LinkedHashMap<>();
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

    public void removeObject(HCName name) {
        objects.remove(name);
    }

    public void addImage(HCImage image) {
        images.put(image.getName(), image);
    }

    public void removeImage(HCName name) {
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

    public HCImage getImage(HCName name) {
        return images.get(name);

    }

    public HCObjectSet getObjectSet(HCName name) {
        return objects.get(name);

    }


    // GETTERS AND SETTERS

    public HashMap<HCName, HCObjectSet> getObjects() {
        return objects;
    }

    public void setObjects(LinkedHashMap<HCName, HCObjectSet> objects) {
        this.objects = objects;
    }

    public HashMap<HCName, HCImage> getImages() {
        return images;
    }

    public void setImages(LinkedHashMap<HCName, HCImage> images) {
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
