// TODO: Add controls for all parameter types (hashsets, etc.)
// TODO: If an assigned image/object name is no longer available, flag up the module button in red

package wbif.sjx.ModularImageAnalysis.GUI;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import ij.io.Opener;
import org.apache.commons.io.FilenameUtils;
import org.reflections.Reflections;
import org.xml.sax.SAXException;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.*;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Process.AnalysisHandler;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

/**
 * Created by Stephen on 20/05/2017.
 */
public class MainGUI implements ActionListener, FocusListener, MouseListener {
    private static final String addModuleText = "+";
    private static final String removeModuleText = "-";
    private static final String moveModuleUpText = "▲";
    private static final String moveModuleDownText = "▼";
    private static final String saveAnalysis = "Save";
    private static final String loadAnalysis = "Load";
    private static final String startAnalysis = "Run";
    private static final String stopAnalysisText = "Stop";

    private int frameWidth = 1100;
    private int frameHeight = 750;
    private int elementHeight = 30;

    private ComponentFactory componentFactory;
    private HCWorkspace testWorkspace = new HCWorkspace(1,null);
    private HCModule activeModule = null;
    private JFrame frame = new JFrame();
    private JMenuBar menuBar = new JMenuBar();
    private JPanel controlPanel = new JPanel();
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

    private GUIAnalysis analysis = new GUIAnalysis();
    private HCModuleCollection modules = analysis.modules;

    public static void main(String[] args) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        new ImageJ();
        new MainGUI();

    }

    private MainGUI() throws InstantiationException, IllegalAccessException {
        componentFactory = new ComponentFactory(elementHeight,this,this);

        // Setting location of panel
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width - frameWidth) / 2, (screenSize.height - frameHeight) / 2);

        frame.setLayout(new GridBagLayout());
        frame.setTitle("Modular image analysis (version "+getClass().getPackage().getImplementationVersion()+")");

        // Creating the menu bar
        initialiseMenuBar();
        frame.setJMenuBar(menuBar);

        renderBasicMode();
//        renderEditingMode();

        // Final bits for listeners
        frame.addMouseListener(this);
        frame.setVisible(true);

        // Populating the list containing all available modules
        listAvailableModules();
        moduleListMenu.show(frame,0,0);
        moduleListMenu.setVisible(false);

    }

    private void initialiseMenuBar() {
        // Creating the file menu
        JMenu menu = new JMenu("File");
        menuBar.add(menu);

        JMenuItem menuItem = new JMenuItem("Load pipeline");
        menuItem.setName("LoadPipeline");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Save pipeline");
        menuItem.setName("SavePipeline");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        // Creating the analysis menu
        menu = new JMenu("Analysis");
        menuBar.add(menu);

        menuItem = new JMenuItem("Set file to analyse");
        menuItem.setName("SetFileToAnalyse");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Run analysis");
        menuItem.setName("StartAnalysis");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Stop analysis");
        menuItem.setName("StopAnalysis");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        // Creating the new menu
        menu = new JMenu("View");
        menuBar.add(menu);

        ButtonGroup group = new ButtonGroup();
        JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem("Basic mode");
        rbMenuItem.setName("BasicModeView");
        rbMenuItem.setSelected(true);
        rbMenuItem.addActionListener(this);
        group.add(rbMenuItem);
        menu.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem("Editing mode");
        rbMenuItem.setName("EditingModeView");
        rbMenuItem.addActionListener(this);
        group.add(rbMenuItem);
        menu.add(rbMenuItem);

    }

    private void clearFrame() {
        frame.remove(controlPanel);
        frame.remove(modulesPanel);
        frame.remove(modulesScrollPane);
        frame.remove(statusPanel);
        frame.remove(paramsPanel);
        frame.remove(paramsScrollPane);
        frame.remove(basicControlPanel);
        frame.remove(basicModulesScrollPane);

    }

    private void renderBasicMode() {
        basicGUI = true;

        clearFrame();

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5,5,0,5);
        c.gridx = 0;
        c.gridy = 0;

        // Initialising the control panel
        initialiseBasicControlPanel();
        frame.add(basicControlPanel,c);

        // Initialising the parameters panel
        initialiseBasicModulesPanel();
        c.gridy++;
        frame.add(basicModulesScrollPane,c);

        // Initialising the status panel
