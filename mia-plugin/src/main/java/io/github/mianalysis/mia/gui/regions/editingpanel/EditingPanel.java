package io.github.mianalysis.mia.gui.regions.editingpanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import com.drew.lang.annotations.Nullable;

import ij.Prefs;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.gui.regions.abstrakt.AbstractPanel;
import io.github.mianalysis.mia.gui.regions.extrapanels.ExtraPanel;
import io.github.mianalysis.mia.gui.regions.extrapanels.filelist.FileListPanel;
import io.github.mianalysis.mia.gui.regions.extrapanels.helpandnotes.HelpNotesPanel;
import io.github.mianalysis.mia.gui.regions.parameterlist.ParametersPanel;
import io.github.mianalysis.mia.gui.regions.progressandstatus.ProgressBarPanel;
import io.github.mianalysis.mia.gui.regions.progressandstatus.StatusPanel;
import io.github.mianalysis.mia.gui.regions.workflowmodules.ModulePanel;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.process.analysishandling.AnalysisTester;

public class EditingPanel extends AbstractPanel {
    private static final long serialVersionUID = -6063268799004206526L;
    private static int frameHeight = GUI.getFrameHeight();
    private static int minimumFrameHeight = GUI.getMinimumFrameHeight();

    private final EditingControlPanel editingControlPanel = new EditingControlPanel();
    private final ProgressBarPanel progressBarPanel = new ProgressBarPanel();
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

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 0);
        c.gridx = 0;
        c.gridy = 0;

        // Creating buttons to add and remove modules
        c.weightx = 0;
        c.weighty = 1;
        c.fill = GridBagConstraints.VERTICAL;
        add(editingControlPanel, c);

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        c.gridy++;
        c.weighty = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 5;
        c.insets = new Insets(0, 5, 5, 5);
        add(separator,c);

        // Initialising the status panel
        c.gridy++;
        c.insets = new Insets(0, 5, 5, 5);
        add(statusPanel, c);

        // Initialising the progress bar
        c.gridy++;
        c.insets = new Insets(0, 5, 5, 5);
        add(progressBarPanel, c);

        // separator = new JSeparator(SwingConstants.VERTICAL);
        c.gridx++;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 0;  
        // c.insets = new Insets(5, 0, 5, 0);
        c.fill = GridBagConstraints.VERTICAL;
        // add(separator,c);

        // c.gridx++;              
        c.insets = new Insets(5, 0, 5, 0);        
        add(modulesPanel, c);

        // separator = new JSeparator(SwingConstants.VERTICAL);
        // c.gridx++;
        // c.insets = new Insets(5, 0, 5, 0);
        // add(separator,c);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, parametersPanel, extraPanel);
        splitPane.setPreferredSize(new Dimension(1, 1));
        // splitPane.setBorder(null);
        splitPane.setDividerSize(5);
        splitPane.setDividerLocation(0.5);
        splitPane.setOneTouchExpandable(true);
        splitPane.putClientProperty( "JSplitPane.expandableSide", "left" );
        // BasicSplitPaneUI splitPaneUI = (BasicSplitPaneUI) splitPane.getUI();
        // splitPaneUI.getDivider().setBorder(new EmptyBorder(0, 0, 0, 0));

        c.gridx++;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5, 5, 5, 5);
        add(splitPane, c);

        MIA.log.writeDebug("EditingPanel.java - set extra panel visibility");
        extraPanel.setVisible(true);
        updateSeparators();

    }

    @Override
    public void updatePanel(boolean testAnalysis, @Nullable Module startModule) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(0, 5, 0, 0);
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
    public int getProgress() {
        return progressBarPanel.getValue();
    }

    @Override
    public void setProgress(int progress) {
        progressBarPanel.setValue(progress);
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
