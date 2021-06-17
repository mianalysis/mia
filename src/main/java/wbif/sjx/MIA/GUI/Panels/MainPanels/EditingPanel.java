package wbif.sjx.MIA.GUI.Panels.MainPanels;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import ij.Prefs;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.GUI.Panels.EditingControlPanel;
import wbif.sjx.MIA.GUI.Panels.FileListPanel;
import wbif.sjx.MIA.GUI.Panels.HelpNotesPanel;
import wbif.sjx.MIA.GUI.Panels.InputOutputPanel;
import wbif.sjx.MIA.GUI.Panels.ModulesPanel;
import wbif.sjx.MIA.GUI.Panels.ParametersPanel;
import wbif.sjx.MIA.GUI.Panels.ProgressBarPanel;
import wbif.sjx.MIA.GUI.Panels.StatusPanel;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.Core.InputControl;
import wbif.sjx.MIA.Module.Core.OutputControl;
import wbif.sjx.MIA.Process.AnalysisHandling.Analysis;
import wbif.sjx.MIA.Process.AnalysisHandling.AnalysisTester;

public class EditingPanel extends MainPanel {
    /**
     *
     */
    private static final long serialVersionUID = -6063268799004206526L;
    private static int frameHeight = GUI.getFrameHeight();
    private static int minimumFrameHeight = GUI.getMinimumFrameHeight();

    private final EditingControlPanel editingControlPanel = new EditingControlPanel();
    private final ProgressBarPanel progressBarPanel = new ProgressBarPanel();
    private final InputOutputPanel inputPanel = new InputOutputPanel();
    private final InputOutputPanel outputPanel = new InputOutputPanel();
    private final ModulesPanel modulesPanel = new ModulesPanel();
    private final ParametersPanel parametersPanel = new ParametersPanel();
    private final HelpNotesPanel helpNotesPanel = new HelpNotesPanel();
    private final StatusPanel statusPanel = new StatusPanel();
    private final FileListPanel fileListPanel = new FileListPanel(GUI.getAnalysisRunner().getWorkspaces());
    private final JSplitPane splitPane1;
    private final JSplitPane splitPane2;

    private boolean showHelp = Prefs.get("MIA.showEditingHelp",false);
    private boolean showNotes = Prefs.get("MIA.showEditingNotes",false);
    private boolean showFileList = Prefs.get("MIA.showEditingFileList",false);
    private Module lastHelpNotesModule = null;


    public EditingPanel() {
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 0);
        c.gridx = 0;
        c.gridy = 0;

        // Creating buttons to addRef and remove modules
        c.weightx = 0;
        c.weighty = 1;
        c.gridheight = 3;
        c.fill = GridBagConstraints.VERTICAL;
        add(editingControlPanel, c);

        // Initialising the status panel
        c.gridheight = 1;
        c.gridy++;
        c.gridy++;
        c.gridy++;
        c.weighty = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 3;
        c.insets = new Insets(0,5,5,5);
        add(statusPanel, c);

        // Initialising the progress bar
        c.gridy++;
        c.insets = new Insets(0,5,5,5);
        add(progressBarPanel,c);

