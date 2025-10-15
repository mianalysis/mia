package io.github.mianalysis.mia.object;

import java.util.HashMap;
import java.util.LinkedHashMap;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.metadata.MetadataI;
import io.github.mianalysis.mia.object.system.Status;

public interface WorkspaceI {
    public default void addObjects(ObjsI object) {
        getAllObjects().put(object.getName(), object);
    }

    public default ImageI getImage(String name) {
        return getImages().get(name);

    }


    public void removeObjects(String name, boolean retainMeasurements);

    public void addImage(ImageI image);

    public void removeImage(String name, boolean retainMeasurements);

    /**
     * Used to reduce memory of the workspace (particularly for batch processing).
     * @param retainMeasurements When true, measurements associated with this image will be retained, while the pixel information will be cleared
     */
    public void clearAllImages(boolean retainMeasurements);

    public void clearAllObjects(boolean retainMeasurements);

    public void clearMetadata();

    public void showMetadata(Module module);

    public void showMetadata();

    public ObjsI getObjects(String name);

    @Deprecated
    public ObjsI getObjectSet(String name);

    /*
     * Creates a structure containing new Workspaces, each of which represent a different time point
     */
    public HashMap<Integer, WorkspaceI> getSingleTimepointWorkspaces();


    // GETTERS AND SETTERS

    public LinkedHashMap<String, ObjsI> getAllObjects();

    public void setAllObjects(LinkedHashMap<String, ObjsI> objects);

    public LinkedHashMap<String, ImageI> getImages();

    public void setImages(LinkedHashMap<String, ImageI> images);

    public MetadataI getMetadata();

    public void setMetadata(MetadataI metadata);

    public int getID();

    public double getProgress();

    public void setProgress(double progress);

    public Status getStatus();

    public void setStatus(Status status);

    public boolean exportWorkspace();

    public void setExportWorkspace(boolean exportWorkspace);

    public Workspaces getWorkspaces();

    public void setWorkspaces(Workspaces workspaces);

}
