package io.github.mianalysis.mia.gui.regions.editingpanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.drew.lang.annotations.Nullable;
import com.formdev.flatlaf.ui.FlatDropShadowBorder;
import com.formdev.flatlaf.util.SystemInfo;

import ij.Prefs;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.gui.regions.ShadowPanel;
import io.github.mianalysis.mia.gui.regions.abstrakt.AbstractPanel;
import io.github.mianalysis.mia.gui.regions.extrapanels.ExtraPanel;
import io.github.mianalysis.mia.gui.regions.extrapanels.filelist.FileListPanel;
import io.github.mianalysis.mia.gui.regions.extrapanels.helpandnotes.HelpNotesPanel;
import io.github.mianalysis.mia.gui.regions.parameterlist.ParametersPanel;
import io.github.mianalysis.mia.gui.regions.progressandstatus.StatusPanel;
import io.github.mianalysis.mia.gui.regions.workflowmodules.ModulePanel;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.system.Colours;
import io.github.mianalysis.mia.process.analysishandling.AnalysisTester;

public class EditingPanel extends AbstractPanel {
    private static final long serialVersionUID = -6063268799004206526L;
    private static int frameHeight = GUI.getFrameHeight();
    private static int minimumFrameHeight = GUI.getMinimumFrameHeight();

    private final EditingControlPanel editingControlPanel = new EditingControlPanel();
    private final ModulePanel modulesPanel = new ModulePanel();
    private final ParametersPanel parametersPanel = new ParametersPanel();
    private final ExtraPanel extraPanel = new ExtraPanel();
    private final StatusPanel statusPanel = new StatusPanel();
    private final JSplitPane splitPane;

    private boolean showHelp = Prefs.get("MIA.showEditingHelp", false);
    private boolean showNotes = Prefs.get("MIA.showEditingNotes", false);
    private boolean showFileList = Prefs.get("MIA.showEditingFileList", false);
    private boolean showSearch = Prefs.get("MIA.showEditingSearch", false);
    private Module lastHelpNotesModule = null;

    public EditingPanel() {
        setLayout(new GridBagLayout());
        setBackground(Colours.getLightGrey(false));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 5;

        if (SystemInfo.isMacFullWindowContentSupported) {
            JPanel titleBar = new JPanel();
            titleBar.setOpaque(false);
            titleBar.setPreferredSize(new Dimension(100, 33));
            titleBar.setBorder(new FlatDropShadowBorder(Color.GRAY, new Insets(0, 0, 5, 0), 1));
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.NORTH;
            add(titleBar, c);

            JPanel titleBarColour = new JPanel();
            titleBarColour.setBackground(Colours.getLightBlue(false));
            titleBarColour.setPreferredSize(new Dimension(100, 28));
            add(titleBarColour, c);
            c.gridy++;
        }

        c.weighty = 1;
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 5, 0);
        c.fill = GridBagConstraints.VERTICAL;
        add(new ShadowPanel(editingControlPanel), c);

        // Initialising the status panel
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 5;
        c.gridy++;
        c.insets = new Insets(0, 5, 5, 5);
        add(new ShadowPanel(statusPanel), c);

        c.gridx++;
        c.gridy = 1;
        c.gridwidth = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.VERTICAL;
        c.insets = new Insets(5, 5, 5, 0);
        modulesPanel.setOpaque(false);
        add(new ShadowPanel(modulesPanel), c);

