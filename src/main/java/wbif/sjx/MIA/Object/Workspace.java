package wbif.sjx.MIA.Object;

import ij.measure.ResultsTable;
import org.apache.commons.io.FilenameUtils;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.References.MetadataRef;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Process.AnalysisHandling.Analysis;
import wbif.sjx.common.Object.Metadata;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/**
 * Created by sc13967 on 02/05/2017.
 */
public class Workspace {
    private LinkedHashMap<String, ObjCollection> objects = new LinkedHashMap<>();
    private LinkedHashMap<String, Image<?>> images = new LinkedHashMap<>();
    private Metadata metadata = new Metadata();
    private int ID;
    private double progress = 0;
    private boolean analysisFailed = false;


    // CONSTRUCTOR

    public Workspace(int ID, File file, int series) {
        this.ID = ID;

        metadata.setFile(file);
        metadata.setSeriesNumber(series);

        if (file == null) {
            metadata.setFilename("");
            metadata.setExt("");
        } else {
            metadata.setFilename(FilenameUtils.getBaseName(file.getName()));
            metadata.setExt(FilenameUtils.getExtension(file.getName()));
        }
    }


    // PUBLIC METHODS

    public void addObjects(ObjCollection object) {
        objects.put(object.getName(), object);
    }

    /*
     * Adds the provided Obj to the relevant ObjCollection.
     * If there isn't already such a collection, one is created.
     */
    public void addObject(Obj obj) {
        String objectName = obj.getName();
        objects.putIfAbsent(objectName,new ObjCollection(objectName,obj.getCalibration()));
        objects.get(objectName).add(obj);

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

    public void addImage(Image<?> image) {
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
        String extension = metadata.getExt();
        File file = metadata.getFile();
        String seriesName = metadata.getSeriesName();
        int seriesNumber = metadata.getSeriesNumber();

        metadata.clear();

        metadata.setFilename(filename);
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

    public Image<?> getImage(String name) {
        return images.get(name);

    }

    public ObjCollection getObjectSet(String name) {
        return objects.get(name);

    }

    /*
     * Creates a structure containing new Workspaces, each of which represent a different time point
     */
    public HashMap<Integer,Workspace> getSingleTimepointWorkspaces() {
        HashMap<Integer,Workspace> workspaces = new HashMap<>();

        for (ObjCollection collection:objects.values()) {
            for (Obj obj:collection.values()) {
                int t = obj.getT();

                // If there isn't already a Workspace for this time point, addRef one
                if (!workspaces.containsKey(t)) {
                    Workspace workspace = new Workspace(ID,null,-1);
                    workspace.setMetadata(metadata);
                    workspace.setImages(images);
                    workspaces.put(t,workspace);
                }

                // Adding the current Obj to the new Workspace
                workspaces.get(t).addObject(obj);

            }
        }

        return workspaces;

    }


    // GETTERS AND SETTERS

    public LinkedHashMap<String, ObjCollection> getObjects() {
        return objects;
    }

    public void setObjects(LinkedHashMap<String, ObjCollection> objects) {
        this.objects = objects;
    }

    public LinkedHashMap<String, Image<?>> getImages() {
        return images;
    }

    public void setImages(LinkedHashMap<String, Image<?>> images) {
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

    public boolean isAnalysisFailed() {
        return analysisFailed;
    }

    public void setAnalysisFailed(boolean analysisFailed) {
        this.analysisFailed = analysisFailed;
    }

    @Override
    public String toString() {
        return "Workspace{File: "+metadata.getFilename()+", series: "+metadata.getSeriesNumber()+"("+metadata.getSeriesName()+")}";
    }
}
