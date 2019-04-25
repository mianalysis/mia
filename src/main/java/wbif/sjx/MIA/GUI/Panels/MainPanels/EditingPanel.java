package wbif.sjx.MIA.GUI.Panels.MainPanels;

import ij.Prefs;
import wbif.sjx.MIA.GUI.ControlObjects.AnalysisControlButton;
import wbif.sjx.MIA.GUI.ControlObjects.ModuleControlButton;
import wbif.sjx.MIA.GUI.ControlObjects.ModuleListMenu;
import wbif.sjx.MIA.GUI.InputOutput.InputControl;
import wbif.sjx.MIA.GUI.InputOutput.OutputControl;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.GUI.Panels.*;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Process.AnalysisHandling.Analysis;
import wbif.sjx.MIA.Process.AnalysisHandling.AnalysisTester;
import wbif.sjx.MIA.Process.ClassHunter;

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
    private static final ButtonGroup moduleGroup = new ButtonGroup();

    private final ProgressBarPanel progressBarPanel = new ProgressBarPanel();
    private final InputOutputPanel inputPanel = new InputOutputPanel(moduleGroup);
    private final InputOutputPanel outputPanel = new InputOutputPanel(moduleGroup);
    private final ModulesPanel modulesPanel = new ModulesPanel(moduleGroup);
    private final ParametersPanel parametersPanel = new ParametersPanel();
    private final JPanel helpNotesPanel = new JPanel();
    private final NotesPanel notesPanel = new NotesPanel();
    private final HelpPanel helpPanel = new HelpPanel();
    private final StatusPanel statusPanel = new StatusPanel();

    private boolean showHelpNotes = Prefs.get("MIA.showEditingHelpNotes",false);
    private Module lastHelpNotesModule = null;


    public EditingPanel() {
        // Starting this process, as it takes longest
        new Thread(new Runnable() {
            @Override
            public void run() {
                listAvailableModules();
            }
        }).start();

        addModuleButton = new ModuleControlButton(ModuleControlButton.ADD_MODULE,GUI.getBigButtonSize(),moduleListMenu);

        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 0);
        c.gridx = 0;
        c.gridy = 0;

        // Creating buttons to add and remove modules
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
        c.gridwidth = 4;
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

        initialiseHelpNotesPanels();
        c.gridx++;
        c.weightx = 0;
        c.insets = new Insets(5,0,5,5);
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
        GridBagConstraints cc = new GridBagConstraints();

        cc.fill = GridBagConstraints.BOTH;
        cc.gridx = 0;
        cc.gridy = 0;
        cc.weightx = 1;
        cc.weighty = 2;
        cc.insets = new Insets(0,0,5,0);
        helpNotesPanel.add(helpPanel,cc);

        cc.gridy++;
        cc.weighty = 1;
        cc.insets = new Insets(0,0,0,0);
        helpNotesPanel.add(notesPanel,cc);

    }

    private void listAvailableModules() {
        try {
            // Waiting until the add module button has been created
            while (addModuleButton == null) Thread.sleep(100);

                addModuleButton.setEnabled(false);
                addModuleButton.setToolTipText("Loading modules");

            Set<Class<? extends Module>> availableModules = new ClassHunter<Module>().getClasses(Module.class,MIA.isDebug());

            Comparator<String> comparator = new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    // Getting the normal order
                    int order = o1.compareTo(o2);

                    // Rank names with more slashes higher
                    int l1 = o1.split("\\\\").length;
                    int l2 = o2.split("\\\\").length;
                    if (l2 < l1) {
                        order = -order;
                    }

                    return order;

                }
            };

            // Creating an alphabetically-ordered list of all modules
            TreeMap<String, Class> modules = new TreeMap<>();
            for (Class clazz : availableModules) {
                if (clazz != InputControl.class && clazz != OutputControl.class) {
                    Module module = (Module) clazz.newInstance();
                    String packageName = module.getPackageName();
                    String moduleName = module.getTitle();
                    modules.put(packageName+moduleName, clazz);
                }
            }

            TreeSet<ModuleListMenu> topList = new TreeSet<>();
            TreeSet<String> moduleNames = new TreeSet<>(comparator);
            moduleNames.addAll(modules.keySet());

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

                Module module = (Module) modules.get(name).newInstance();
                if (module != null && activeItem != null) activeItem.addMenuItem(module);

            }

            for (ModuleListMenu listMenu : topList) moduleListMenu.add(listMenu);

        } catch (IllegalAccessException | InstantiationException | InterruptedException e){
            e.printStackTrace(System.err);
        }

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

        statusPanel.add(GUI.getTextField(),c);
        helpNotesPanel.setVisible(showHelpNotes);

        GUI.updateTestFile();
        updateModules();
        updateParameters();

        if (showHelpNotes) updateHelpNotes();

        revalidate();
        repaint();

    }

    @Override
    public int getPreferredWidth() {
        if (showHelpNotes) {
            return frameWidth + 315;
        } else {
            return frameWidth;
        }
    }

    @Override
    public int getMinimumWidth() {
        if (showHelpNotes) {
            return minimumFrameWidth + 315;
        } else {
            return minimumFrameWidth;
        }
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

        boolean runnable = AnalysisTester.testModule(analysis.getInputControl(),analysis.getModules());
        analysis.getInputControl().setRunnable(runnable);
        inputPanel.updateButtonState();
        inputPanel.updatePanel(analysis.getInputControl());

        runnable = AnalysisTester.testModule(analysis.getOutputControl(),analysis.getModules());
        analysis.getInputControl().setRunnable(runnable);
        outputPanel.updateButtonState();
        outputPanel.updatePanel(analysis.getOutputControl());

        parametersPanel.updatePanel(GUI.getActiveModule());
        modulesPanel.updateButtonStates();
        modulesPanel.updatePanel();

    }

    @Override
    public void updateModuleStates(boolean verbose) {
        int nRunnable = AnalysisTester.testModules(GUI.getModules());
        int nActive = 0;
        for (Module module:GUI.getModules()) if (module.isEnabled()) nActive++;
        int nModules = GUI.getModules().size();
        if (verbose && nModules > 0) System.out.println(nRunnable+" of "+nActive+" active modules are runnable");
        modulesPanel.updateButtonStates();

    }

    @Override
    public void updateParameters() {
        parametersPanel.updatePanel(GUI.getActiveModule());
    }

    @Override
    public void updateHelpNotes() {
        // Only update the help and notes if the module has changed
        Module activeModule = GUI.getActiveModule();

        if (activeModule != lastHelpNotesModule) {
            lastHelpNotesModule = activeModule;
        } else {
            return;
        }

        helpPanel.updatePanel();
        notesPanel.updatePanel();

    }

    @Override
    public boolean showHelpNotes() {
        return showHelpNotes;
    }

    @Override
    public void setShowHelpNotes(boolean showHelpNotes) {
        this.showHelpNotes = showHelpNotes;
        Prefs.set("MIA.showEditingHelpNotes",showHelpNotes);

        helpNotesPanel.setVisible(showHelpNotes);
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
