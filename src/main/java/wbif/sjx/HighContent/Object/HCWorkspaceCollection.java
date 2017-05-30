package wbif.sjx.HighContent.Object;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by sc13967 on 27/10/2016.
 */
public class HCWorkspaceCollection extends ArrayList<HCWorkspace> {
    int maxID = 0;

    // PUBLIC METHODS

    /**
     * Creates a new workspace and adds it to the collection
     * @param currentFile
     * @return
     */
    public HCWorkspace getNewWorkspace(File currentFile) {
        HCWorkspace workspace =  new HCWorkspace(++maxID, currentFile);
        add(workspace);

        return workspace;

    }
}
