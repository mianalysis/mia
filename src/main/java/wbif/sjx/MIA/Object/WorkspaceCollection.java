package wbif.sjx.MIA.Object;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by sc13967 on 27/10/2016.
 */
public class WorkspaceCollection extends LinkedHashSet<Workspace> {
    private int maxID = 0;


    // PUBLIC METHODS

    /*
     * Creates a new workspace and adds it to the collection
     */
    public Workspace getNewWorkspace(File currentFile, int series) {
        Workspace workspace =  new Workspace(++maxID, currentFile, series);

        add(workspace);

        return workspace;

    }

    public HashMap<String, Workspace> getMetadataWorkspaces(String metadataName) {
        HashMap<String,Workspace> workspaces = new HashMap<>();

        int ID = 0;
        for (Workspace currWorkspace:this) {
            // The metadata value to group on
            String metadataValue = currWorkspace.getMetadata().getAsString(metadataName);

            // If no workspace exists for this metadata value, create one
            if (!workspaces.containsKey(metadataValue)) {
                Workspace metadataWorkspace = new Workspace(++ID,null,-1);

                // Creating a store for the number of workspaces in this collection
                metadataWorkspace.getMetadata().put("Count",0);

                workspaces.put(metadataValue,metadataWorkspace);

            }

            // Getting the metadata workspace
            Workspace metadataWorkspace = workspaces.get(metadataValue);

            // Incrementing the workspace count
            metadataWorkspace.getMetadata().put("Count",((int) metadataWorkspace.getMetadata().get("Count")) + 1);

            // Adding all objects to the current workspace (there can only be one image for each name, so it makes no
            // sense to do any images)
            LinkedHashMap<String,ObjCollection> currObjects = currWorkspace.getObjects();
            for (String objName:currObjects.keySet()) {
                // If this is the first time these objects have been added, create a blank ObjCollection
                if (metadataWorkspace.getObjectSet(objName) == null) {
                    metadataWorkspace.addObjects(new ObjCollection(objName));
                }

                // If a collection of these objects already exists, addRef to this
                ObjCollection coreSet = metadataWorkspace.getObjectSet(objName);
                for (Obj currObject:currObjects.get(objName).values()) {
                    // Adding the object and incrementing the count (a new ID has to be assigned for this to prevent
                    // clashes between workspaces)
                    coreSet.put(coreSet.getAndIncrementID(),currObject);
                }
            }
        }

        return workspaces;

    }
}
