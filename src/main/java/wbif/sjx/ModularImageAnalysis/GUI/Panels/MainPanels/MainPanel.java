package wbif.sjx.ModularImageAnalysis.GUI.Panels.MainPanels;

import wbif.sjx.ModularImageAnalysis.Module.Module;

import javax.swing.*;

public abstract class MainPanel extends JPanel {
    public abstract void updatePanel();
    public abstract void updateModules();
    public abstract void updateParameters();
    public abstract void updateHelpNotes();
    public abstract void updateTestFile();

    public abstract int getPreferredWidth();
    public abstract int getMinimumWidth();
    public abstract int getPreferredHeight();
    public abstract int getMinimumHeight();

    public abstract int getProgress();
    public abstract void setProgress(int progress);

    public abstract boolean showHelpNotes();
    public abstract void setShowHelpNotes(boolean showHelpNotes);

    public abstract Module getLastHelpNotesModule();
    public abstract void setLastHelpNotesModule(Module module);

}
