package io.github.mianalysis.mia.object;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.process.math.CumStat;

/**
 * Created by sc13967 on 27/10/2016.
 */
public class Workspaces extends LinkedHashSet<WorkspaceI> {
    /**
     *
     */
    private static final long serialVersionUID = -2388993934322564718L;
    private int maxID = 0;

    // PUBLIC METHODS

    /*
     * Creates a new workspace and adds it to the collection
     */
    public WorkspaceI getNewWorkspace(File currentFile, int series) {
        WorkspaceI workspace = new Workspace(++maxID, currentFile, series, this);

        add(workspace);

        return workspace;

    }

    public WorkspaceI getWorkspace(int ID) {
        for (WorkspaceI workspace : this)
            if (workspace.getID() == ID)
                return workspace;

        // If no Workspace had this ID, return null
        return null;

    }

    public HashMap<String, WorkspaceI> getMetadataWorkspaces(String metadataName) {
        HashMap<String, WorkspaceI> workspaceList = new HashMap<>();
        Workspaces workspacesMeta = new Workspaces();

        for (WorkspaceI currWorkspace:this) {
            // The metadata value to group on
            String metadataValue = currWorkspace.getMetadata().getAsString(metadataName);

            // If no workspace exists for this metadata value, create one
            if (!workspaceList.containsKey(metadataValue)) {
                WorkspaceI metadataWorkspace = workspacesMeta.getNewWorkspace(null, -1);
                
                // Creating a store for the number of workspaces in this collection
                metadataWorkspace.getMetadata().put("Count",0);

                workspaceList.put(metadataValue,metadataWorkspace);

            }

            // Getting the metadata workspace
            WorkspaceI metadataWorkspace = workspaceList.get(metadataValue);

            // Incrementing the workspace count
            metadataWorkspace.getMetadata().put("Count",((int) metadataWorkspace.getMetadata().get("Count")) + 1);

            // Adding all objects to the current workspace (there can only be one image for each name, so it makes no
            // sense to do any images)
            LinkedHashMap<String,ObjsI> currObjects = currWorkspace.getAllObjects();
            for (String objName:currObjects.keySet()) {
                // If there are no current objects, skip this
                ObjsI currObjectSet = currObjects.get(objName);
                if (currObjectSet == null)
                    continue;

                // If this is the first time these objects have been added, create a blank Objs
                if (metadataWorkspace.getObjects(objName) == null)
                    metadataWorkspace.addObjects(ObjsFactories.getDefaultFactory().createFromExample(objName,currObjectSet));
                
                // If a collection of these objects already exists, add to this
                ObjsI coreSet = metadataWorkspace.getObjects(objName);
                for (ObjI currObject:currObjectSet.values())
                    // Adding the object and incrementing the count (a new ID has to be assigned for this to prevent
                    // clashes between workspaces)
                    coreSet.put(coreSet.getAndIncrementID(),currObject);
                
            }
        }

        return workspaceList;

    }

    public synchronized void resetProgress() {
        for (WorkspaceI workspace : this) {
            workspace.setProgress(0);
        }
    }

    public synchronized double getOverallProgress() {
        CumStat cs = new CumStat();
        for (WorkspaceI workspace : this)
            cs.addMeasure(workspace.getProgress());

        // Subtracting 1 from the total, so it doesn't hit 100% until exporting is done
        return cs.getMean() - 0.01;

    }
}
