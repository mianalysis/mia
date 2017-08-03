package wbif.sjx.ModularImageAnalysis.Object;

import wbif.sjx.common.Object.HCMetadata;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by sc13967 on 02/05/2017.
 */
public class Workspace {
    private LinkedHashMap<String, ObjSet> objects = new LinkedHashMap<>();
    private LinkedHashMap<String, Image> images = new LinkedHashMap<>();
    private HCMetadata metadata = new HCMetadata();
    private int ID;

    // CONSTRUCTOR

    public Workspace(int ID, File currentFile) {
        this.ID = ID;
        metadata.put(HCMetadata.FILE,currentFile);

    }

    // PUBLIC METHODS

    public void addObjects(ObjSet object) {
        objects.put(object.getName(), object);
    }

    public void removeObject(String name) {
        objects.remove(name);

        // Running garbage collector
        Runtime.getRuntime().gc();

    }

    public void addImage(Image image) {
        images.put(image.getName(), image);
    }

    public void removeImage(String name) {
        images.remove(name);

        // Running garbage collector
        Runtime.getRuntime().gc();

    }

    /**
     * Used to reduce memory of the workspace (particularly for batch processing).
     * @param retainMeasurements Delete image data, but leave measurements
     */
    public void clearAllImages(boolean retainMeasurements) {
        if (retainMeasurements) {
            // Sets the ImagePlus to null, but leaves measurements
            for (Image image:images.values()) {
                image.setImagePlus(null);
            }

        } else {
            // Removes all the data
            images = null;
        }
    }

    public void clearAllObjects(boolean retainMeasurements) {
        if (retainMeasurements) {
            // Sets the ImagePlus to null, but leaves measurements
            for (ObjSet objSet:objects.values()) {
                for (Obj obj:objSet.values()) {
                    obj.setCoordinates(null);

                }
            }

        } else {
            // Removes all the data
            objects = null;
        }
    }

    public Image getImage(String name) {
        return images.get(name);

    }

    public ObjSet getObjectSet(String name) {
        return objects.get(name);

    }


    // GETTERS AND SETTERS

    public HashMap<String, ObjSet> getObjects() {
        return objects;
    }

    public void setObjects(LinkedHashMap<String, ObjSet> objects) {
        this.objects = objects;
    }

    public HashMap<String, Image> getImages() {
        return images;
    }

    public void setImages(LinkedHashMap<String, Image> images) {
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
