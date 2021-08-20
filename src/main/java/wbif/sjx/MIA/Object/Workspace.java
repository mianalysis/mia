package wbif.sjx.MIA.Object;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.commons.io.FilenameUtils;

import ij.measure.ResultsTable;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.References.MetadataRef;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.common.MetadataExtractors.Metadata;

/**
 * Created by sc13967 on 02/05/2017.
 */
public class Workspace {
    private WorkspaceCollection workspaces;
    private LinkedHashMap<String, ObjCollection> objects = new LinkedHashMap<>();
    private LinkedHashMap<String, Image> images = new LinkedHashMap<>();
    private Metadata metadata = new Metadata();
    private int ID;
    private double progress = 0;
    private Status status = Status.PASS;
    // private boolean analysisFailed = false;
    private boolean exportWorkspace = true;


    // CONSTRUCTOR

    protected Workspace(int ID, File file, int series, WorkspaceCollection workspaces) {
        this.ID = ID;
        this.workspaces = workspaces;

        metadata.setFile(file);
        metadata.setSeriesNumber(series);

        if (file == null) {
            metadata.setFilepath("");
            metadata.setFilename("");
            metadata.setExt("");
        } else {
            metadata.setFilepath(FilenameUtils.getFullPath(file.getAbsolutePath()));
            metadata.setFilename(FilenameUtils.getBaseName(file.getName()));
            metadata.setExt(FilenameUtils.getExtension(file.getName()));
        }
    }


    // PUBLIC METHODS

    public void addObjects(ObjCollection object) {
        objects.put(object.getName(), object);
    }

    public void removeObjects(String name, boolean retainMeasurements) {
        if (retainMeasurements) {
            for (Obj obj:objects.get(name).values()) {
                obj.clearAllCoordinates();
            }
        } else {
            objects.remove(name);
        }
    }

    public void addImage(Image image) {
        images.put(image.getName(), image);
    }

    public void removeImage(String name, boolean retainMeasurements) {
        if (retainMeasurements) {
            images.get(name).getImagePlus().close();
        } else {
            images.remove(name);
        }
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
            images = new LinkedHashMap<>();
        }
    }

    public void clearAllObjects(boolean retainMeasurements) {
        if (retainMeasurements) {
            // Sets the ImagePlus to null, but leaves measurements
            for (ObjCollection objCollection :objects.values()) {
                for (Obj obj: objCollection.values()) {
                    obj.clearAllCoordinates();
                }
            }
        } else {
            // Removes all the data
            objects = new LinkedHashMap<>();
        }
    }

    public void clearMetadata() {
        String filename = metadata.getFilename();
        String filepath = metadata.getFilepath();
        String extension = metadata.getExt();
        File file = metadata.getFile();
        String seriesName = metadata.getSeriesName();
        int seriesNumber = metadata.getSeriesNumber();

        metadata.clear();

        metadata.setFilename(filename);
        metadata.setFilepath(filepath);
        metadata.setExt(extension);
        metadata.setFile(file);
        metadata.setSeriesName(seriesName);
        metadata.setSeriesNumber(seriesNumber);

    }

    public void showMetadata(Module module) {
        // Getting MeasurementReferences
        MetadataRefCollection metadataRefs = module.updateAndGetMetadataReferences();

        // Creating a new ResultsTable for these values
        ResultsTable rt = new ResultsTable();

        for (MetadataRef metadataRef:metadataRefs.values()) {
            String metadataName = metadataRef.getName();
            rt.setValue(metadataName, 0, metadata.getAsString(metadataName));
        }

        rt.show("\"" + module.getName() + " \"metadata values");

    }

    public void showMetadata() {
        // Creating a new ResultsTable for these values
        ResultsTable rt = new ResultsTable();

        // Iterating over each metadata value
        for (String metadataName : metadata.keySet()) rt.setValue(metadataName, 0, metadata.getAsString(metadataName));

        // Displaying the results table
        rt.show("Metadata values");

    }

    public Image getImage(String name) {
        return images.get(name);

    }

    public ObjCollection getObjectSet(String name) {
        return objects.get(name);

    }

    /*
     * Creates a structure containing new Workspaces, each of which represent a different time point
     */
    public HashMap<Integer,Workspace> getSingleTimepointWorkspaces() {
        HashMap<Integer, Workspace> workspaceList = new HashMap<>();
        WorkspaceCollection workspacesT = new WorkspaceCollection();

        for (ObjCollection collection:objects.values()) {
            for (Obj obj:collection.values()) {
                int t = obj.getT();

                // If there isn't already a Workspace for this time point, addRef one
                if (!workspaceList.containsKey(t)) {
                    Workspace workspace = workspacesT.getNewWorkspace(null, -1);
                    workspace.setMetadata(metadata);
                    workspace.setImages(images);
                    workspaceList.put(t,workspace);
                }

                // Adding the current Obj to the new Workspace
                if (workspaceList.get(t).getObjectSet(obj.getName()) == null) {
                    ObjCollection currObjects = new ObjCollection(obj.getName(), obj.getObjectCollection());
                    workspaceList.get(t).addObjects(currObjects);
                }
                
                workspaceList.get(t).getObjectSet(obj.getName()).add(obj);

            }
        }

        return workspaceList;

    }


    // GETTERS AND SETTERS

    public LinkedHashMap<String, ObjCollection> getObjects() {
        return objects;
    }

    public void setObjects(LinkedHashMap<String, ObjCollection> objects) {
        this.objects = objects;
    }

    public LinkedHashMap<String, Image> getImages() {
        return images;
    }

    public void setImages(LinkedHashMap<String, Image> images) {
        this.images = images;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public int getID() {
        return ID;
    }

    public synchronized double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean exportWorkspace() {
        return exportWorkspace;
    }

    public void setExportWorkspace(boolean exportWorkspace) {
        this.exportWorkspace = exportWorkspace;
    }

    public WorkspaceCollection getWorkspaces() {
        return workspaces;

    }

    public void setWorkspaces(WorkspaceCollection workspaces) {
        this.workspaces = workspaces;
        
    }

    @Override
    public String toString() {
        return "Workspace{File: "+metadata.getFilename()+", series: "+metadata.getSeriesNumber()+"("+metadata.getSeriesName()+")}";
    }
}
