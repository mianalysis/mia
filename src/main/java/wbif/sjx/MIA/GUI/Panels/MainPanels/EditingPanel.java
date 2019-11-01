package wbif.sjx.MIA.GUI.Panels.MainPanels;

import ij.Prefs;
import wbif.sjx.MIA.GUI.ControlObjects.AnalysisControlButton;
import wbif.sjx.MIA.GUI.ControlObjects.ModuleControlButton;
import wbif.sjx.MIA.GUI.ControlObjects.ModuleListMenu;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Hidden.InputControl;
import wbif.sjx.MIA.Module.Hidden.OutputControl;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.GUI.Panels.*;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Process.AnalysisHandling.Analysis;
import wbif.sjx.MIA.Process.AnalysisHandling.AnalysisTester;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.util.*;

public class EditingPanel extends MainPanel {
    private static int frameWidth = 1100;
    private static int minimumFrameWidth = 800;
    private static int frameHeight = GUI.getFrameHeight();
    private static int minimumFrameHeight = GUI.getMinimumFrameHeight();

    private final EditingControlPanel editingControlPanel = new EditingControlPanel();
    private final ProgressBarPanel progressBarPanel = new ProgressBarPanel();
    private final InputOutputPanel inputPanel = new InputOutputPanel();
    private final InputOutputPanel outputPanel = new InputOutputPanel();
    private final ModulesPanel modulesPanel = new ModulesPanel();
    private final ParametersPanel parametersPanel = new ParametersPanel();
    private final JPanel helpNotesPanel = new JPanel();
    private final NotesPanel notesPanel = new NotesPanel();
    private final HelpPanel helpPanel = new HelpPanel();
    private final StatusPanel statusPanel = new StatusPanel();
    private final FileListPanel fileListPanel = new FileListPanel(GUI.getAnalysisRunner().getWorkspaces());

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
        c.gridwidth = 5;
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
        c.gridx++;
        c.gridy = 0;
        c.gridheight = 3;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5, 5, 5, 5);
        add(parametersPanel, c);

        updateFileList();
        c.gridx++;
        c.weightx = 0;
        c.insets = new Insets(5,0,5,5);
        add(fileListPanel,c);

        initialiseHelpNotesPanels();
        updateHelpNotes();
        c.gridx++;
        add(helpNotesPanel,c);

    }




    private void initialiseHelpNotesPanels() {
        // Adding panels to combined JPanel
        helpNotesPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 1;
        c.insets = new Insets(0,0,5,0);
        helpNotesPanel.add(helpPanel,c);

        c.gridy++;
        c.insets = new Insets(0,0,0,0);
        helpNotesPanel.add(notesPanel,c);

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

        GUI.updateTestFile(false);
        updateModules();
        updateParameters();

        if (showHelp || showNotes) updateHelpNotes();
        if (showFileList) updateFileList();

        revalidate();
        repaint();

    }

    @Override
    public int getPreferredWidth() {
        int currentWidth = frameWidth;

        if (showHelp || showNotes) {
            currentWidth = currentWidth + 315;
        }
        if (showFileList) {
            currentWidth = currentWidth + 315;
        }

        return currentWidth;

    }

    @Override
    public int getMinimumWidth() {
        int currentWidth = frameWidth;

        if (showHelp || showNotes) currentWidth = currentWidth + 315;
        if (showFileList) currentWidth = currentWidth + 315;

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
        helpPanel.setVisible(showHelp);
        notesPanel.setVisible(showNotes);
        helpNotesPanel.setVisible(showHelp || showNotes);

        // Only update the help and notes if the module has changed
        Module activeModule = GUI.getFirstSelectedModule();

        // If null, show a special message
        if (activeModule == null) {
            helpPanel.showUsageMessage();
            notesPanel.showUsageMessage();
            return;
        }

        if (activeModule != lastHelpNotesModule) {
            lastHelpNotesModule = activeModule;
            helpPanel.updatePanel();
            notesPanel.updatePanel();
        }
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

        helpNotesPanel.setVisible(showHelp);
        GUI.updatePanel();

    }

    @Override
    public boolean showNotes() {
        return showNotes;
    }

    @Override
    public void setShowNotes(boolean showNotes) {
        this.showNotes = showNotes;
        Prefs.set("MIA.showEditingNotes",showNotes);

        helpNotesPanel.setVisible(showNotes);
        GUI.updatePanel();

    }

    @Override
    public boolean showFileList() {
        return showFileList;
    }

    @Override
    public void setShowFileList(boolean showFileList) {
        this.showFileList = showFileList;
        Prefs.set("MIA.showEditingFileList",showFileList);

        fileListPanel.setVerifyInputWhenFocusTarget(showFileList);
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
