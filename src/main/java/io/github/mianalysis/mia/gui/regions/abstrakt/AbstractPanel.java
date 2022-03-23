package io.github.mianalysis.mia.gui.regions.abstrakt;

import io.github.mianalysis.mia.module.Module;

import javax.swing.*;

public abstract class AbstractPanel extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = 7906763411967111269L;

    public abstract void updatePanel();
    public abstract void updateAvailableModules();
    public abstract void updateModules();
    public abstract void updateModuleStates();
    public abstract void updateParameters();
    public abstract void updateHelpNotes();
    public abstract void updateFileList();
    public abstract void updateSearch();

    public abstract int getPreferredWidth();
    public abstract int getMinimumWidth();
    public abstract int getPreferredHeight();
    public abstract int getMinimumHeight();

    public abstract int getProgress();
    public abstract void setProgress(int progress);
    public abstract void resetJobNumbers();

    public abstract boolean showHelp();
    public abstract void setShowHelp(boolean showHelp);
    public abstract boolean showNotes();
    public abstract void setShowNotes(boolean showNotes);
    public abstract boolean showFileList();
    public abstract void setShowFileList(boolean showFileList);
    public abstract boolean showSearch();
    public abstract void setShowSearch(boolean showSearch);
    
    public abstract Module getLastHelpNotesModule();
    public abstract void setLastHelpNotesModule(Module module);

}
