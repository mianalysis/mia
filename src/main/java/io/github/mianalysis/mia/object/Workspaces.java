package io.github.mianalysis.mia.object;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import io.github.sjcross.sjcommon.mathfunc.CumStat;

/**
 * Created by sc13967 on 27/10/2016.
 */
public class Workspaces extends LinkedHashSet<Workspace> {
    /**
     *
     */
    private static final long serialVersionUID = -2388993934322564718L;
    private int maxID = 0;

    // PUBLIC METHODS

    /*
     * Creates a new workspace and adds it to the collection
     */
    public Workspace getNewWorkspace(File currentFile, int series) {
        Workspace workspace = new Workspace(++maxID, currentFile, series, this);

        add(workspace);

        return workspace;

    }

    public Workspace getWorkspace(int ID) {
        for (Workspace workspace : this)
            if (workspace.getID() == ID)
                return workspace;

        // If no Workspace had this ID, return null
        return null;

    }

    public HashMap<String, Workspace> getMetadataWorkspaces(String metadataName) {
        HashMap<String, Workspace> workspaceList = new HashMap<>();
        Workspaces workspacesMeta = new Workspaces();

        for (Workspace currWorkspace:this) {
            // The metadata value to group on
            String metadataValue = currWorkspace.getMetadata().getAsString(metadataName);

            // If no workspace exists for this metadata value, create one
            if (!workspaceList.containsKey(metadataValue)) {
                Workspace metadataWorkspace = workspacesMeta.getNewWorkspace(null, -1);
                
                // Creating a store for the number of workspaces in this collection
                metadataWorkspace.getMetadata().put("Count",0);

                workspaceList.put(metadataValue,metadataWorkspace);

            }

            // Getting the metadata workspace
            Workspace metadataWorkspace = workspaceList.get(metadataValue);

            // Incrementing the workspace count
            metadataWorkspace.getMetadata().put("Count",((int) metadataWorkspace.getMetadata().get("Count")) + 1);

            // Adding all objects to the current workspace (there can only be one image for each name, so it makes no
            // sense to do any images)
            LinkedHashMap<String,Objs> currObjects = currWorkspace.getObjects();
            for (String objName:currObjects.keySet()) {
                // If there are no current objects, skip this
                Objs currObjectSet = currObjects.get(objName);
                if (currObjectSet == null)
                    continue;

                // If this is the first time these objects have been added, create a blank Objs
                if (metadataWorkspace.getObjectSet(objName) == null)
                    metadataWorkspace.addObjects(new Objs(objName,currObjectSet));
                
                // If a collection of these objects already exists, add to this
                Objs coreSet = metadataWorkspace.getObjectSet(objName);
                for (Obj currObject:currObjectSet.values())
                    // Adding the object and incrementing the count (a new ID has to be assigned for this to prevent
                    // clashes between workspaces)
                    coreSet.put(coreSet.getAndIncrementID(),currObject);
                
            }
        }

        return workspaceList;

    }

    public synchronized void resetProgress() {
        for (Workspace workspace : this) {
            workspace.setProgress(0);
        }
    }

    public synchronized double getOverallProgress() {
        CumStat cs = new CumStat();
        for (Workspace workspace : this)
            cs.addMeasure(workspace.getProgress());

        // Subtracting 1 from the total, so it doesn't hit 100% until exporting is done
        return cs.getMean() - 0.01;

    }
}
