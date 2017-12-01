// TODO: Add controls for all parameter types (hashsets, etc.)
// TODO: If an assigned image/object name is no longer available, flag up the module button in red
// TODO: Output panel could allow the user to select which objects and images to output to the spreadsheet

package wbif.sjx.ModularImageAnalysis.GUI.Layouts;

import org.reflections.Reflections;
import wbif.sjx.ModularImageAnalysis.GUI.*;
import wbif.sjx.ModularImageAnalysis.GUI.ControlObjects.*;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.InputControl;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.OutputControl;
import wbif.sjx.ModularImageAnalysis.GUI.ParameterControls.ModuleName;
import wbif.sjx.ModularImageAnalysis.Module.*;
import wbif.sjx.ModularImageAnalysis.Object.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;

import java.io.*;
import java.util.*;

/**
 * Created by Stephen on 20/05/2017.
 */
public class MainGUI extends GUI {
    private int mainFrameWidth = 1100;
    private int basicFrameWidth = 400;
    private int frameHeight = 750;
    private int elementHeight = 25;
    private int bigButtonSize = 40;
    private int moduleButtonWidth = 300;

    private GUIAnalysis analysis = new GUIAnalysis();
    private ComponentFactory componentFactory;
    private HCModule activeModule = null;
    private JFrame frame = new JFrame();
    private JMenuBar menuBar = new JMenuBar();
    private JMenu viewMenu = new JMenu("View");
    private JPanel controlPanel = new JPanel();
    private JPanel inputEnablePanel = new JPanel();
    private JPanel outputEnablePanel = new JPanel();
    private JPanel modulesPanel = new JPanel();
    private JScrollPane modulesScrollPane = new JScrollPane(modulesPanel);
    private JPanel paramsPanel = new JPanel();
    private JScrollPane paramsScrollPane = new JScrollPane(paramsPanel);
    private JPanel statusPanel = new JPanel();
    private JPanel basicControlPanel = new JPanel();
    private JPanel basicModulesPanel = new JPanel();
    private JScrollPane basicModulesScrollPane = new JScrollPane(basicModulesPanel);
    private JPopupMenu moduleListMenu = new JPopupMenu();
    private int lastModuleEval = -1;
    private boolean basicGUI = true;
    private boolean debugOn = false;

    public MainGUI(boolean debugOn) throws InstantiationException, IllegalAccessException {
        this.debugOn = debugOn;

        analysis.getInputControl().initialiseParameters();

        componentFactory = new ComponentFactory(this, elementHeight);

        // Setting location of panel
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width - mainFrameWidth) / 2, (screenSize.height - frameHeight) / 2);
        frame.setLayout(new GridBagLayout());
        frame.setTitle("Modular image analysis (version " + getClass().getPackage().getImplementationVersion() + ")");

        // Creating the menu bar
        initialiseMenuBar();
        frame.setJMenuBar(menuBar);

        if (debugOn) {
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            renderEditingMode();
        } else {
            renderBasicMode();
        }

        // Final bits for listeners
