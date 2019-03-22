package wbif.sjx.ModularImageAnalysis.GUI.Panels;

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

    public abstract Module getActiveModule();
    public abstract void setActiveModule(Module module);

    public abstract boolean showHelpNotes();
    public abstract void setShowHelpNotes(boolean showHelpNotes);

}
