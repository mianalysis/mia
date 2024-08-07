package io.github.mianalysis.mia.gui.regions.processingpanel;

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
import io.github.mianalysis.mia.gui.regions.abstrakt.AnalysisControlButton;
import io.github.mianalysis.mia.gui.regions.extrapanels.ExtraPanel;
import io.github.mianalysis.mia.gui.regions.extrapanels.filelist.FileListPanel;
import io.github.mianalysis.mia.gui.regions.extrapanels.helpandnotes.HelpNotesPanel;
import io.github.mianalysis.mia.gui.regions.progressandstatus.StatusPanel;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.system.Colours;
import io.github.mianalysis.mia.object.system.SwingPreferences;

public class ProcessingPanel extends AbstractPanel {
    /**
     *
     */
    private static final long serialVersionUID = -1825169366002822113L;
    private static int frameHeight = GUI.getFrameHeight();
    private static int minimumFrameHeight = GUI.getMinimumFrameHeight();

    private static final ProcessingControlPanel controlPanel = new ProcessingControlPanel();
    private static ExtraPanel extraPanel = new ExtraPanel();
    private final StatusPanel statusPanel = new StatusPanel();
    private final JSplitPane splitPane;

    private static boolean showHelp = Prefs.get("MIA.showProcessingHelp", false);
    private static boolean showNotes = Prefs.get("MIA.showProcessingNotes", false);
    private boolean showFileList = Prefs.get("MIA.showProcessingFileList", false);
    private static Module lastHelpNotesModule = null;

    public ProcessingPanel() {
        boolean isDark = ((SwingPreferences) MIA.getPreferences()).darkThemeEnabled();
        setBackground(Colours.getLightGrey(isDark));
        setLayout(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();

        // Initialising the control panel
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;

        if (SystemInfo.isMacFullWindowContentSupported) {
            JPanel titleBar = new JPanel();
            titleBar.setOpaque(false);
            titleBar.setPreferredSize(new Dimension(100, 33));
            if (isDark)
                titleBar.setBorder(new FlatDropShadowBorder(new Color(42, 42, 42), new Insets(0, 0, 5, 0), 1));
            else
                titleBar.setBorder(new FlatDropShadowBorder(Color.GRAY, new Insets(0, 0, 5, 0), 1));
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.NORTH;
            add(titleBar, c);

            JPanel titleBarColour = new JPanel();
            titleBarColour.setBackground(Colours.getBlue(false));
            titleBarColour.setPreferredSize(new Dimension(100, 28));
            add(titleBarColour, c);
            c.gridy++;
        }
        
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        add(new ShadowPanel(initialiseProcessingControlPanel()), c);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new ShadowPanel(controlPanel),
                new ShadowPanel(extraPanel));
        splitPane.setPreferredSize(new Dimension(1, 1));
        splitPane.setBorder(null);
        splitPane.setDividerSize(5);
        splitPane.setDividerLocation(0.5);
        splitPane.setOpaque(false);
        splitPane.setOneTouchExpandable(true);
        splitPane.putClientProperty( "JSplitPane.expandableSide", "left" );

        c.gridy++;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 5, 5, 5);
        add(splitPane, c);

        // Initialising the status panel
        c.gridy++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 0;
        c.insets = new Insets(0, 5, 5, 5);
        add(new ShadowPanel(statusPanel), c);