//        initialiseStatusPanel(500);
//        c.gridx = 0;
//        c.gridy++;
//        c.gridwidth = 1;
//        c.insets = new Insets(5,5,5,5);
//        frame.add(statusPanel,c);

        frame.pack();
        frame.revalidate();
        frame.repaint();

        populateBasicModules();

    }

    private void renderEditingMode() throws InstantiationException, IllegalAccessException {
        basicGUI = false;

        clearFrame();

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5,5,5,0);
        c.gridx = 0;
        c.gridy = 0;

        // Creating buttons to add and remove modules
        initialiseControlPanel();
        frame.add(controlPanel,c);

        // Initialising the module list
        initialisingModulesPanel();
        c.gridx++;
        frame.add(modulesScrollPane,c);

        // Initialising the parameters panel
        initialiseParametersPanel();
        c.gridx++;
        c.insets = new Insets(5,5,5,5);
        frame.add(paramsScrollPane,c);

        // Initialising the status panel
//        initialiseStatusPanel(1090);
//        c.gridx = 0;
//        c.gridy++;
//        c.gridwidth = 3;
//        c.insets = new Insets(0,5,5,5);
//        frame.add(statusPanel,c);

        frame.pack();
        frame.revalidate();
        frame.repaint();

        populateModuleList();
        populateModuleParameters();

    }

    private void initialiseControlPanel() {
        controlPanel = new JPanel();
        int buttonSize = 50;

        controlPanel = new JPanel();
        controlPanel.setPreferredSize(new Dimension(buttonSize + 15, frameHeight-50));
        controlPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        controlPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.PAGE_START;

        // Add module button
        JButton addModuleButton = new JButton(addModuleText);
        addModuleButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        addModuleButton.addActionListener(this);
        addModuleButton.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,20));
        addModuleButton.setMargin(new Insets(0,0,0,0));
        addModuleButton.setFocusPainted(false);
        addModuleButton.setName("ControlButton");
        addModuleButton.setMargin(new Insets(0,0,0,0));
        controlPanel.add(addModuleButton, c);

        // Remove module button
        JButton removeModuleButton = new JButton(removeModuleText);
        removeModuleButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        removeModuleButton.addActionListener(this);
        removeModuleButton.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,20));
        removeModuleButton.setMargin(new Insets(0,0,0,0));
        removeModuleButton.setFocusPainted(false);
        removeModuleButton.setName("ControlButton");
        c.gridy++;
        controlPanel.add(removeModuleButton, c);

        // Move module up button
        JButton moveModuleUpButton = new JButton(moveModuleUpText);
        moveModuleUpButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        moveModuleUpButton.addActionListener(this);
        moveModuleUpButton.setFont(new Font(Font.SANS_SERIF,Font.BOLD,16));
        moveModuleUpButton.setMargin(new Insets(0,0,0,0));
        moveModuleUpButton.setFocusPainted(false);
        moveModuleUpButton.setName("ControlButton");
        c.gridy++;
        controlPanel.add(moveModuleUpButton, c);

        // Move module down button
        JButton moveModuleDownButton = new JButton(moveModuleDownText);
        moveModuleDownButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        moveModuleDownButton.addActionListener(this);
        moveModuleDownButton.setFont(new Font(Font.SANS_SERIF,Font.BOLD,16));
        moveModuleDownButton.setMargin(new Insets(0,0,0,0));
        moveModuleDownButton.setFocusPainted(false);
        moveModuleDownButton.setName("ControlButton");
        c.gridy++;
        controlPanel.add(moveModuleDownButton, c);

        // Load analysis protocol button
        JButton loadAnalysisButton = new JButton(loadAnalysis);
        loadAnalysisButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        loadAnalysisButton.addActionListener(this);
        loadAnalysisButton.setFocusPainted(false);
        loadAnalysisButton.setMargin(new Insets(0,0,0,0));
        loadAnalysisButton.setName("LoadPipeline");
        c.gridy++;
        c.weighty = 1;
        c.anchor = GridBagConstraints.PAGE_END;
        controlPanel.add(loadAnalysisButton, c);

        // Save analysis protocol button
        JButton saveAnalysisButton = new JButton(saveAnalysis);
        saveAnalysisButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        saveAnalysisButton.addActionListener(this);
        saveAnalysisButton.setFocusPainted(false);
        saveAnalysisButton.setMargin(new Insets(0,0,0,0));
        saveAnalysisButton.setName("SavePipeline");
        c.gridy++;
        c.weighty = 0;
        controlPanel.add(saveAnalysisButton, c);

        // Start analysis button
        JButton startAnalysisButton = new JButton(startAnalysis);
        startAnalysisButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        startAnalysisButton.addActionListener(this);
        startAnalysisButton.setFocusPainted(false);
        startAnalysisButton.setMargin(new Insets(0,0,0,0));
        startAnalysisButton.setName("StartAnalysis");
        c.gridy++;
        controlPanel.add(startAnalysisButton, c);

        // Stop analysis button
        JButton stopAnalysisButton = new JButton(stopAnalysisText);
        stopAnalysisButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        stopAnalysisButton.addActionListener(this);
        stopAnalysisButton.setMargin(new Insets(0,0,0,0));
        stopAnalysisButton.setFocusPainted(false);
        stopAnalysisButton.setName("StopAnalysis");
        c.gridy++;
        controlPanel.add(stopAnalysisButton, c);

        controlPanel.validate();
        controlPanel.repaint();

    }

    private void initialisingModulesPanel() {
        modulesScrollPane = new JScrollPane(modulesPanel);
        int buttonWidth = 300;

        // Initialising the scroll panel
        modulesScrollPane.setPreferredSize(new Dimension(buttonWidth + 15, frameHeight-50));
        modulesScrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        modulesScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        modulesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

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
        paramsScrollPane.setPreferredSize(new Dimension(700, frameHeight-50));
        paramsScrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        paramsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        paramsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        paramsPanel.removeAll();

        paramsPanel.setLayout(new GridBagLayout());

        // Adding placeholder text
        JTextField textField = new JTextField("Select a module to edit its parameters");
        textField.setFont(new Font(Font.SANS_SERIF,Font.BOLD,12));
        textField.setBorder(null);
        textField.setEditable(false);
        paramsPanel.add(textField);

        paramsPanel.validate();
        paramsPanel.repaint();

        paramsScrollPane.validate();
        paramsScrollPane.repaint();

    }

    private void initialiseStatusPanel(int width) {
        statusPanel = new JPanel();
        statusPanel.setPreferredSize(new Dimension(width,40));
        statusPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        statusPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5,5,5,5);

        JTextField textField = new JTextField();
        textField.setBackground(null);
        textField.setPreferredSize(new Dimension(width-20,25));
        textField.setBorder(null);
        textField.setText("Modular image analysis (version "+getClass().getPackage().getImplementationVersion()+")");
        textField.setFont(new Font(Font.SANS_SERIF,Font.BOLD,12));
        statusPanel.add(textField,c);

        OutputStreamTextField outputStreamTextField = new OutputStreamTextField(textField);
        PrintStream printStream = new PrintStream(outputStreamTextField);
        System.setOut(printStream);

    }

    private void initialiseBasicControlPanel() {
        basicControlPanel = new JPanel();
        int buttonSize = 50;

        basicControlPanel = new JPanel();
        basicControlPanel.setPreferredSize(new Dimension(500, buttonSize + 15));
        basicControlPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        basicControlPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.FIRST_LINE_START;

        // Load analysis protocol button
        JButton loadAnalysisButton = new JButton(loadAnalysis);
        loadAnalysisButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        loadAnalysisButton.addActionListener(this);
        loadAnalysisButton.setFocusPainted(false);
        loadAnalysisButton.setMargin(new Insets(0,0,0,0));
        loadAnalysisButton.setName("LoadPipeline");
        c.gridx++;
        c.anchor = GridBagConstraints.PAGE_END;
        basicControlPanel.add(loadAnalysisButton, c);

        // Save analysis protocol button
        JButton saveAnalysisButton = new JButton(saveAnalysis);
        saveAnalysisButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        saveAnalysisButton.addActionListener(this);
        saveAnalysisButton.setFocusPainted(false);
        saveAnalysisButton.setMargin(new Insets(0,0,0,0));
        saveAnalysisButton.setName("SavePipeline");
        c.gridx++;
        basicControlPanel.add(saveAnalysisButton, c);

        // Start analysis button
        JButton startAnalysisButton = new JButton(startAnalysis);
        startAnalysisButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        startAnalysisButton.addActionListener(this);
        startAnalysisButton.setMargin(new Insets(0,0,0,0));
        startAnalysisButton.setFocusPainted(false);
        startAnalysisButton.setName("StartAnalysis");
        c.gridx++;
        c.weightx = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_END;
        basicControlPanel.add(startAnalysisButton, c);

        // Stop analysis button
        JButton stopAnalysisButton = new JButton(stopAnalysisText);
        stopAnalysisButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        stopAnalysisButton.addActionListener(this);
        stopAnalysisButton.setMargin(new Insets(0,0,0,0));
        stopAnalysisButton.setFocusPainted(false);
        stopAnalysisButton.setName("StopAnalysis");
        c.gridx++;
        c.weightx = 0;
        basicControlPanel.add(stopAnalysisButton, c);

        basicControlPanel.validate();
        basicControlPanel.repaint();
    }

    private void initialiseBasicModulesPanel() {
        int elementWidth = 500;

        // Initialising the scroll panel
        basicModulesScrollPane.setPreferredSize(new Dimension(elementWidth, frameHeight-165));
        basicModulesScrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        basicModulesScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        basicModulesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Initialising the panel for module buttons
        basicModulesPanel.setLayout(new GridBagLayout());
        basicModulesPanel.validate();
        basicModulesPanel.repaint();

        basicModulesScrollPane.validate();
        basicModulesScrollPane.repaint();

    }

    private void populateModuleList() {
        modulesPanel.removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.FIRST_LINE_START;

        ButtonGroup group = new ButtonGroup();

        // Adding module buttons
        for (HCModule module : modules) {
            Color color;
            int idx = modules.indexOf(module);

            if (!module.isEnabled()) {
                color = Color.LIGHT_GRAY;
            } else {
                if (idx <= lastModuleEval) {
                    color = Color.getHSBColor(0.27f,1f,0.6f);
                } else {
                    color = Color.getHSBColor(0f,1f,0.6f);
                }
            }

            if (idx == modules.size()-1) c.weighty = 1;

            JPanel modulePanel = componentFactory.createAdvancedModuleControl(module,group,activeModule,color,300-25);
            modulesPanel.add(modulePanel, c);
            c.gridy++;

        }

        modulesPanel.validate();
        modulesPanel.repaint();
        modulesScrollPane.validate();
        modulesScrollPane.repaint();

    }

    private void populateModuleParameters() {
        paramsPanel.removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        c.insets = new Insets(0,0,0,5);
        c.fill = GridBagConstraints.HORIZONTAL;

        // If the active module is set to null (i.e. we're looking at the analysis options panel) exit this method
        if (activeModule == null) {
            return;
        }

        // If the active module hasn't got parameters enabled, skip it
        if (activeModule.getActiveParameters() != null) {
            Iterator<HCParameter> iterator = activeModule.getActiveParameters().values().iterator();
            while (iterator.hasNext()) {
                HCParameter parameter = iterator.next();

                c.gridx = 0;
                JPanel paramPanel = componentFactory.createParameterControl(parameter,modules,activeModule,635);
                paramsPanel.add(paramPanel,c);

                // Adding a checkbox to determine if the parameter should be visible to the user
                c.gridx++;
                VisibleCheck visibleCheck = new VisibleCheck(parameter);
                visibleCheck.addActionListener(this);
                paramsPanel.add(visibleCheck,c);

                c.gridy++;

            }
        }

        // Creating the notes/help field at the bottom of the panel
        JTabbedPane notesHelpPane = new JTabbedPane();
        notesHelpPane.setPreferredSize(new Dimension(-1, elementHeight*3));

        String help = activeModule.getHelp();
        JTextArea helpArea = new JTextArea(help);
        helpArea.setEditable(false);
        notesHelpPane.addTab("Help", null, helpArea);

        String notes = activeModule.getNotes();
        JTextArea notesArea = new JTextArea(notes);
        notesArea.setName("NotesArea");
        notesArea.addFocusListener(this);
        notesHelpPane.addTab("Notes", null, notesArea);

        c.anchor = GridBagConstraints.LAST_LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.weighty = 1;
        c.gridwidth = 3;
        c.insets = new Insets(5, 5, 5, 5);
        paramsPanel.add(notesHelpPane,c);

        paramsPanel.validate();
        paramsPanel.repaint();

        paramsScrollPane.validate();
        paramsScrollPane.repaint();

    }

    private void populateBasicModules() {
        basicModulesPanel.removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0;

        // Adding module buttons
        for (HCModule module : modules) {
            Color color;
            int idx = modules.indexOf(module);
            if (!module.isEnabled()) {
                color = Color.LIGHT_GRAY;
            } else {
                if (idx <= lastModuleEval) {
                    color = Color.getHSBColor(0.27f,1f,0.6f);
                } else {
                    color = Color.getHSBColor(0f,1f,0.6f);
                }
            }

            if (idx == modules.size()-1) c.weighty = 1;

            // Only show if the module is enabled
            if (!module.isEnabled()) continue;

            // Only displaying the module title if it has at least one visible parameter
            boolean hasVisibleParameters = false;
            for (HCParameter parameter:module.getActiveParameters().values()) {
                if (parameter.isVisible()) hasVisibleParameters = true;
            }
            if (!hasVisibleParameters) continue;

            JPanel titlePanel = componentFactory.createBasicModuleHeading(module,color,500-50);

            c.gridy++;
            c.anchor = GridBagConstraints.FIRST_LINE_START;
            basicModulesPanel.add(titlePanel,c);

            for (HCParameter parameter:module.getActiveParameters().values()) {
                if (parameter.isVisible()) {
                    JPanel paramPanel = componentFactory.createParameterControl(parameter, modules, module, 500-80);

                    c.gridy++;
                    c.anchor = GridBagConstraints.FIRST_LINE_END;
                    basicModulesPanel.add(paramPanel, c);

                }
            }

            c.gridy++;
            JSeparator separator = new JSeparator();
            separator.setPreferredSize(new Dimension(0,15));
            separator.setName("Separator");
            basicModulesPanel.add(separator,c);

        }

        c.gridy++;
        c.weighty = 1;
        JSeparator separator = new JSeparator();
        separator.setPreferredSize(new Dimension(0,0));
        separator.setName("Separator");
        basicModulesPanel.add(separator,c);

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
        TreeMap<String,ArrayList<HCModule>> availableModulesList = new TreeMap<>();
        for (Class clazz : availableModules) {
            String[] names = clazz.getPackage().getName().split("\\.");
            String pkg = names[names.length-1];

            availableModulesList.putIfAbsent(pkg,new ArrayList<>());
            availableModulesList.get(pkg).add((HCModule) clazz.newInstance());

        }

        // Sorting the ArrayList based on module title
        for (ArrayList<HCModule> modules:availableModulesList.values()) {
            Collections.sort(modules, Comparator.comparing(HCModule::getTitle));
        }

        // Adding the modules to the list
        for (String pkgName:availableModulesList.keySet()) {
            ArrayList<HCModule> modules = availableModulesList.get(pkgName);
            JMenu packageMenu = new JMenu(pkgName);
            packageMenu.setName("ModuleListPackage");
            packageMenu.addMouseListener(this);

            for (HCModule module : modules) {
                PopupMenuItem menuItem = new PopupMenuItem(module);
                menuItem.addActionListener(this);
                menuItem.setName("ModuleName");
                packageMenu.add(menuItem);

            }

            moduleListMenu.add(packageMenu);

        }
    }

    private void addModule() {
        moduleListMenu.setLocation(MouseInfo.getPointerInfo().getLocation());
        moduleListMenu.setVisible(true);

    }

    private void removeModule() {
        if (activeModule != null) {
            // Removing a module resets all the current evaluation
            int idx = modules.indexOf(activeModule);
            if (idx < lastModuleEval) lastModuleEval = -1;

            modules.remove(activeModule);
            activeModule = null;

            populateModuleList();
            populateModuleParameters();

        }
    }

    private void moveModuleUp() {
        if (activeModule != null) {
            int idx = modules.indexOf(activeModule);
            if (idx != 0) {
                if (idx-1 <= lastModuleEval) lastModuleEval = idx-2;

                modules.remove(activeModule);
                modules.add(idx - 1, activeModule);
                populateModuleList();

            }
        }
    }

    private void moveModuleDown() {
        if (activeModule != null) {
            int idx = modules.indexOf(activeModule);
            if (idx != modules.size()) {
                if (idx <= lastModuleEval) lastModuleEval = idx-1;

                modules.remove(activeModule);
                modules.add(idx + 1, activeModule);
                populateModuleList();
            }
        }
    }

    private void evaluateModule(HCModule module) throws GenericMIAException {
        module.execute(testWorkspace,true);
        lastModuleEval = modules.indexOf(module);

        if (basicGUI) {
            populateBasicModules();

        } else {
            populateModuleList();

        }
    }

    private void reactToAction(Object object)
            throws IllegalAccessException, InstantiationException, IOException, ClassNotFoundException, TransformerException, ParserConfigurationException, SAXException {

        String componentName = ((JComponent) object).getName();

        if (componentName.equals("BasicModeView")) {
            renderBasicMode();

        } else if (componentName.equals("EditingModeView")) {
            renderEditingMode();

        } else if (componentName.equals("ControlButton")) {
            if (((JButton) object).getText().equals(addModuleText)) {
                addModule();

            } else if (((JButton) object).getText().equals(removeModuleText)) {
                removeModule();

            } else if (((JButton) object).getText().equals(moveModuleUpText)) {
                moveModuleUp();

            } else if (((JButton) object).getText().equals(moveModuleDownText)) {
                moveModuleDown();

            }

        } else if (componentName.equals("LoadPipeline")) {
            analysis = (GUIAnalysis) new AnalysisHandler().loadAnalysis();
            modules = analysis.getModules();

            if (basicGUI) {
                populateBasicModules();

            } else {
                populateModuleList();
                populateModuleParameters();

            }

            lastModuleEval = -1;

        } else if (componentName.equals("SavePipeline")) {
            new AnalysisHandler().saveAnalysis(analysis);

        } else if (componentName.equals("SetFileToAnalyse")) {
            FileDialog fileDialog = new FileDialog(new Frame(), "Select file to save", FileDialog.LOAD);
            fileDialog.setMultipleMode(false);
            fileDialog.setVisible(true);

            testWorkspace = new HCWorkspace(1,fileDialog.getFiles()[0]);

            System.out.println("Set current file to \""+fileDialog.getFiles()[0].getName()+"\"");

        } else if (componentName.equals("StartAnalysis")) {
            Thread t = new Thread(() -> {
                try {
                    testWorkspace = new AnalysisHandler().startAnalysis(analysis);
                    lastModuleEval = modules.size() - 1;

                    if (basicGUI) {
                        populateBasicModules();
                    } else {
                        populateModuleList();
                        populateModuleParameters();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (GenericMIAException e) {
                    IJ.showMessage(e.getMessage());
                }
            });
            t.start();

        } else if (componentName.equals("StopAnalysis")) {
            System.out.println("Shutting system down");
            analysis.shutdown();

        } else if (componentName.equals("ModuleName")) {
            moduleListMenu.setVisible(false);

            if (((PopupMenuItem) object).getModule() == null) return;

            // Adding it after the currently-selected module
            HCModule newModule = ((PopupMenuItem) object).getModule().getClass().newInstance();
            if (activeModule != null) {
                int idx = modules.indexOf(activeModule);
                activeModule = newModule;
                modules.add(++idx,newModule);

            } else {
                activeModule = newModule;
                modules.add(newModule);

            }

            // Adding to the list of modules
            populateModuleList();

            activeModule = newModule;

            if (basicGUI) {
                populateBasicModules();
            } else {
                populateModuleParameters();
            }

        } else if (componentName.equals("EvalButton")) {
            HCModule evalModule = ((EvalButton) object).getModule();
            int idx = modules.indexOf(evalModule);

            // If the module is ready to be evaluated
            if (idx <= lastModuleEval) new Thread(() -> {
                try {
                    evaluateModule(evalModule);
                } catch (GenericMIAException e) {
                    IJ.showMessage(e.getMessage());
                }
            }).start();

            // If multiple modules will need to be evaluated first
            new Thread(() -> {
                for (int i = lastModuleEval+1;i<=idx;i++) {
                    HCModule module = modules.get(i);
                    if (module.isEnabled()) try {
                        evaluateModule(module);
                    } catch (GenericMIAException e) {
                        IJ.showMessage(e.getMessage());
                    }

                }
            }).start();

        } else if (componentName.equals("ModuleEnabledCheck")) {
            HCModule module = ((ModuleEnabledCheck) object).getModule();
            module.setEnabled(((ModuleEnabledCheck) object).isSelected());

            int idx = modules.indexOf(module);
            if (idx <= lastModuleEval) {
                lastModuleEval = idx-1;
            }

            populateModuleList();
            populateModuleParameters();

        } else if (componentName.equals("ModuleButton")) {
            activeModule = ((ModuleButton) object).getModule();

            if (basicGUI) {
                populateBasicModules();
            } else {
                populateModuleParameters();
            }

        } else if (componentName.equals("InputParameter")) {
            HCParameter parameter = ((ImageObjectInputParameter) object).getParameter();
            parameter.setValue(((ImageObjectInputParameter) object).getSelectedItem());
            HCModule module = ((ImageObjectInputParameter) object).getModule();

            int idx = modules.indexOf(module);
            if (idx <= lastModuleEval) lastModuleEval = idx-1;
            if (basicGUI) {
                populateBasicModules();
            } else {
                populateModuleList();
                populateModuleParameters();
            }

        } else if (componentName.equals("TextParameter")) {
            HCParameter parameter = ((TextParameter) object).getParameter();
            String text = ((TextParameter) object).getText();
            HCModule module = ((TextParameter) object).getModule();

            if (parameter.getType() == HCParameter.OUTPUT_IMAGE | parameter.getType() == HCParameter.OUTPUT_OBJECTS) {
                parameter.setValue(text);

            } else if (parameter.getType() == HCParameter.INTEGER) {
                parameter.setValue(Integer.valueOf(text));

            } else if (parameter.getType() == HCParameter.DOUBLE) {
                parameter.setValue(Double.valueOf(text));

            } else if (parameter.getType() == HCParameter.STRING) {
                parameter.setValue(text);

            }

            int idx = modules.indexOf(module);
            if (idx <= lastModuleEval) lastModuleEval = idx-1;

            if (basicGUI) {
                populateBasicModules();
            } else {
                populateModuleList();
            }

        } else if (componentName.equals("BooleanParameter")) {
            HCParameter parameter = ((BooleanParameter) object).getParameter();
            HCModule module = ((BooleanParameter) object).getModule();

            parameter.setValue(((BooleanParameter) object).isSelected());

            int idx = modules.indexOf(module);
            if (idx <= lastModuleEval) lastModuleEval = idx-1;

            if (basicGUI) {
                populateBasicModules();
            } else {
                populateModuleList();
                populateModuleParameters();
            }

        } else if (componentName.equals("FileParameter")) {
            HCParameter parameter = ((FileParameter) object).getParameter();
            HCModule module = ((FileParameter) object).getModule();

            FileDialog fileDialog = new FileDialog(new Frame(), "Select image to load", FileDialog.LOAD);
            fileDialog.setMultipleMode(false);
            fileDialog.setVisible(true);

            parameter.setValue(fileDialog.getFiles()[0].getAbsolutePath());
            ((FileParameter) object).setText(FilenameUtils.getName(parameter.getValue()));

            int idx = modules.indexOf(module);
            if (idx <= lastModuleEval) lastModuleEval = idx-1;

            if (basicGUI) {
                populateBasicModules();
            } else {
                populateModuleList();
            }

        } else if (componentName.equals("ChoiceArrayParameter")) {
            HCParameter parameter = ((ChoiceArrayParameter) object).getParameter();
            parameter.setValue(((ChoiceArrayParameter) object).getSelectedItem());
            HCModule module = ((ChoiceArrayParameter) object).getModule();

            int idx = modules.indexOf(module);
            if (idx <= lastModuleEval) lastModuleEval = idx-1;

            if (basicGUI) {
                populateBasicModules();
            } else {
                populateModuleList();
                populateModuleParameters();
            }

        } else if (componentName.equals("VisibleCheck")) {
            HCParameter parameter = ((VisibleCheck) object).getParameter();
            parameter.setVisible(((VisibleCheck) object).isSelected());

        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new Thread(() -> {
            try {
                reactToAction(e.getSource());
            } catch (IllegalAccessException | InstantiationException | ClassNotFoundException | ParserConfigurationException | IOException | TransformerException | SAXException e1) {
                e1.printStackTrace();
            }
        }).start();
    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        new Thread(() -> {
            try {
                reactToAction(e.getSource());
            } catch (IllegalAccessException | InstantiationException | ClassNotFoundException | ParserConfigurationException | IOException | TransformerException | SAXException e1) {
                e1.printStackTrace();
            }
        }).start();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        moduleListMenu.setVisible(false);

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getComponent().getName().equals("ModuleListPackage")) {
            // Adding the mouse listener to show the relevant sub-menu
            moduleListMenu.show(frame, e.getX(), e.getY());

        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
