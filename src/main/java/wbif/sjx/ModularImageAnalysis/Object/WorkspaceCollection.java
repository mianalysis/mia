package wbif.sjx.ModularImageAnalysis.Object;

import java.io.File;
import java.util.LinkedHashSet;

/**
 * Created by sc13967 on 27/10/2016.
 */
public class WorkspaceCollection extends LinkedHashSet<Workspace> {
    private int maxID = 0;


    // PUBLIC METHODS

    /**
     * Creates a new workspace and adds it to the collection
     * @param currentFile
     * @return
     */
    public Workspace getNewWorkspace(File currentFile) {
        Workspace workspace =  new Workspace(++maxID, currentFile);

        add(workspace);

        return workspace;

    }
}
