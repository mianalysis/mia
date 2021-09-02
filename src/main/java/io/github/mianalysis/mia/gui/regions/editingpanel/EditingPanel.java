package io.github.mianalysis.mia.gui.regions.editingpanel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import ij.Prefs;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.gui.regions.abstrakt.AbstractPanel;
import io.github.mianalysis.mia.gui.regions.filelist.FileListPanel;
import io.github.mianalysis.mia.gui.regions.helpandnotes.HelpNotesPanel;
import io.github.mianalysis.mia.gui.regions.parameterlist.ParametersPanel;
import io.github.mianalysis.mia.gui.regions.progressandstatus.ProgressBarPanel;
import io.github.mianalysis.mia.gui.regions.progressandstatus.StatusPanel;
import io.github.mianalysis.mia.gui.regions.search.SearchPanel;
import io.github.mianalysis.mia.gui.regions.workflowmodules.ModulePanel;
import io.github.mianalysis.mia.module.Module;

public class EditingPanel extends AbstractPanel {
    private static final long serialVersionUID = -6063268799004206526L;
    private static int frameHeight = GUI.getFrameHeight();
    private static int minimumFrameHeight = GUI.getMinimumFrameHeight();

    private final EditingControlPanel editingControlPanel = new EditingControlPanel();
    private final ProgressBarPanel progressBarPanel = new ProgressBarPanel();
    private final ModulePanel modulesPanel = new ModulePanel();
    private final ParametersPanel parametersPanel = new ParametersPanel();
    private final HelpNotesPanel helpNotesPanel = new HelpNotesPanel();
    private final StatusPanel statusPanel = new StatusPanel();
    private final FileListPanel fileListPanel = new FileListPanel(GUI.getAnalysisRunner().getWorkspaces());
    private final SearchPanel searchPanel = new SearchPanel();
    private final JSplitPane splitPane1;
    private final JSplitPane splitPane2;
    private final JSplitPane splitPane3;

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

        // Initialising the status panel
        c.gridy++;
        c.weighty = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 4;
        c.insets = new Insets(0, 5, 5, 5);
        add(statusPanel, c);

        // Initialising the progress bar
        c.gridy++;
        c.insets = new Insets(0, 5, 5, 5);
        add(progressBarPanel, c);