//        frame.addMouseListener(this);
        frame.setVisible(true);

        // Populating the list containing all available modules
        listAvailableModules();
        moduleListMenu.show(frame, 0, 0);
        moduleListMenu.setVisible(false);

    }

    private void initialiseMenuBar() {
        // Creating the file menu
        JMenu menu = new JMenu("File");
        menuBar.add(menu);
        menu.add(new AnalysisMenuItem(this, AnalysisMenuItem.LOAD_ANALYSIS));
        menu.add(new AnalysisMenuItem(this, AnalysisMenuItem.SAVE_ANALYSIS));

        // Creating the analysis menu
        menu = new JMenu("Analysis");
        menuBar.add(menu);
        menu.add(new AnalysisMenuItem(this, AnalysisMenuItem.SET_FILE_TO_ANALYSE));
        menu.add(new AnalysisMenuItem(this, AnalysisMenuItem.START_ANALYSIS));
        menu.add(new AnalysisMenuItem(this, AnalysisMenuItem.STOP_ANALYSIS));
        menu.add(new AnalysisMenuItem(this, AnalysisMenuItem.CLEAR_PIPELINE));

        // Creating the new menu
        menuBar.add(viewMenu);
        ButtonGroup group = new ButtonGroup();
        ViewControlButton rbMenuItem = new ViewControlButton(this, ViewControlButton.BASIC_MODE);
        rbMenuItem.setSelected(!debugOn);
        group.add(rbMenuItem);
        viewMenu.add(rbMenuItem);

        rbMenuItem = new ViewControlButton(this, ViewControlButton.EDITING_MODE);
        rbMenuItem.setSelected(debugOn);
        group.add(rbMenuItem);
        viewMenu.add(rbMenuItem);

    }

    private void clearFrame() {
        frame.remove(controlPanel);
        frame.remove(inputEnablePanel);
        frame.remove(outputEnablePanel);
        frame.remove(modulesPanel);
        frame.remove(modulesScrollPane);
        frame.remove(statusPanel);
        frame.remove(paramsPanel);
        frame.remove(paramsScrollPane);
        frame.remove(basicControlPanel);
        frame.remove(basicModulesScrollPane);

    }

    public void renderBasicMode() {
        basicGUI = true;

        clearFrame();

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 0, 5);
        c.gridx = 0;
        c.gridy = 0;

        // Initialising the control panel
        initialiseBasicControlPanel();
        frame.add(basicControlPanel, c);

        // Initialising the parameters panel
        initialiseBasicModulesPanel();
        c.gridy++;
        frame.add(basicModulesScrollPane, c);

        // Initialising the status panel
        if (!debugOn) {
            initialiseStatusPanel(basicFrameWidth);
            c.gridx = 0;
            c.gridy++;
            c.gridwidth = 1;
            c.insets = new Insets(5,5,5,5);
            frame.add(statusPanel,c);
        }

        frame.pack();
        frame.revalidate();
        frame.repaint();

        populateBasicModules();

    }

    public void renderEditingMode() throws InstantiationException, IllegalAccessException {
        basicGUI = false;

        clearFrame();

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 0);
        c.gridx = 0;
        c.gridy = 0;

        // Creating buttons to add and remove modules
        initialiseControlPanel();
        c.gridheight = 3;
        frame.add(controlPanel, c);

        // Initialising the status panel
        if (!debugOn) {
            initialiseStatusPanel(1080);
            c.gridheight = 1;
            c.gridy++;
            c.gridy++;
            c.gridy++;
            c.gridwidth = 3;
            c.insets = new Insets(0, 5, 5, 5);
            frame.add(statusPanel, c);
        }

        // Initialising the input enable panel
        initialiseInputEnablePanel();
        c.gridy = 0;
        c.gridx++;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 0, 0);
        frame.add(inputEnablePanel, c);

        // Initialising the module list panel
        initialisingModulesPanel();
        c.gridy++;
        c.insets = new Insets(5, 5, 5, 0);
        frame.add(modulesScrollPane, c);

        // Initialising the output enable panel
        initialiseOutputEnablePanel();
        c.gridy++;
        c.gridheight = 1;
        c.insets = new Insets(0, 5, 5, 0);
        frame.add(outputEnablePanel, c);

        // Initialising the parameters panel
        initialiseParametersPanel();
        c.gridx++;
        c.gridy = 0;
        c.gridheight = 3;
        c.insets = new Insets(5, 5, 5, 5);
        frame.add(paramsScrollPane, c);

        frame.pack();
        frame.revalidate();
        frame.repaint();

        populateModuleList();
        populateModuleParameters();

    }

    private void initialiseControlPanel() {
        controlPanel = new JPanel();

        controlPanel = new JPanel();
        controlPanel.setPreferredSize(new Dimension(bigButtonSize + 15, frameHeight - 50));
        controlPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        controlPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.PAGE_START;

        // Add module button
        ModuleControlButton.setButtonSize(bigButtonSize);
        ModuleControlButton addModuleButton = new ModuleControlButton(this, ModuleControlButton.ADD_MODULE);
        addModuleButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        controlPanel.add(addModuleButton, c);

        // Remove module button
        ModuleControlButton removeModuleButton = new ModuleControlButton(this, ModuleControlButton.REMOVE_MODULE);
        removeModuleButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        c.gridy++;
        controlPanel.add(removeModuleButton, c);

        // Move module up button
        ModuleControlButton moveModuleUpButton = new ModuleControlButton(this, ModuleControlButton.MOVE_MODULE_UP);
        moveModuleUpButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        c.gridy++;
        controlPanel.add(moveModuleUpButton, c);

        // Move module down button
        ModuleControlButton moveModuleDownButton = new ModuleControlButton(this, ModuleControlButton.MOVE_MODULE_DOWN);
        moveModuleDownButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        c.gridy++;
        controlPanel.add(moveModuleDownButton, c);

        // Load analysis protocol button
        AnalysisControlButton.setButtonSize(bigButtonSize);
        AnalysisControlButton loadAnalysisButton = new AnalysisControlButton(this, AnalysisControlButton.LOAD_ANALYSIS);
        c.gridy++;
        c.weighty = 1;
        c.anchor = GridBagConstraints.PAGE_END;
        controlPanel.add(loadAnalysisButton, c);

        // Save analysis protocol button
        AnalysisControlButton saveAnalysisButton = new AnalysisControlButton(this, AnalysisControlButton.SAVE_ANALYSIS);
        c.gridy++;
        c.weighty = 0;
        controlPanel.add(saveAnalysisButton, c);

        // Start analysis button
        AnalysisControlButton startAnalysisButton = new AnalysisControlButton(this, AnalysisControlButton.START_ANALYSIS);
        c.gridy++;
        controlPanel.add(startAnalysisButton, c);

        // Stop analysis button
        AnalysisControlButton stopAnalysisButton = new AnalysisControlButton(this, AnalysisControlButton.STOP_ANALYSIS);
        c.gridy++;
        controlPanel.add(stopAnalysisButton, c);

        controlPanel.validate();
        controlPanel.repaint();

    }

    private void initialiseInputEnablePanel() {
        inputEnablePanel = new JPanel();

        // Initialising the panel
        inputEnablePanel.setPreferredSize(new Dimension(moduleButtonWidth + 15, bigButtonSize + 15));
        inputEnablePanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        inputEnablePanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.PAGE_START;

        ModuleButton inputButton = new ModuleButton(this,analysis.getInputControl());
        inputButton.setPreferredSize(new Dimension(moduleButtonWidth,bigButtonSize));
        inputEnablePanel.add(inputButton, c);

        inputEnablePanel.validate();
        inputEnablePanel.repaint();

    }

    private void initialiseOutputEnablePanel() {
        outputEnablePanel = new JPanel();

        // Initialising the panel
        outputEnablePanel.setPreferredSize(new Dimension(moduleButtonWidth + 15, bigButtonSize + 15));
        outputEnablePanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        outputEnablePanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.PAGE_START;

        ModuleButton outputButton = new ModuleButton(this,analysis.getOutputControl());
        outputButton.setPreferredSize(new Dimension(moduleButtonWidth,bigButtonSize));
        outputEnablePanel.add(outputButton, c);

        outputEnablePanel.validate();
        outputEnablePanel.repaint();

    }

    private void initialisingModulesPanel() {
        modulesScrollPane = new JScrollPane(modulesPanel);

        // Initialising the scroll panel
        modulesScrollPane.setPreferredSize(new Dimension(moduleButtonWidth + 15, frameHeight - 2 * bigButtonSize - 90));
        modulesScrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        modulesScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        modulesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        modulesScrollPane.getVerticalScrollBar().setUnitIncrement(10);

        // Initialising the panel for module buttons
        modulesPanel.setLayout(new GridBagLayout());
        modulesPanel.validate();
        modulesPanel.repaint();

        modulesScrollPane.validate();
        modulesScrollPane.repaint();

    }

    private void initialiseParametersPanel() {
        paramsScrollPane = new JScrollPane(paramsPanel);

        // Initialising the scroll panel
        paramsScrollPane.setPreferredSize(new Dimension(700, frameHeight - 50));
        paramsScrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        paramsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        paramsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        paramsPanel.removeAll();

        paramsPanel.setLayout(new GridBagLayout());

        // Adding placeholder text
        JTextField textField = new JTextField("Select a module to edit its parameters");
        textField.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        textField.setBorder(null);
        textField.setEditable(false);
        paramsPanel.add(textField);

        paramsPanel.validate();
        paramsPanel.repaint();

        paramsScrollPane.validate();
        paramsScrollPane.repaint();

        // Displaying the input controls
        activeModule = analysis.getInputControl();
        updateModules();

    }

    private void initialiseStatusPanel(int width) {
        statusPanel = new JPanel();
        statusPanel.setPreferredSize(new Dimension(width, 40));
        statusPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        statusPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);

        JTextField textField = new JTextField();
        textField.setBackground(null);
        textField.setPreferredSize(new Dimension(width - 20, 25));
        textField.setBorder(null);
        textField.setText("Modular image analysis (version " + getClass().getPackage().getImplementationVersion() + ")");
        textField.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        statusPanel.add(textField, c);

        OutputStreamTextField outputStreamTextField = new OutputStreamTextField(textField);
        PrintStream printStream = new PrintStream(outputStreamTextField);
        System.setOut(printStream);

    }

    private void initialiseBasicControlPanel() {
        basicControlPanel = new JPanel();
        int buttonSize = 50;

        basicControlPanel = new JPanel();
        basicControlPanel.setPreferredSize(new Dimension(basicFrameWidth, buttonSize + 15));
        basicControlPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        basicControlPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.FIRST_LINE_START;

        // Load analysis protocol button
        AnalysisControlButton.setButtonSize(buttonSize);
        AnalysisControlButton loadAnalysisButton
                = new AnalysisControlButton(this, AnalysisControlButton.LOAD_ANALYSIS);
        c.gridx++;
        c.anchor = GridBagConstraints.PAGE_END;
        basicControlPanel.add(loadAnalysisButton, c);

        // Save analysis protocol button
        AnalysisControlButton saveAnalysisButton
                = new AnalysisControlButton(this, AnalysisControlButton.SAVE_ANALYSIS);
        c.gridx++;
        basicControlPanel.add(saveAnalysisButton, c);

        // Start analysis button
        AnalysisControlButton startAnalysisButton
                = new AnalysisControlButton(this, AnalysisControlButton.START_ANALYSIS);
        c.gridx++;
        c.weightx = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_END;
        basicControlPanel.add(startAnalysisButton, c);

        // Stop analysis button
        AnalysisControlButton stopAnalysisButton
                = new AnalysisControlButton(this, AnalysisControlButton.STOP_ANALYSIS);
        c.gridx++;
        c.weightx = 0;
        basicControlPanel.add(stopAnalysisButton, c);

        basicControlPanel.validate();
        basicControlPanel.repaint();

    }

    private void initialiseBasicModulesPanel() {
        int elementWidth = basicFrameWidth;

        // Initialising the scroll panel
        basicModulesScrollPane.setPreferredSize(new Dimension(elementWidth, frameHeight - 165));
        Border margin = new EmptyBorder(0,0,5,0);
        Border border = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
        basicModulesScrollPane.setBorder(new CompoundBorder(margin,border));
        basicModulesScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        basicModulesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Initialising the panel for module buttons
        basicModulesPanel.setLayout(new GridBagLayout());
        basicModulesPanel.validate();
        basicModulesPanel.repaint();

        basicModulesScrollPane.validate();
        basicModulesScrollPane.repaint();

    }

    public void populateModuleList() {
        modulesPanel.removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.FIRST_LINE_START;

        ButtonGroup group = new ButtonGroup();

        // Adding module buttons
        ModuleCollection modules = getModules();
        for (int i=0;i<modules.size();i++) {
        HCModule module = modules.get(i);
            int idx = modules.indexOf(module);
            if (idx == modules.size() - 1) c.weighty = 1;

            JPanel modulePanel = componentFactory
                    .createAdvancedModuleControl(module, group, activeModule, moduleButtonWidth - 25);

            // If this is the final module, add a gap at the bottom
            if (i==modules.size()-1) modulePanel.setBorder(new EmptyBorder(0,0,5,0));

            modulesPanel.add(modulePanel, c);
            c.gridy++;

        }

        modulesPanel.validate();
        modulesPanel.repaint();
        modulesScrollPane.validate();
        modulesScrollPane.repaint();

    }

    public void populateModuleParameters() {
        paramsPanel.removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        c.insets = new Insets(5, 5, 20, 5);
        c.anchor = GridBagConstraints.WEST;

        // If the active module is set to null (i.e. we're looking at the analysis options panel) exit this method
        if (activeModule == null) return;

        boolean isInputOutput = activeModule.getClass().isInstance(new InputControl())
                || activeModule.getClass().isInstance(new OutputControl());

        // Adding the nickname control to the top of the panel
        ModuleName moduleName = new ModuleName(this, activeModule);
        paramsPanel.add(moduleName, c);

        ResetModuleName resetModuleName = new ResetModuleName(this, activeModule);
        c.gridx++;
        c.weightx = 1;
        c.anchor = GridBagConstraints.EAST;
        paramsPanel.add(resetModuleName, c);

        // If it's an input/output control, get the current version
        if (activeModule.getClass().isInstance(new InputControl())) activeModule = analysis.getInputControl();
        if (activeModule.getClass().isInstance(new OutputControl())) activeModule = analysis.getOutputControl();

        // If the active module hasn't got parameters enabled, skip it
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.SOUTH;
        c.gridwidth = 2;
        c.insets = new Insets(0, 0, 0, 5);
        if (activeModule.getActiveParameters() != null) {
            Iterator<Parameter> iterator = activeModule.getActiveParameters().values().iterator();
            while (iterator.hasNext()) {
                Parameter parameter = iterator.next();

                if (isInputOutput &! iterator.hasNext()) {
                    c.weighty = 1;
                    c.anchor = GridBagConstraints.NORTH;
                }

                c.gridx = 0;
                c.gridy++;
                JPanel paramPanel = componentFactory.createParameterControl(parameter, getModules(), activeModule, 635);
                paramsPanel.add(paramPanel, c);

//                 Adding a checkbox to determine if the parameter should be visible to the user
//                if (!isInputOutput) {
                c.gridx++;
                paramsPanel.add(new VisibleCheck(parameter), c);
//                }
            }
        }

        // Creating the notes/help field at the bottom of the panel
        if (!isInputOutput) {
            JTabbedPane notesHelpPane = new JTabbedPane();
            notesHelpPane.setPreferredSize(new Dimension(-1, elementHeight * 3));

            String help = activeModule.getHelp();
            JTextArea helpArea = new JTextArea(help);
            helpArea.setEditable(false);
            notesHelpPane.addTab("Help", null, helpArea);

            String notes = activeModule.getNotes();
            NotesArea notesArea = new NotesArea(this, notes);
            notesHelpPane.addTab("Notes", null, notesArea);

            c.anchor = GridBagConstraints.LAST_LINE_START;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy++;
            c.weighty = 1;
            c.gridwidth = 3;
            c.insets = new Insets(5, 5, 5, 5);
            paramsPanel.add(notesHelpPane, c);

        }

        paramsPanel.validate();
        paramsPanel.repaint();

        paramsScrollPane.validate();
        paramsScrollPane.repaint();

    }

    void updateEvalButtonStates() {
        if (basicGUI) {
            for (Component component : basicModulesScrollPane.getComponents()) {
                if (component.getClass() == EvalButton.class) {
                    ((EvalButton) component).updateColour();

                }
            }
        } else {
            for (Component component : modulesScrollPane.getComponents()) {
                if (component.getClass() == EvalButton.class) {
                    ((EvalButton) component).updateColour();

                }
            }
        }
    }

    public void populateBasicModules() {
        basicModulesPanel.removeAll();

        JSeparator separator;
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0;

        // Adding input control options
        c.gridy++;
        JPanel inputPanel =
                componentFactory.createBasicModuleControl(analysis.getInputControl(),basicFrameWidth-40);

        if (inputPanel != null) {
            basicModulesPanel.add(inputPanel,c);

            // Adding a separator between the input and main modules
            c.gridy++;
            separator = new JSeparator();
            separator.setPreferredSize(new Dimension(basicFrameWidth-40, 15));
            basicModulesPanel.add(separator,c);

        }

        // Adding module buttons
        ModuleCollection modules = getModules();
        for (HCModule module : modules) {
            int idx = modules.indexOf(module);
            if (idx == modules.size() - 1) c.weighty = 1;
            c.gridy++;

            JPanel modulePanel = componentFactory.createBasicModuleControl(module,basicFrameWidth-40);
            if (modulePanel!=null) basicModulesPanel.add(modulePanel,c);

        }

        c.gridy++;
        JPanel outputPanel =
                componentFactory.createBasicModuleControl(analysis.getOutputControl(),basicFrameWidth-40);

        if (outputPanel != null) {
            // Adding a separator between the input and main modules
            c.gridy++;
            separator = new JSeparator();
            separator.setPreferredSize(new Dimension(basicFrameWidth-40, 15));
            basicModulesPanel.add(separator,c);

            c.gridy++;
            basicModulesPanel.add(outputPanel,c);

        }

        c.gridy++;
        c.weighty = 100;
        separator = new JSeparator();
        separator.setPreferredSize(new Dimension(0, 15));
        basicModulesPanel.add(separator, c);

        basicModulesPanel.validate();
        basicModulesPanel.repaint();
        basicModulesScrollPane.validate();
        basicModulesScrollPane.repaint();

    }

    private void listAvailableModules() throws IllegalAccessException, InstantiationException {
        // Using Reflections tool to get list of classes extending HCModule
        Reflections.log = null;
        Reflections reflections = new Reflections("wbif.sjx.ModularImageAnalysis");
        Set<Class<? extends HCModule>> availableModules = reflections.getSubTypesOf(HCModule.class);

        // Creating new instances of these classes and adding to ArrayList
        TreeMap<String, ArrayList<HCModule>> availableModulesList = new TreeMap<>();
        for (Class clazz : availableModules) {
            if (clazz != InputControl.class) {
                String[] names = clazz.getPackage().getName().split("\\.");
                String pkg = names[names.length - 1];

                availableModulesList.putIfAbsent(pkg, new ArrayList<>());
                availableModulesList.get(pkg).add((HCModule) clazz.newInstance());

            }
        }

        // Sorting the ArrayList based on module title
        for (ArrayList<HCModule> modules : availableModulesList.values()) {
            Collections.sort(modules, Comparator.comparing(HCModule::getTitle));
        }

        // Adding the modules to the list
        for (String pkgName : availableModulesList.keySet()) {
            ArrayList<HCModule> modules = availableModulesList.get(pkgName);
            ModuleListMenu packageMenu = new ModuleListMenu(this, pkgName, modules);

            moduleListMenu.add(packageMenu);

        }
    }

    public void addModule() {
        moduleListMenu.setLocation(MouseInfo.getPointerInfo().getLocation());
        moduleListMenu.setVisible(true);

    }

    public void removeModule() {
        if (activeModule != null) {
            ModuleCollection modules = getModules();
            // Removing a module resets all the current evaluation
            int idx = modules.indexOf(activeModule);
            if (idx < lastModuleEval) lastModuleEval = -1;

            modules.remove(activeModule);
            activeModule = null;

            populateModuleList();
            populateModuleParameters();

        }
    }

    public void moveModuleUp() {
        if (activeModule != null) {
            ModuleCollection modules = getModules();
            int idx = modules.indexOf(activeModule);
            if (idx != 0) {
                if (idx - 1 <= lastModuleEval) lastModuleEval = idx - 2;

                modules.remove(activeModule);
                modules.add(idx - 1, activeModule);
                populateModuleList();

            }
        }
    }

    public void moveModuleDown() {
        if (activeModule != null) {
            ModuleCollection modules = getModules();
            int idx = modules.indexOf(activeModule);
            if (idx != modules.size()) {
                if (idx <= lastModuleEval) lastModuleEval = idx - 1;

                modules.remove(activeModule);
                modules.add(idx + 1, activeModule);
                populateModuleList();
            }
        }
    }

    public JFrame getFrame() {
        return frame;
    }

    public JPopupMenu getModuleListMenu() {
        return moduleListMenu;
    }

    public JMenu getViewMenu() {
        return viewMenu;
    }

    public boolean isBasicGUI() {
        return basicGUI;
    }

    public GUIAnalysis getAnalysis() {
        return analysis;
    }

    public void setAnalysis(GUIAnalysis analysis) {
        this.analysis = analysis;
    }

    public ModuleCollection getModules() {
        return analysis.getModules();
    }

    @Override
    public void updateModules() {
        updateEvalButtonStates();
        if (!isBasicGUI()) {
            populateModuleParameters();
        } else {
            populateBasicModules();
        }
        populateModuleList();
    }

    public void setActiveModule(HCModule activeModule) {
        this.activeModule = activeModule;
    }

}
