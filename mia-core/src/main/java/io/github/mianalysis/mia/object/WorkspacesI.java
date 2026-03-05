package io.github.mianalysis.mia.object;

import java.io.File;
import java.util.HashMap;

/**
 * Created by sc13967 on 27/10/2016.
 */
public interface WorkspacesI extends Iterable<WorkspaceI> {
    public WorkspaceI getNewWorkspace(File currentFile, int series);
    public WorkspaceI getWorkspace(int ID);
    public HashMap<String, WorkspaceI> getMetadataWorkspaces(String metadataName);
    public void resetProgress();
    public double getOverallProgress();
    public boolean add(WorkspaceI workspace);
    public int size();
    public boolean contains(WorkspaceI workspace);
    public void clear();
}
