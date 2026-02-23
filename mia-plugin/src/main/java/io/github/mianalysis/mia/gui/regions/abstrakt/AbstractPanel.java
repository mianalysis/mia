package io.github.mianalysis.mia.gui.regions.abstrakt;

import javax.swing.JPanel;

import com.drew.lang.annotations.Nullable;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModuleI;

public abstract class AbstractPanel extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = 7906763411967111269L;

    public abstract void updatePanel(boolean testAnalysis, @Nullable ModuleI startModule);

    public abstract void updateAvailableModules();

    public abstract void updateModules(boolean testAnalysis, @Nullable ModuleI startModule);

    public abstract void updateModuleStates();

    public abstract void updateParameters(boolean testAnalysis, @Nullable ModuleI startModule);

    public abstract int getPreferredWidth();

    public abstract int getMinimumWidth();

    public abstract int getPreferredHeight();

    public abstract int getMinimumHeight();

    public abstract double getProgress();

    public abstract void setProgress(double progress);

    public abstract void resetJobNumbers();

    public abstract void setShowSidebar(boolean showSidebar);

    public abstract ModuleI getLastHelpNotesModule();

    public abstract void setLastHelpNotesModule(ModuleI module);

}
