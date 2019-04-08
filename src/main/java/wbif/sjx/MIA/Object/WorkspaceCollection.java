package wbif.sjx.MIA.Object;

import java.io.File;
import java.util.HashMap;
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

    public HashMap<String, Set<Workspace>> getMetadataWorkspaces(String metadataName) {
        HashMap<String,Set<Workspace>> workspaces = new HashMap<>();

        int ID = 0;
        for (Workspace currWorkspace:this) {
            String metadataValue = currWorkspace.getMetadata().getAsString(metadataName);

            if (!workspaces.containsKey(metadataValue)) {
                Workspace metadataWorkspace = new Workspace(++ID,null,-1);
                metadataWorkspace.setObjects(currWorkspace.getObjects());
                metadataWorkspace.setImages(images);
                workspaces.put(t,metadataWorkspace);
            }

            // Adding the current Obj to the new Workspace
            workspaces.get(t).addObject(obj);
        }

        for (ObjCollection collection:objects.values()) {
            for (Obj obj:collection.values()) {


            }
        }

        return workspaces;

    }
}