        // Initialising the input enable panel
        c.gridx++;
        c.gridy = 0;
        c.weightx = 0;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 0, 0);
        add(inputPanel, c);

        // Initialising the module list panel
        c.gridy++;
        c.weighty = 1;
        c.fill = GridBagConstraints.VERTICAL;
        add(modulesPanel, c);

        // Initialising the output enable panel
        c.gridy++;
        c.gridheight = 1;
        c.weighty = 0;
        c.insets = new Insets(5, 5, 5, 0);
        add(outputPanel, c);

        // Initialising the parameters panel
        updateFileList();
        updateHelpNotes();

        splitPane1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,fileListPanel,helpNotesPanel);
        splitPane1.setPreferredSize(new Dimension(1,1));
        splitPane1.setBorder(null);
        splitPane1.setDividerSize(5);
        splitPane1.setDividerLocation(0.5);
        BasicSplitPaneUI splitPaneUI = (BasicSplitPaneUI) splitPane1.getUI();
        splitPaneUI.getDivider().setBorder(new EmptyBorder(0,0,0,0));

        splitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,parametersPanel,splitPane1);
        splitPane2.setPreferredSize(new Dimension(1,1));
        splitPane2.setBorder(null);
        splitPane2.setDividerSize(5);
        splitPane2.setDividerLocation(0.5);
        splitPaneUI = (BasicSplitPaneUI) splitPane2.getUI();
        splitPaneUI.getDivider().setBorder(new EmptyBorder(0,0,0,0));

        c.gridx++;
        c.gridy = 0;
        c.gridheight = 3;
        c.gridwidth = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5, 5, 5, 5);
        add(splitPane2,c);

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
        c.insets = new Insets(0,5,0,0);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH;

        statusPanel.add(GUI.getTextField(),c);

        updateModules();
        updateParameters();

        if (showHelp || showNotes) updateHelpNotes();
        if (showFileList) updateFileList();

        revalidate();
        repaint();

    }

    void updateSeparators() {
        splitPane1.getLeftComponent().setVisible(showFileList);
        splitPane1.getRightComponent().setVisible(showHelp || showNotes);

        // If both helpnotes and filelist are visible, show the separator for splitPane1
        int pane1MinWidth = 0;
        if ((showHelp || showNotes) && showFileList) {
            splitPane1.setDividerSize(5);
            splitPane1.setDividerLocation(0.5);
            pane1MinWidth = HelpNotesPanel.getMinimumWidth()+FileListPanel.getMinimumWidth();
        } else {
            splitPane1.setDividerSize(0);
            if (showHelp || showNotes) pane1MinWidth = HelpNotesPanel.getMinimumWidth();
            else pane1MinWidth = FileListPanel.getMinimumWidth();
        }
        splitPane1.setMinimumSize(new Dimension(pane1MinWidth,1));

        // If either the helpnotes or filelist is visible, show the separator for splitPane2
        if (showHelp || showNotes || showFileList) {
            splitPane2.setDividerSize(5);
            splitPane2.getRightComponent().setVisible(true);
            splitPane2.setDividerLocation(0.5);
            splitPane2.setMinimumSize(new Dimension(ParametersPanel.getMinimumWidth()+pane1MinWidth,1));
        } else {
            splitPane2.setDividerSize(0);
            splitPane2.getRightComponent().setVisible(false);
            splitPane2.setMinimumSize(new Dimension(ParametersPanel.getMinimumWidth(),1));
        }
    }

    @Override
    public int getPreferredWidth() {
        int currentWidth = EditingControlPanel.getMinimumWidth()
                + ModulesPanel.getMinimumWidth()
                + ParametersPanel.getPreferredWidth();

        if (showHelp || showNotes) currentWidth = currentWidth + HelpNotesPanel.getPreferredWidth();
        if (showFileList) currentWidth = currentWidth + FileListPanel.getPreferredWidth();

        return currentWidth;

    }

    @Override
    public int getMinimumWidth() {
        int currentWidth = EditingControlPanel.getMinimumWidth()
                + ModulesPanel.getMinimumWidth()
                + ParametersPanel.getMinimumWidth();

        if (showHelp || showNotes) currentWidth = currentWidth + HelpNotesPanel.getMinimumWidth();
        if (showFileList) currentWidth = currentWidth + FileListPanel.getMinimumWidth();

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
    public void updateModules() {
        Analysis analysis = GUI.getAnalysis();
        InputControl inputControl = analysis.getModules().getInputControl();
        OutputControl outputControl = analysis.getModules().getOutputControl();

        boolean runnable = AnalysisTester.testModule(inputControl,analysis.getModules());
        inputControl.setRunnable(runnable);
        inputPanel.updateButtonState();
        inputPanel.updatePanel(inputControl);

        runnable = AnalysisTester.testModule(outputControl,analysis.getModules());
        outputControl.setRunnable(runnable);
        outputPanel.updateButtonState();
        outputPanel.updatePanel(outputControl);

        parametersPanel.updatePanel(GUI.getFirstSelectedModule());
        modulesPanel.updateButtonStates();
        modulesPanel.updatePanel();

    }

    @Override
    public void updateModuleStates() {
        inputPanel.updateButtonState();
        modulesPanel.updateButtonStates();
        outputPanel.updateButtonState();
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
        helpNotesPanel.updatePanel(GUI.getFirstSelectedModule(),lastHelpNotesModule);
    }

    @Override
    public void updateFileList() {
        fileListPanel.setVisible(showFileList);
        fileListPanel.updatePanel();
    }

    @Override
    public boolean showHelp() {
        return showHelp;
    }

    @Override
    public void setShowHelp(boolean showHelp) {
        this.showHelp = showHelp;
        Prefs.set("MIA.showEditingHelp",showHelp);

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
        Prefs.set("MIA.showEditingNotes",showNotes);

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
        Prefs.set("MIA.showEditingFileList",showFileList);

        fileListPanel.setVisible(showFileList);
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