        ShadowPanel shadowExtraPanel = new ShadowPanel(extraPanel);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new ShadowPanel(parametersPanel),
                shadowExtraPanel);
        splitPane.setPreferredSize(new Dimension(1, 1));
        splitPane.setDividerSize(5);
        splitPane.setDividerLocation(0.5);
        splitPane.setOpaque(false);
        splitPane.setOneTouchExpandable(true);
        splitPane.putClientProperty("JSplitPane.expandableSide", "left");

        c.gridx++;
        c.gridy = 1;
        c.gridwidth = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5, 5, 5, 5);
        add(splitPane, c);

        MIA.log.writeDebug("EditingPanel.java - set extra panel visibility");
        shadowExtraPanel.setVisible(false);
        updateSeparators();

    }

    @Override
    public void updatePanel(boolean testAnalysis, @Nullable Module startModule) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(0, 10, 0, 0);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH;

        statusPanel.add(GUI.getTextField(), c);

        updateModules(testAnalysis, startModule);
        updateParameters(testAnalysis, startModule);

        revalidate();
        repaint();

    }

    void updateSeparators() {
        // if (showHelp || showNotes || showFileList) {
        // splitPane.setDividerSize(5);
        // splitPane.getRightComponent().setVisible(true);
        // splitPane.setDividerLocation(0.5);
        // splitPane.setMinimumSize(new Dimension(ParametersPanel.getMinimumWidth() +
        // pane1MinWidth, 1));
        // } else {
        // splitPane.setDividerSize(0);
        // splitPane.getRightComponent().setVisible(false);
        // splitPane.setMinimumSize(new Dimension(ParametersPanel.getMinimumWidth(),
        // 1));
        // }
    }

    @Override
    public int getPreferredWidth() {
        int currentWidth = EditingControlPanel.getMinimumWidth() + ModulePanel.getMinimumWidth()
                + ParametersPanel.getPreferredWidth();

        // if (showSearch)
        // currentWidth = currentWidth + SearchPanel.getMinimumWidth(); // Fixed width
        if (showHelp || showNotes)
            currentWidth = currentWidth + HelpNotesPanel.getPreferredWidth();
        if (showFileList)
            currentWidth = currentWidth + FileListPanel.getPreferredWidth();

        return currentWidth;

    }

    @Override
    public int getMinimumWidth() {
        int currentWidth = EditingControlPanel.getMinimumWidth() + ModulePanel.getMinimumWidth()
                + ParametersPanel.getMinimumWidth();

        // if (showSearch)
        // currentWidth = currentWidth + SearchPanel.getMinimumWidth();
        if (showHelp || showNotes)
            currentWidth = currentWidth + HelpNotesPanel.getMinimumWidth();
        if (showFileList)
            currentWidth = currentWidth + FileListPanel.getMinimumWidth();

        return currentWidth;

    }

    @Override
    public int getPreferredHeight() {
        return frameHeight;
    }

    @Override
    public int getMinimumHeight() {
        return minimumFrameHeight;
    }

    @Override
    public double getProgress() {
        return statusPanel.getValue();
    }

    @Override
    public void setProgress(double progress) {
        statusPanel.setValue(progress);
        extraPanel.getFileListPanel().updatePanel();
    }

    @Override
    public void resetJobNumbers() {
        extraPanel.getFileListPanel().resetJobNumbers();
    }

    @Override
    public void updateAvailableModules() {
        editingControlPanel.listAvailableModules();
    }

    @Override
    public void updateModules(boolean testAnalysis, @Nullable Module startModule) {
        if (testAnalysis)
            AnalysisTester.testModules(GUI.getModules(), GUI.getTestWorkspace(), startModule);

        modulesPanel.updatePanel();

    }

    @Override
    public void updateModuleStates() {
        modulesPanel.updateModuleStates();

    }

    @Override
    public void updateParameters(boolean testAnalysis, @Nullable Module startModule) {
        parametersPanel.updatePanel(GUI.getFirstSelectedModule());
    }

    @Override
    public boolean showHelp() {
        return showHelp;
    }

    @Override
    public void setShowHelp(boolean showHelp) {
        // this.showHelp = showHelp;
        // Prefs.set("MIA.showEditingHelp", showHelp);
        // Prefs.savePreferences();

        // helpNotesPanel.showHelp(showHelp);
        // GUI.updatePanel();

        // updateSeparators();

    }

    @Override
    public boolean showNotes() {
        return showNotes;
    }

    @Override
    public void setShowNotes(boolean showNotes) {
        // this.showNotes = showNotes;
        // Prefs.set("MIA.showEditingNotes", showNotes);
        // Prefs.savePreferences();

        // helpNotesPanel.showNotes(showNotes);
        // GUI.updatePanel();

        // updateSeparators();

    }

    @Override
    public boolean showFileList() {
        return showFileList;
    }

    @Override
    public void setShowFileList(boolean showFileList) {
        // this.showFileList = showFileList;
        // Prefs.set("MIA.showEditingFileList", showFileList);
        // Prefs.savePreferences();

        // fileListPanel.setVisible(showFileList);
        // GUI.updatePanel();

        // updateSeparators();

    }

    @Override
    public boolean showSearch() {
        return showSearch;
    }

    @Override
    public void setShowSearch(boolean showSearch) {
        // this.showSearch = showSearch;
        // Prefs.set("MIA.showEditingSearch", showSearch);
        // Prefs.savePreferences();

        // searchPanel.setVisible(showSearch);
        // GUI.updatePanel();

        // updateSeparators();

    }

    @Override
    public Module getLastHelpNotesModule() {
        return lastHelpNotesModule;
    }

    @Override
    public void setLastHelpNotesModule(Module module) {
        lastHelpNotesModule = module;
    }
}
