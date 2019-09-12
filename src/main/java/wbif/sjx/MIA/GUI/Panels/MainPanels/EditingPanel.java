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

    private ModuleControlButton addModuleButton = null;
    private static final JPopupMenu moduleListMenu = new JPopupMenu();

    private final ProgressBarPanel progressBarPanel = new ProgressBarPanel();
    private final InputOutputPanel inputPanel = new InputOutputPanel();
    private final InputOutputPanel outputPanel = new InputOutputPanel();
    private final ModulesPanel modulesPanel = new ModulesPanel();
    private final ParametersPanel parametersPanel = new ParametersPanel();
    private final JPanel helpNotesPanel = new JPanel();
    private final NotesPanel notesPanel = new NotesPanel();
    private final HelpPanel helpPanel = new HelpPanel();
    private final StatusPanel statusPanel = new StatusPanel();
    private final FileListPanel fileListPanel = new FileListPanel();

    private boolean showHelp = Prefs.get("MIA.showEditingHelp",false);
    private boolean showNotes = Prefs.get("MIA.showEditingNotes",false);
    private boolean showFileList = Prefs.get("MIA.showFileList",false);
    private Module lastHelpNotesModule = null;


    public EditingPanel() {
        addModuleButton = new ModuleControlButton(ModuleControlButton.ADD_MODULE,GUI.getBigButtonSize(),moduleListMenu);
        listAvailableModules();

        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 0);
        c.gridx = 0;
        c.gridy = 0;

        // Creating buttons to addRef and remove modules
        JPanel controlPanel = initialiseControlPanel();
        c.weightx = 0;
        c.weighty = 1;
        c.gridheight = 3;
        c.fill = GridBagConstraints.VERTICAL;
        add(controlPanel, c);

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


    private JPanel initialiseControlPanel() {
        int bigButtonSize = GUI.getBigButtonSize();
        int frameHeight = GUI.getFrameHeight();
        int statusHeight = GUI.getStatusHeight();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0;
        c.insets = new Insets(5, 5, 0, 5);
        c.anchor = GridBagConstraints.PAGE_START;

        JPanel controlPanel = new JPanel();
        controlPanel.setMaximumSize(new Dimension(bigButtonSize + 20, Integer.MAX_VALUE));
        controlPanel.setMinimumSize(new Dimension(bigButtonSize + 20, frameHeight - statusHeight-350));
        controlPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        controlPanel.setLayout(new GridBagLayout());

        // Add module button
        addModuleButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        controlPanel.add(addModuleButton, c);

        // Remove module button
        ModuleControlButton removeModuleButton = new ModuleControlButton(ModuleControlButton.REMOVE_MODULE,bigButtonSize,moduleListMenu);
        removeModuleButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        c.gridy++;
        controlPanel.add(removeModuleButton, c);

        // Move module up button
        ModuleControlButton moveModuleUpButton = new ModuleControlButton(ModuleControlButton.MOVE_MODULE_UP,bigButtonSize,moduleListMenu);
        moveModuleUpButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        c.gridy++;
        controlPanel.add(moveModuleUpButton, c);

        // Move module down button
        ModuleControlButton moveModuleDownButton = new ModuleControlButton(ModuleControlButton.MOVE_MODULE_DOWN,bigButtonSize,moduleListMenu);
        moveModuleDownButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        c.gridy++;
        controlPanel.add(moveModuleDownButton, c);

        // Load analysis protocol button
        AnalysisControlButton loadAnalysisButton = new AnalysisControlButton(AnalysisControlButton.LOAD_ANALYSIS,bigButtonSize);
        c.gridy++;
        c.weighty = 1;
        c.anchor = GridBagConstraints.PAGE_END;
        controlPanel.add(loadAnalysisButton, c);

        // Save analysis protocol button
        AnalysisControlButton saveAnalysisButton = new AnalysisControlButton(AnalysisControlButton.SAVE_ANALYSIS,bigButtonSize);
        c.gridy++;
        c.weighty = 0;
        controlPanel.add(saveAnalysisButton, c);

        // Start analysis button
        AnalysisControlButton startAnalysisButton = new AnalysisControlButton(AnalysisControlButton.START_ANALYSIS,bigButtonSize);
        c.gridy++;
        controlPanel.add(startAnalysisButton, c);

        // Stop analysis button
        AnalysisControlButton stopAnalysisButton = new AnalysisControlButton(AnalysisControlButton.STOP_ANALYSIS,bigButtonSize);
        c.gridy++;
        c.insets = new Insets(5, 5, 5, 5);
        controlPanel.add(stopAnalysisButton, c);

        controlPanel.validate();
        controlPanel.repaint();

        return controlPanel;

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

    private void listAvailableModules() {
        addModuleButton.setEnabled(false);
        addModuleButton.setToolTipText("Loading modules");

        TreeMap<String,Module> availableModules = GUI.getAvailableModules();
        TreeSet<ModuleListMenu> topList = new TreeSet<>();
        TreeSet<String> moduleNames = new TreeSet<>();
        moduleNames.addAll(availableModules.keySet());

        for (String name : moduleNames) {
            // ActiveList starts at the top list
            TreeSet<ModuleListMenu> activeList = topList;
            ModuleListMenu activeItem = null;

            String[] names = name.split("\\\\");
            for (int i = 0; i < names.length-1; i++) {
                boolean found = false;
                for (ModuleListMenu listItemm : activeList) {
                    if (listItemm.getName().equals(names[i])) {
                        activeItem = listItemm;
                        found = true;
                    }
                }

                if (!found) {
                    ModuleListMenu newItem = new ModuleListMenu(names[i], new ArrayList<>(),moduleListMenu);
                    newItem.setName(names[i]);
                    activeList.add(newItem);
                    if (activeItem != null) activeItem.add(newItem);
                    activeItem = newItem;
                }

                activeList = activeItem.getChildren();

            }

            if (activeItem != null) activeItem.addMenuItem(availableModules.get(name));

        }

        for (ModuleListMenu listMenu : topList) moduleListMenu.add(listMenu);

        addModuleButton.setToolTipText("Add module");
        addModuleButton.setEnabled(true);

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
//        modulesPanel.updateButtonStates();
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