        splitPane3 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, modulesPanel, searchPanel);
        splitPane3.setPreferredSize(new Dimension(ModulePanel.getMinimumWidth(), Integer.MAX_VALUE));
        splitPane3.setBorder(null);
        splitPane3.setDividerSize(5);
        splitPane3.setDividerLocation(0.5);
        BasicSplitPaneUI splitPaneUI = (BasicSplitPaneUI) splitPane3.getUI();
        splitPaneUI.getDivider().setBorder(new EmptyBorder(0, 0, 0, 0));

        c.gridx++;
        c.gridy = 0;
        c.weightx = 0;
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 5, 0);
        add(splitPane3, c);

        // // Initialising the modules panel
        // c.gridx++;
        // c.gridy = 0;
        // c.weightx = 0;
        // c.gridwidth = 1;
        // c.insets = new Insets(5, 5, 5, 0);
        // add(searchPanel, c);

        // // Initialising the modules panel
        // c.gridx++;
        // add(modulesPanel, c);

        // Initialising the parameters panel
        updateFileList();
        updateHelpNotes();
        updateSearch();

        splitPane1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fileListPanel, helpNotesPanel);
        splitPane1.setPreferredSize(new Dimension(1, 1));
        splitPane1.setBorder(null);
        splitPane1.setDividerSize(5);
        splitPane1.setDividerLocation(0.5);
        splitPaneUI = (BasicSplitPaneUI) splitPane1.getUI();
        splitPaneUI.getDivider().setBorder(new EmptyBorder(0, 0, 0, 0));

        splitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, parametersPanel, splitPane1);
        splitPane2.setPreferredSize(new Dimension(1, 1));
        splitPane2.setBorder(null);
        splitPane2.setDividerSize(5);
        splitPane2.setDividerLocation(0.5);
        splitPaneUI = (BasicSplitPaneUI) splitPane2.getUI();
        splitPaneUI.getDivider().setBorder(new EmptyBorder(0, 0, 0, 0));

        c.gridx++;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5, 5, 5, 5);
        add(splitPane2, c);

        updateSeparators();
        helpNotesPanel.updateSeparator();

    }

    @Override
    public void updatePanel() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(0, 5, 0, 0);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH;

        statusPanel.add(GUI.getTextField(), c);

        updateModules();
        updateParameters();

        updateHelpNotes();
        updateFileList();
        updateSearch();

        revalidate();
        repaint();

    }

    void updateSeparators() {
        if (showSearch) {
            splitPane3.setDividerSize(5);
            splitPane3.getTopComponent().setVisible(true);
            splitPane3.getBottomComponent().setVisible(true);
            splitPane3.setDividerLocation(0.5);
        } else {
            splitPane3.setDividerSize(0);
            splitPane3.getBottomComponent().setVisible(false);
        }

        splitPane1.getLeftComponent().setVisible(showFileList);
        splitPane1.getRightComponent().setVisible(showHelp || showNotes);

        // If both helpnotes and filelist are visible, show the separator for splitPane1
        int pane1MinWidth = 0;
        if ((showHelp || showNotes) && showFileList) {
            splitPane1.setDividerSize(5);
            splitPane1.setDividerLocation(0.5);
            pane1MinWidth = HelpNotesPanel.getMinimumWidth() + FileListPanel.getMinimumWidth();
        } else {
            splitPane1.setDividerSize(0);
            if (showHelp || showNotes)
                pane1MinWidth = HelpNotesPanel.getMinimumWidth();
            else
                pane1MinWidth = FileListPanel.getMinimumWidth();
        }
        splitPane1.setMinimumSize(new Dimension(pane1MinWidth, 1));

        // If either the helpnotes or filelist is visible, show the separator for
        // splitPane2
        if (showHelp || showNotes || showFileList) {
            splitPane2.setDividerSize(5);
            splitPane2.getRightComponent().setVisible(true);
            splitPane2.setDividerLocation(0.5);
            splitPane2.setMinimumSize(new Dimension(ParametersPanel.getMinimumWidth() + pane1MinWidth, 1));
        } else {
            splitPane2.setDividerSize(0);
            splitPane2.getRightComponent().setVisible(false);
            splitPane2.setMinimumSize(new Dimension(ParametersPanel.getMinimumWidth(), 1));
        }
    }

    @Override
    public int getPreferredWidth() {
        int currentWidth = EditingControlPanel.getMinimumWidth() + ModulePanel.getMinimumWidth()
                + ParametersPanel.getPreferredWidth();

        // if (showSearch)
        //     currentWidth = currentWidth + SearchPanel.getMinimumWidth(); // Fixed width
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
        //     currentWidth = currentWidth + SearchPanel.getMinimumWidth();
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
        fileListPanel.updatePanel();
    }

    @Override
    public void resetJobNumbers() {
        fileListPanel.resetJobNumbers();
    }

    @Override
    public void updateAvailableModules() {
        editingControlPanel.listAvailableModules();
    }

    @Override
    public void updateModules() {
        modulesPanel.updatePanel();
        parametersPanel.updatePanel(GUI.getFirstSelectedModule());

    }

    @Override
    public void updateModuleStates() {
        modulesPanel.updateModuleStates();

    }

    @Override
    public void updateParameters() {
        parametersPanel.updatePanel(GUI.getFirstSelectedModule());
    }

    @Override
    public void updateHelpNotes() {
        helpNotesPanel.showHelp(showHelp);
        helpNotesPanel.showNotes(showNotes);
        helpNotesPanel.setVisible(showHelp || showNotes);
        helpNotesPanel.updatePanel(GUI.getFirstSelectedModule(), lastHelpNotesModule);
    }

    @Override
    public void updateFileList() {
        fileListPanel.setVisible(showFileList);
        fileListPanel.updatePanel();
    }

    @Override
    public void updateSearch() {
        searchPanel.setVisible(showSearch);
        searchPanel.updatePanel();
    }

    @Override
    public boolean showHelp() {
        return showHelp;
    }

    @Override
    public void setShowHelp(boolean showHelp) {
        this.showHelp = showHelp;
        Prefs.set("MIA.showEditingHelp", showHelp);

        helpNotesPanel.showHelp(showHelp);
        GUI.updatePanel();

        updateSeparators();

    }

    @Override
    public boolean showNotes() {
        return showNotes;
    }

    @Override
    public void setShowNotes(boolean showNotes) {
        this.showNotes = showNotes;
        Prefs.set("MIA.showEditingNotes", showNotes);

        helpNotesPanel.showNotes(showNotes);
        GUI.updatePanel();

        updateSeparators();

    }

    @Override
    public boolean showFileList() {
        return showFileList;
    }

    @Override
    public void setShowFileList(boolean showFileList) {
        this.showFileList = showFileList;
        Prefs.set("MIA.showEditingFileList", showFileList);

        fileListPanel.setVisible(showFileList);
        GUI.updatePanel();

        updateSeparators();

    }

    @Override
    public boolean showSearch() {
        return showSearch;
    }

    @Override
    public void setShowSearch(boolean showSearch) {
        this.showSearch = showSearch;
        Prefs.set("MIA.showEditingSearch", showSearch);

        searchPanel.setVisible(showSearch);
        GUI.updatePanel();

        updateSeparators();

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
