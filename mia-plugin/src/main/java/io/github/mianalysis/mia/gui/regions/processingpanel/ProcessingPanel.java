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
    private static ShadowPanel shadowExtraPanel = new ShadowPanel(extraPanel);
    private final StatusPanel statusPanel = new StatusPanel();
    private final JSplitPane splitPane;

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
        
        c.insets = new Insets(5, 10, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        add(new ShadowPanel(initialiseProcessingControlPanel()), c);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new ShadowPanel(controlPanel),
            shadowExtraPanel);
        splitPane.setPreferredSize(new Dimension(1, 1));
        splitPane.setBorder(null);
        splitPane.setDividerSize(5);
        splitPane.setDividerLocation(0.5);
        splitPane.setOpaque(false);
        splitPane.putClientProperty( "JSplitPane.expandableSide", "left" );

        c.gridy++;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 10, 5, 5);
        add(splitPane, c);

        // Initialising the status panel
        c.gridy++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 0;
        c.insets = new Insets(0, 10, 5, 5);
        add(new ShadowPanel(statusPanel), c);

        shadowExtraPanel.setVisible(Prefs.get("MIA.showSidebar",true));
        
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

        setShowSidebar(GUI.showSidebar());
        extraPanel.setShowSearch(false);
        updateModules(testAnalysis, startModule);

        revalidate();
        repaint();

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

        if (GUI.showSidebar())
            currentWidth = currentWidth + ExtraPanel.getPreferredWidth();

        return currentWidth;

    }

    @Override
    public int getMinimumWidth() {
        int currentWidth = ProcessingControlPanel.getMinimumWidth();

        if (GUI.showSidebar())
            currentWidth = currentWidth + ExtraPanel.getPreferredWidth();

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
    public void setShowSidebar(boolean showSidebar) {
        shadowExtraPanel.setVisible(showSidebar);
        if (showSidebar)
            splitPane.setDividerSize(5);
        else
            splitPane.setDividerSize(0);
        GUI.updatePanel();

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
