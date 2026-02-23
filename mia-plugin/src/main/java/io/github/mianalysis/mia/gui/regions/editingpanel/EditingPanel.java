package io.github.mianalysis.mia.gui.regions.editingpanel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.drew.lang.annotations.Nullable;
import com.formdev.flatlaf.util.SystemInfo;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.gui.regions.ShadowPanel;
import io.github.mianalysis.mia.gui.regions.abstrakt.AbstractPanel;
import io.github.mianalysis.mia.gui.regions.extrapanels.ExtraPanel;
import io.github.mianalysis.mia.gui.regions.parameterlist.ParametersPanel;
import io.github.mianalysis.mia.gui.regions.progressandstatus.StatusPanel;
import io.github.mianalysis.mia.gui.regions.workflowmodules.ModulePanel;
import io.github.mianalysis.mia.module.ModuleI;
import io.github.mianalysis.mia.object.system.Colours;
import io.github.mianalysis.mia.object.system.SwingPreferences;
import io.github.mianalysis.mia.process.analysishandling.AnalysisTester;

public class EditingPanel extends AbstractPanel {
    private static final long serialVersionUID = -6063268799004206526L;
    private static int frameHeight = GUI.getFrameHeight();
    private static int minimumFrameHeight = GUI.getMinimumFrameHeight();

    private final EditingControlPanel editingControlPanel = new EditingControlPanel();
    private final ModulePanel modulesPanel = new ModulePanel();
    private final ParametersPanel parametersPanel = new ParametersPanel();
    private final ExtraPanel extraPanel = new ExtraPanel();
    private final ShadowPanel shadowExtraPanel = new ShadowPanel(extraPanel);
    private final StatusPanel statusPanel = new StatusPanel();
    private final JSplitPane splitPane;

    private ModuleI lastHelpNotesModule = null;

    public EditingPanel() {
        boolean isDark = ((SwingPreferences) MIA.getPreferences()).darkThemeEnabled();
        setBackground(Colours.getLightGrey(isDark));

        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 5;

        if (SystemInfo.isMacFullWindowContentSupported) {
            JPanel titleBarColour = new JPanel();
            titleBarColour.setBackground(Colours.getLightBlue(false));
            titleBarColour.setPreferredSize(new Dimension(Integer.MAX_VALUE, 28));
            titleBarColour.setMinimumSize(new Dimension(100, 28));

            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.NORTH;
            add(titleBarColour, c);

            // JPanel titleBar = new JPanel();
            // titleBar.setOpaque(false);
            // titleBar.setMinimumSize(new Dimension(Integer.MAX_VALUE, 33));
            // titleBar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 33));

            // if (isDark)
            //     titleBar.setBorder(new FlatDropShadowBorder(new Color(42, 42, 42), new Insets(0, 0, 6, 0), 1));
            // else
            //     titleBar.setBorder(new FlatDropShadowBorder(Color.GRAY, new Insets(0, 0, 6, 0), 1));
            
            // add(titleBar, c);

            c.gridy++;

        }

        c.weighty = 1;
        c.gridwidth = 1;
        c.insets = new Insets(5, 10, 5, 0);
        c.fill = GridBagConstraints.BOTH;
        add(new ShadowPanel(editingControlPanel), c);

        // Initialising the status panel
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 5;
        c.gridy++;
        c.insets = new Insets(0, 10, 5, 5);
        add(new ShadowPanel(statusPanel), c);

        c.gridx++;
        c.gridy = 1;
        c.gridwidth = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.VERTICAL;
        c.insets = new Insets(5, 5, 5, 0);
        modulesPanel.setOpaque(false);
        add(new ShadowPanel(modulesPanel), c);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new ShadowPanel(parametersPanel),
                shadowExtraPanel);
        splitPane.setPreferredSize(new Dimension(1, 1));
        splitPane.setDividerSize(5);
        splitPane.setDividerLocation(0.5);
        splitPane.setOpaque(false);
        splitPane.putClientProperty("JSplitPane.expandableSide", "left");

        c.gridx++;
        c.gridy = 1;
        c.gridwidth = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5, 5, 5, 5);
        add(splitPane, c);

        setShowSidebar(GUI.showSidebar());

    }

    @Override
    public void updatePanel(boolean testAnalysis, @Nullable ModuleI startModule) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(0, 10, 0, 0);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH;

        statusPanel.add(GUI.getTextField(), c);

        extraPanel.setShowSearch(true);
        setShowSidebar(GUI.showSidebar());
        updateModules(testAnalysis, startModule);
        updateParameters(testAnalysis, startModule);

        revalidate();
        repaint();

    }

    @Override
    public int getPreferredWidth() {
        int currentWidth = EditingControlPanel.getMinimumWidth() + ModulePanel.getMinimumWidth()
                + ParametersPanel.getPreferredWidth();

        if (GUI.showSidebar())
            currentWidth = currentWidth + ExtraPanel.getPreferredWidth();

        return currentWidth;

    }

    @Override
    public int getMinimumWidth() {
        int currentWidth = EditingControlPanel.getMinimumWidth() + ModulePanel.getMinimumWidth()
                + ParametersPanel.getMinimumWidth();

        if (GUI.showSidebar())
            currentWidth = currentWidth + ExtraPanel.getMinimumWidth();

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
    public void updateModules(boolean testAnalysis, @Nullable ModuleI startModule) {
        if (testAnalysis)
            AnalysisTester.testModules(GUI.getModules(), GUI.getTestWorkspace(), startModule);

        modulesPanel.updatePanel();

    }

    @Override
    public void updateModuleStates() {
        modulesPanel.updateModuleStates();

    }

    @Override
    public void updateParameters(boolean testAnalysis, @Nullable ModuleI startModule) {
        parametersPanel.updatePanel(GUI.getFirstSelectedModule());
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
    public ModuleI getLastHelpNotesModule() {
        return lastHelpNotesModule;
    }

    @Override
    public void setLastHelpNotesModule(ModuleI module) {
        lastHelpNotesModule = module;
    }
}