        revalidate();
        repaint();

    }

    private static JPanel initialiseProcessingControlPanel() {
        int bigButtonSize = GUI.getBigButtonSize();
        int frameWidth = GUI.getFrameHeight();

        JPanel processingControlPanel = new JPanel();

        processingControlPanel.setMinimumSize(new Dimension(frameWidth - 30, bigButtonSize + 15));
        // processingControlPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        processingControlPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.FIRST_LINE_START;

        // Load analysis protocol button
        AnalysisControlButton loadModulesButton = new AnalysisControlButton(AnalysisControlButton.LOAD_MODULES,
                bigButtonSize);
        c.gridx++;
        c.anchor = GridBagConstraints.PAGE_END;
        processingControlPanel.add(loadModulesButton, c);

        // Save analysis protocol button
        AnalysisControlButton saveModulesButton = new AnalysisControlButton(AnalysisControlButton.SAVE_MODULES,
                bigButtonSize);
        c.gridx++;
        processingControlPanel.add(saveModulesButton, c);

        // Start analysis button
        AnalysisControlButton startAnalysisButton = new AnalysisControlButton(AnalysisControlButton.START_ANALYSIS,
                bigButtonSize);
        c.gridx++;
        c.weightx = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_END;
        processingControlPanel.add(startAnalysisButton, c);

        // Stop analysis button
        AnalysisControlButton stopAnalysisButton = new AnalysisControlButton(AnalysisControlButton.STOP_ANALYSIS,
                bigButtonSize);
        c.gridx++;
        c.weightx = 0;
        processingControlPanel.add(stopAnalysisButton, c);

        processingControlPanel.validate();
        processingControlPanel.repaint();

        return processingControlPanel;

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

        revalidate();
        repaint();

    }

    void updateSeparators() {
        // // If both helpnotes and filelist are visible, show the separator for splitPane1
        // int pane1MinWidth = 0;
        // if ((showHelp || showNotes) && showFileList) {
        //     splitPane1.setDividerSize(5);
        //     splitPane1.setDividerLocation(0.5);
        //     pane1MinWidth = HelpNotesPanel.getMinimumWidth() + FileListPanel.getMinimumWidth();
        // } else {
        //     splitPane1.setDividerSize(0);
        //     if (showHelp || showNotes)
        //         pane1MinWidth = HelpNotesPanel.getMinimumWidth();
        //     else
        //         pane1MinWidth = FileListPanel.getMinimumWidth();
        // }
        // splitPane1.setMinimumSize(new Dimension(pane1MinWidth, 1));

        // // If either the helpnotes or filelist is visible, show the separator for
        // // splitPane2
        // if (showHelp || showNotes || showFileList) {
        //     splitPane.setDividerSize(5);
        //     splitPane.getRightComponent().setVisible(true);
        //     splitPane.setDividerLocation(0.5);
        //     splitPane.setMinimumSize(new Dimension(ParametersPanel.getMinimumWidth() + pane1MinWidth, 1));
        // } else {
        //     splitPane.setDividerSize(0);
        //     splitPane.getRightComponent().setVisible(false);
        //     splitPane.setMinimumSize(new Dimension(ParametersPanel.getMinimumWidth(), 1));
        // }
    }

    @Override
    public void updateAvailableModules() {

    }

    @Override
    public void updateModules(boolean testAnalysis, @Nullable Module startModule) {
        controlPanel.updatePanel(testAnalysis, startModule);
    }

    @Override
    public void updateModuleStates() {
        controlPanel.updateButtonStates();
        // controlPanel.updatePanel();
    }

    @Override
    public void updateParameters(boolean testAnalysis, @Nullable Module startModule) {
        controlPanel.updatePanel(testAnalysis, startModule);
    }

    @Override
    public int getPreferredWidth() {
        int currentWidth = ProcessingControlPanel.getPreferredWidth();

        if (showHelp || showNotes)
            currentWidth = currentWidth + HelpNotesPanel.getPreferredWidth();
        if (showFileList)
            currentWidth = currentWidth + FileListPanel.getPreferredWidth();

        return currentWidth;

    }

    @Override
    public int getMinimumWidth() {
        int currentWidth = ProcessingControlPanel.getMinimumWidth();

        if (showHelp || showNotes)
            currentWidth = currentWidth + HelpNotesPanel.getPreferredWidth();
        if (showFileList)
            currentWidth = currentWidth + FileListPanel.getPreferredWidth();

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
    public boolean showHelp() {
        return showHelp;
    }

    @Override
    public void setShowHelp(boolean showHelp) {
        this.showHelp = showHelp;
        Prefs.set("MIA.showProcessingHelp", showHelp);
        Prefs.savePreferences();

        extraPanel.getHelpPanel().setVisible(showHelp);
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
        Prefs.set("MIA.showProcessingNotes", showNotes);
        Prefs.savePreferences();

        extraPanel.getNotesPanel().setVisible(showNotes);
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
        Prefs.set("MIA.showProcessingFileList", showFileList);
        Prefs.savePreferences();

        extraPanel.getFileListPanel().setVisible(showFileList);
        GUI.updatePanel();

        updateSeparators();
    }

    @Override
    public boolean showSearch() {
        return false;
    }

    @Override
    public void setShowSearch(boolean showSearch) {

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
