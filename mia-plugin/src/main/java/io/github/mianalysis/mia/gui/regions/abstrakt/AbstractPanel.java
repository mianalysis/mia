package io.github.mianalysis.mia.gui.regions.abstrakt;

import io.github.mianalysis.mia.module.Module;

import javax.swing.*;

import com.drew.lang.annotations.Nullable;

public abstract class AbstractPanel extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = 7906763411967111269L;

    public abstract void updatePanel(boolean testAnalysis, @Nullable Module startModule);
    public abstract void updateAvailableModules();
    public abstract void updateModules(boolean testAnalysis, @Nullable Module startModule);
    public abstract void updateModuleStates();
    public abstract void updateParameters(boolean testAnalysis, @Nullable Module startModule);

    public abstract int getPreferredWidth();
    public abstract int getMinimumWidth();
    public abstract int getPreferredHeight();
    public abstract int getMinimumHeight();

    public abstract double getProgress();
    public abstract void setProgress(double progress);
    public abstract void resetJobNumbers();

    public abstract void setShowSidebar(boolean showSidebar);
    
    public abstract Module getLastHelpNotesModule();
    public abstract void setLastHelpNotesModule(Module module);

}
