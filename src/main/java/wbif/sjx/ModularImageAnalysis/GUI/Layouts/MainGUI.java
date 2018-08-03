// TODO: Add controls for all parameter types (hashsets, etc.)
// TODO: If an assigned image/object name is no longer available, flag up the module button in red
// TODO: Output panel could allow the user to select which objects and images to output to the spreadsheet

package wbif.sjx.ModularImageAnalysis.GUI.Layouts;

import org.apache.commons.lang.SystemUtils;
import org.reflections.Reflections;
import wbif.sjx.ModularImageAnalysis.GUI.*;
import wbif.sjx.ModularImageAnalysis.GUI.ControlObjects.*;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.InputControl;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.OutputControl;
import wbif.sjx.ModularImageAnalysis.MIA;
import wbif.sjx.ModularImageAnalysis.Module.*;
import wbif.sjx.ModularImageAnalysis.Module.Miscellaneous.GUISeparator;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Process.Analysis;
import wbif.sjx.ModularImageAnalysis.Process.BatchProcessor;
import wbif.sjx.common.FileConditions.ExtensionMatchesString;

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
    private int editingFrameWidth = 1200;
    private int minimumEditingFrameWidth = 800;
    private int basicFrameWidth = 400;
    private int minimumFrameHeight = 600;
    private int frameHeight = 800;
    private int elementHeight = 26;
    private int bigButtonSize = 40;
    private int moduleButtonWidth = 295;
    private int statusHeight = 40;

    private static boolean initialised = false;
    private boolean basicGUI = true;
    private boolean debugOn = false;

    private ComponentFactory componentFactory;
    private static final JFrame frame = new JFrame();
    private static final JMenuBar menuBar = new JMenuBar();
    private static final JMenu viewMenu = new JMenu("View");
    private static final JPanel basicPanel = new JPanel();
    private static final JPanel editingPanel = new JPanel();
    private static final JPanel modulesPanel = new JPanel();
    private static final JScrollPane modulesScrollPane = new JScrollPane(modulesPanel);
    private static final JPanel paramsPanel = new JPanel();
    private static final JScrollPane paramsScrollPane = new JScrollPane(paramsPanel);
    private static final JProgressBar editingProgressBar = new JProgressBar(0,100);
    private static final JProgressBar basicProgressBar = new JProgressBar(0,100);
    private static final JPanel basicModulesPanel = new JPanel();
    private static final StatusTextField textField = new StatusTextField();
    private static final JScrollPane basicModulesScrollPane = new JScrollPane(basicModulesPanel);
    private static final JPopupMenu moduleListMenu = new JPopupMenu();
    private static final JPanel basicStatusPanel = new JPanel();
    private static final JPanel editingStatusPanel = new JPanel();
    private static final GUISeparator loadSeparator = new GUISeparator();

    public MainGUI(boolean debugOn) throws InstantiationException, IllegalAccessException {
        // Only create a GUI if one hasn't already been created
        if (initialised) {
            frame.setVisible(true);
            return;
        }
        initialised = true;

        this.debugOn = debugOn;

        analysis.getInputControl().initialiseParameters();
        loadSeparator.initialiseParameters();
        loadSeparator.updateParameterValue(GUISeparator.TITLE,"File loading");
        componentFactory = new ComponentFactory(this, elementHeight);

        // Setting location of panel
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width - editingFrameWidth) / 2, (screenSize.height - frameHeight) / 2);
        frame.setTitle("MIA (version " + getClass().getPackage().getImplementationVersion() + ")");

        if (!debugOn) initialiseStatusTextField();

        // Creating the menu bar
        initialiseMenuBar();
        frame.setJMenuBar(menuBar);

        initialiseBasicMode();
        initialiseEditingMode();

        if (debugOn) {
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            renderEditingMode();
        } else {
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            renderBasicMode();
        }

        // Final bits for listeners
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

        // Creating the edit menu
        menu = new JMenu("Edit");
        menuBar.add(menu);
        menu.add(new AnalysisMenuItem(this, AnalysisMenuItem.CLEAR_PIPELINE));
        menu.add(new AnalysisMenuItem(this, AnalysisMenuItem.ENABLE_ALL));
        menu.add(new AnalysisMenuItem(this, AnalysisMenuItem.DISABLE_ALL));
        menu.add(new AnalysisMenuItem(this, AnalysisMenuItem.OUTPUT_ALL));
        menu.add(new AnalysisMenuItem(this, AnalysisMenuItem.SILENCE_ALL));

        // Creating the analysis menu
        menu = new JMenu("Analysis");
        menuBar.add(menu);
//        menu.add(new AnalysisMenuItem(this, AnalysisMenuItem.SET_FILE_TO_ANALYSE));
        menu.add(new AnalysisMenuItem(this, AnalysisMenuItem.START_ANALYSIS));
        menu.add(new AnalysisMenuItem(this, AnalysisMenuItem.STOP_ANALYSIS));

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

    public void render() throws IllegalAccessException, InstantiationException {
        if (basicGUI) {
            renderBasicMode();
        } else {
            renderEditingMode();
        }
    }

    public void initialiseBasicMode() {
        basicPanel.setLayout(new GridBagLayout());
        basicPanel.setPreferredSize(new Dimension(basicFrameWidth-20,frameHeight));
        basicPanel.setMinimumSize(new Dimension(basicFrameWidth-20,frameHeight));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 0, 5);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;

        // Initialising the control panel
        basicPanel.add(initialiseBasicControlPanel(), c);

        // Initialising the parameters panel
        initialiseBasicModulesPanel();
        c.gridy++;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        basicPanel.add(basicModulesScrollPane, c);

        // Initialising the status panel
        if (!debugOn) {
            c.gridy++;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weighty = 0;
            initialiseBasicStatusPanel();
            basicPanel.add(basicStatusPanel,c);
        }

        // Initialising the progress bar
        initialiseBasicProgressBar();
        c.gridy++;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5,5,5,5);
        basicPanel.add(basicProgressBar,c);

    }

    public void initialiseEditingMode() {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 0);
        c.gridx = 0;
        c.gridy = 0;

        editingPanel.setLayout(new GridBagLayout());

        // Creating buttons to add and remove modules
        JPanel controlPanel = initialiseControlPanel();
        c.weightx = 0;
        c.weighty = 1;
        c.gridheight = 3;
        c.fill = GridBagConstraints.VERTICAL;
        editingPanel.add(controlPanel, c);

        // Initialising the status panel
        if (!debugOn) {
            c.gridheight = 1;
            c.gridy++;
            c.gridy++;
            c.gridy++;
            c.weighty = 0;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridwidth = 3;
            c.insets = new Insets(0,5,5,5);
            initialiseEditingStatusPanel();
            editingPanel.add(editingStatusPanel, c);
        } else {
            c.gridheight = 1;
            c.gridy++;
            c.gridy++;
            c.gridy++;
            c.weighty = 0;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridwidth = 3;
        }

        // Initialising the progress bar
        initialiseEditingProgressBar();
        c.gridy++;
        c.insets = new Insets(0,5,5,5);
        editingPanel.add(editingProgressBar,c);

//        // Initialising the input enable panel
        initialiseInputEnablePanel();
        c.gridx++;
        c.gridy = 0;
        c.weightx = 0;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 0, 0);
        editingPanel.add(initialiseInputEnablePanel(), c);

        // Initialising the module list panel
        initialisingModulesPanel();
        c.gridy++;
        c.insets = new Insets(5, 5, 0, 0);
        c.weighty = 1;
        c.fill = GridBagConstraints.VERTICAL;
        editingPanel.add(modulesScrollPane, c);

        // Initialising the output enable panel
        initialiseOutputEnablePanel();
        c.gridy++;
        c.gridheight = 1;
        c.weighty = 0;
        c.insets = new Insets(5, 5, 5, 0);
        editingPanel.add(initialiseOutputEnablePanel(), c);

        // Initialising the parameters panel
        initialiseParametersPanel();
        c.gridx++;
        c.gridy = 0;
        c.gridheight = 3;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5, 5, 5, 5);
        editingPanel.add(paramsScrollPane, c);

    }

    public void renderBasicMode() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(0,5,0,0);
        c.anchor = GridBagConstraints.WEST;

        basicGUI = true;

        frame.remove(editingPanel);
        frame.add(basicPanel);
        basicStatusPanel.add(textField,c);

        basicPanel.setVisible(true);
        basicPanel.validate();
        basicPanel.repaint();

        frame.setPreferredSize(new Dimension(basicFrameWidth,frameHeight));
        frame.setMinimumSize(new Dimension(basicFrameWidth,minimumFrameHeight));

        frame.pack();
        frame.revalidate();
        frame.repaint();

        populateBasicModules();
        updateTestFile();

    }

    public void renderEditingMode() throws InstantiationException, IllegalAccessException {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(0,5,0,0);
        c.anchor = GridBagConstraints.WEST;

        basicGUI = false;

        frame.remove(basicPanel);
        frame.add(editingPanel);
        editingStatusPanel.add(textField,c);

        editingPanel.setVisible(true);
        editingPanel.validate();
        editingPanel.repaint();

        frame.setPreferredSize(new Dimension(editingFrameWidth,frameHeight));
        frame.setMinimumSize(new Dimension(minimumEditingFrameWidth,minimumFrameHeight));

        frame.pack();
        frame.revalidate();
        frame.repaint();

        populateModuleList();
        populateModuleParameters();
        updateTestFile();

    }

    private JPanel initialiseControlPanel() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.PAGE_START;

        JPanel controlPanel = new JPanel();
        controlPanel.setMaximumSize(new Dimension(bigButtonSize + 20, Integer.MAX_VALUE));
        controlPanel.setMinimumSize(new Dimension(bigButtonSize + 20, frameHeight - statusHeight-350));
        controlPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        controlPanel.setLayout(new GridBagLayout());

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

        return controlPanel;

    }

    private JPanel initialiseInputEnablePanel() {
        JPanel inputEnablePanel = new JPanel();

        // Initialising the panel
        inputEnablePanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        inputEnablePanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.PAGE_START;

        ModuleButton inputButton = new ModuleButton(this,analysis.getInputControl());
        inputButton.setPreferredSize(new Dimension(basicFrameWidth-65-bigButtonSize,bigButtonSize));
        inputEnablePanel.add(inputButton, c);

        inputEnablePanel.validate();
        inputEnablePanel.repaint();

        return inputEnablePanel;

    }

    private JPanel initialiseOutputEnablePanel() {
        JPanel outputEnablePanel = new JPanel();

        // Initialising the panel
        outputEnablePanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        outputEnablePanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.PAGE_START;

        ModuleButton outputButton = new ModuleButton(this,analysis.getOutputControl());
        outputButton.setPreferredSize(new Dimension(basicFrameWidth-65-bigButtonSize,bigButtonSize));
        outputEnablePanel.add(outputButton, c);

        outputEnablePanel.validate();
        outputEnablePanel.repaint();

        return outputEnablePanel;

    }

    private void initialisingModulesPanel() {
        // Initialising the scroll panel
        modulesScrollPane.setPreferredSize(new Dimension(basicFrameWidth-50-bigButtonSize, -1));
        modulesScrollPane.setMinimumSize(new Dimension(basicFrameWidth-50-bigButtonSize, -1));
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
        // Initialising the scroll panel
        paramsScrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        paramsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        paramsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        paramsPanel.setLayout(new GridBagLayout());

        paramsPanel.validate();
        paramsPanel.repaint();

        paramsScrollPane.validate();
        paramsScrollPane.repaint();

        // Displaying the input controls
        activeModule = analysis.getInputControl();
        updateModules();

    }

    private void initialiseEditingStatusPanel() {
        editingStatusPanel.setLayout(new GridBagLayout());
        editingStatusPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        editingStatusPanel.setMinimumSize(new Dimension(0,statusHeight+15));
        editingStatusPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,statusHeight+15));
        editingStatusPanel.setPreferredSize(new Dimension(basicFrameWidth-30,statusHeight+15));
        editingStatusPanel.setOpaque(false);
    }

    private void initialiseStatusTextField() {
        textField.setPreferredSize(new Dimension(Integer.MAX_VALUE,statusHeight));
        textField.setBorder(null);
        textField.setText("MIA (version " + getClass().getPackage().getImplementationVersion() + ")");
        textField.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        textField.setEditable(false);
        textField.setToolTipText(textField.getText());
        textField.setOpaque(false);

        OutputStreamTextField outputStreamTextField = new OutputStreamTextField(textField);
        PrintStream printStream = new PrintStream(outputStreamTextField);
        System.setOut(printStream);

    }

    private void initialiseEditingProgressBar() {
        editingProgressBar.setValue(0);
        editingProgressBar.setBorderPainted(false);
        editingProgressBar.setMinimumSize(new Dimension(0, 15));
        editingProgressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 15));
        editingProgressBar.setStringPainted(true);
        editingProgressBar.setString("");
        editingProgressBar.setForeground(new Color(86,190,253));
    }

    private void initialiseBasicProgressBar() {
        basicProgressBar.setValue(0);
        basicProgressBar.setBorderPainted(false);
        basicProgressBar.setPreferredSize(new Dimension(basicFrameWidth-30, 15));
        basicProgressBar.setStringPainted(true);
        basicProgressBar.setString("");
        basicProgressBar.setForeground(new Color(86,190,253));

    }

    private JPanel initialiseBasicControlPanel() {
        JPanel basicControlPanel = new JPanel();

        basicControlPanel.setPreferredSize(new Dimension(basicFrameWidth-30, bigButtonSize + 15));
        basicControlPanel.setMinimumSize(new Dimension(basicFrameWidth-30, bigButtonSize + 15));
        basicControlPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        basicControlPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.FIRST_LINE_START;

        // Load analysis protocol button
        AnalysisControlButton.setButtonSize(bigButtonSize);
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

        return basicControlPanel;

    }

    private void initialiseBasicModulesPanel() {
        int elementWidth = basicFrameWidth;

        // Initialising the scroll panel
        basicModulesScrollPane.setPreferredSize(new Dimension(basicFrameWidth-30, -1));
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

    private void initialiseBasicStatusPanel() {
        basicStatusPanel.setLayout(new GridBagLayout());
        basicStatusPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        basicStatusPanel.setMinimumSize(new Dimension(basicFrameWidth-30,statusHeight+15));
        basicStatusPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,statusHeight+15));
        basicStatusPanel.setPreferredSize(new Dimension(basicFrameWidth-30,statusHeight+15));
        basicStatusPanel.setOpaque(false);

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
        c.insets = new Insets(2,0,0,0);
        for (int i=0;i<modules.size();i++) {
            Module module = modules.get(i);
            int idx = modules.indexOf(module);
            if (idx == modules.size() - 1) c.weighty = 1;

            JPanel modulePanel = componentFactory
                    .createAdvancedModuleControl(module, group, activeModule, moduleButtonWidth - 25);

            // If this is the final module, add a gap at the bottom
            if (i==modules.size()-1) modulePanel.setBorder(new EmptyBorder(0,0,5,0));

            modulesPanel.add(modulePanel, c);
            c.insets = new Insets(0,0,0,0);
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
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 20, 0);
        c.anchor = GridBagConstraints.WEST;

        // If the active module is set to null (i.e. we're looking at the analysis options panel) exit this method
        if (activeModule == null) return;

        boolean isInput = activeModule.getClass().isInstance(new InputControl());
        boolean isOutput = activeModule.getClass().isInstance(new OutputControl());

        JPanel topPanel = componentFactory.createParametersTopRow(activeModule);
        c.gridwidth = 2;
        paramsPanel.add(topPanel,c);

        // If it's an input/output control, get the current version
        if (activeModule.getClass().isInstance(new InputControl())) activeModule = analysis.getInputControl();
        if (activeModule.getClass().isInstance(new OutputControl())) activeModule = analysis.getOutputControl();

        // If the active module hasn't got parameters enabled, skip it
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridwidth = 1;
        c.insets = new Insets(2, 5, 0, 0);
        if (activeModule.updateAndGetParameters() != null) {
            Iterator<Parameter> iterator = activeModule.updateAndGetParameters().values().iterator();
            while (iterator.hasNext()) {
                Parameter parameter = iterator.next();
                c.insets = new Insets(2, 5, 0, 0);
                c.gridx = 0;
                c.gridy++;
                c.weightx = 1;
                c.anchor = GridBagConstraints.WEST;
                JPanel paramPanel = componentFactory.createParameterControl(parameter, getModules(), activeModule);
                paramsPanel.add(paramPanel, c);

                c.insets = new Insets(2, 5, 0, 0);
                c.gridx++;
                c.weightx=0;
                c.anchor = GridBagConstraints.EAST;
                VisibleCheck visibleCheck = new VisibleCheck(parameter);
                visibleCheck.setPreferredSize(new Dimension(elementHeight,elementHeight));
                paramsPanel.add(visibleCheck, c);

            }
        }

        // If selected, adding the measurement selector for output control
        if (activeModule.getClass().isInstance(new OutputControl())
                && analysis.getOutputControl().isEnabled()
                && (boolean) analysis.getOutputControl().getParameterValue(OutputControl.SELECT_MEASUREMENTS)) {

            LinkedHashSet<Parameter> imageNameParameters = getModules().getParametersMatchingType(Parameter.OUTPUT_IMAGE);
            for (Parameter imageNameParameter:imageNameParameters) {
                String imageName = imageNameParameter.getValue();

                JPanel measurementHeader = componentFactory.createMeasurementHeader(imageName+" (Image)");
                c.gridx = 0;
                c.gridy++;
                c.fill = GridBagConstraints.HORIZONTAL;
                paramsPanel.add(measurementHeader,c);

                MeasurementReferenceCollection measurementReferences = getModules().getImageMeasurementReferences(imageName);
                // Iterating over the measurements for the current image, adding a control for each
                for (MeasurementReference measurementReference:measurementReferences.values()) {
                    if (!measurementReference.isCalculated()) continue;

                    // Adding measurement control
                    JPanel currentMeasurementPanel = componentFactory.createMeasurementControl(measurementReference);
                    c.gridy++;
                    paramsPanel.add(currentMeasurementPanel,c);

                }
            }

            LinkedHashSet<Parameter> objectNameParameters = getModules().getParametersMatchingType(Parameter.OUTPUT_OBJECTS);
            for (Parameter objectNameParameter:objectNameParameters) {
                String objectName = objectNameParameter.getValue();

                JPanel measurementHeader = componentFactory.createMeasurementHeader(objectName+" (Object)");
                c.gridx = 0;
                c.gridy++;
                c.fill = GridBagConstraints.HORIZONTAL;
                paramsPanel.add(measurementHeader,c);

                MeasurementReferenceCollection measurementReferences = getModules().getObjectMeasurementReferences(objectName);
                // Iterating over the measurements for the current object, adding a control for each
                for (MeasurementReference measurementReference:measurementReferences.values()) {
                    if (!measurementReference.isCalculated()) continue;

                    // Adding measurement control
                    JPanel currentMeasurementPanel = componentFactory.createMeasurementControl(measurementReference);
                    c.gridy++;
                    paramsPanel.add(currentMeasurementPanel,c);

                }
            }
        }

        // Creating the notes/help field at the bottom of the panel
        if (!isInput && !isOutput) {
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
            c.insets = new Insets(5, 5, 5, 10);
            paramsPanel.add(notesHelpPane, c);

        } else {
            JSeparator separator = new JSeparator();
            separator.setOpaque(false);
            separator.setSize(new Dimension(0,0));
            c.weighty = 1;
            c.gridy++;
            c.fill = GridBagConstraints.VERTICAL;
            paramsPanel.add(separator,c);
        }

        paramsPanel.validate();
        paramsPanel.repaint();

        paramsScrollPane.validate();
        paramsScrollPane.repaint();

    }

    private void updateEvalButtonStates() {
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

        // Only modules below an expanded GUISeparator should be displayed
        boolean expanded = loadSeparator.getParameterValue(GUISeparator.EXPANDED);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.insets = new Insets(0,5,0,5);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Check if there are no modules
        if (analysis.modules.size()==0) return;

        // Adding a separator between the input and main modules
        basicModulesPanel.add(componentFactory.getSeparator(loadSeparator,basicFrameWidth-80),c);

        // Adding input control options
        if (expanded) {
            c.gridy++;
            JPanel inputPanel = componentFactory.createBasicModuleControl(analysis.getInputControl(), basicFrameWidth - 80);
            if (inputPanel != null) basicModulesPanel.add(inputPanel, c);
        }

        // Adding module buttons
        ModuleCollection modules = getModules();
        for (Module module : modules) {
            // If the module is the special-case GUISeparator, create this module, then return
            JPanel modulePanel;
            if (module.getClass().isInstance(new GUISeparator())) {
                // Adding a blank space before the next separator
                if (expanded) {
                    JPanel blankPanel = new JPanel();
                    blankPanel.setPreferredSize(new Dimension(10, 10));
                    c.gridy++;
                    basicModulesPanel.add(blankPanel, c);
                }
                expanded = module.getParameterValue(GUISeparator.EXPANDED);
                modulePanel = componentFactory.getSeparator(module, basicFrameWidth-80);
            } else {
                modulePanel = componentFactory.createBasicModuleControl(module,basicFrameWidth-80);
            }

            if (modulePanel!=null && (expanded || module.getClass().isInstance(new GUISeparator()))) {
                c.gridy++;
                basicModulesPanel.add(modulePanel,c);
            }
        }

        JPanel outputPanel =componentFactory.createBasicModuleControl(analysis.getOutputControl(),basicFrameWidth-80);
        if (outputPanel != null) {
            c.gridy++;
            basicModulesPanel.add(outputPanel,c);
        }

        c.gridy++;
        c.weighty = 100;
        c.fill = GridBagConstraints.VERTICAL;
        JSeparator separator = new JSeparator();
        separator.setPreferredSize(new Dimension(-1,1));
        basicModulesPanel.add(separator, c);

        basicModulesPanel.validate();
        basicModulesPanel.repaint();
        basicModulesScrollPane.validate();
        basicModulesScrollPane.repaint();

    }

    private void listAvailableModules() throws IllegalAccessException, InstantiationException {
        // Using Reflections tool to get list of classes extending Module
        Reflections.log = null;
        Reflections reflections = new Reflections("wbif.sjx.ModularImageAnalysis");
        Set<Class<? extends Module>> availableModules = reflections.getSubTypesOf(Module.class);

        // Creating an alphabetically-ordered list of all modules
        TreeMap<String,Class> modules = new TreeMap<>();
        for (Class clazz : availableModules) {
            if (clazz != InputControl.class && clazz != OutputControl.class) {
                String[] names = clazz.getPackage().getName().split(MIA.slashes+".");
                StringBuilder stringBuilder = new StringBuilder();
                for (String name:names) stringBuilder.append(name);
                modules.put(stringBuilder.toString()+clazz.getSimpleName(),clazz);
            }
        }

        LinkedHashSet<ModuleListMenu> topList = new LinkedHashSet<>();
        for (Class clazz : modules.values()) {
            // ActiveList starts at the top list
            LinkedHashSet<ModuleListMenu> activeList = topList;
            ModuleListMenu activeItem = null;

            String[] names = clazz.getPackage().getName().split(MIA.slashes+".");
            for (int i=4;i<names.length;i++) {
                boolean found = false;
                for (ModuleListMenu listItemm:activeList) {
                    if (listItemm.getName().equals(names[i])) {
                        activeItem = listItemm;
                        found = true;
                    }
                }

                if (!found) {
                    ModuleListMenu newItem = new ModuleListMenu(this, names[i], new ArrayList<>());
                    newItem.setName(names[i]);
                    activeList.add(newItem);
                    if (activeItem != null) activeItem.add(newItem);
                    activeItem = newItem;
                }

                activeList = activeItem.getChildren();

            }
            activeItem.addMenuItem((Module) clazz.newInstance());
        }

        for (ModuleListMenu listMenu:topList) moduleListMenu.add(listMenu);
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

            if (idx <= lastModuleEval) lastModuleEval = idx - 1;

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
                if (idx - 2 <= lastModuleEval) lastModuleEval = idx - 2;

                modules.remove(activeModule);
                modules.add(idx - 1, activeModule);
                populateModuleList();
                updateModules();
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
                updateModules();
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

    public void setAnalysis(Analysis analysis) {
        this.analysis = analysis;
    }

    public ModuleCollection getModules() {
        return analysis.getModules();
    }

    public static void setProgress(int val) {
        editingProgressBar.setValue(val);
        basicProgressBar.setValue(val);
    }

    @Override
    public void updateModules() {
        populateModuleList();
        updateEvalButtonStates();

        if (isBasicGUI()) populateBasicModules();
        else populateModuleParameters();

    }

    @Override
    public void updateTestFile() {
        // Ensuring the input file specified in the InputControl is active in the test workspace
        InputControl inputControl = getAnalysis().getInputControl();
        String inputMode = inputControl.getParameterValue(InputControl.INPUT_MODE);
        String singleFile = inputControl.getParameterValue(InputControl.SINGLE_FILE_PATH);
        String batchFolder = inputControl.getParameterValue(InputControl.BATCH_FOLDER_PATH);
        String extension = inputControl.getParameterValue(InputControl.FILE_EXTENSION);
        int nThreads = inputControl.getParameterValue(InputControl.NUMBER_OF_THREADS);
        boolean useFilenameFilter1 = inputControl.getParameterValue(InputControl.USE_FILENAME_FILTER_1);
        String filenameFilter1 = inputControl.getParameterValue(InputControl.FILENAME_FILTER_1);
        String filenameFilterType1 = inputControl.getParameterValue(InputControl.FILENAME_FILTER_TYPE_1);
        boolean useFilenameFilter2 = inputControl.getParameterValue(InputControl.USE_FILENAME_FILTER_2);
        String filenameFilter2 = inputControl.getParameterValue(InputControl.FILENAME_FILTER_2);
        String filenameFilterType2 = inputControl.getParameterValue(InputControl.FILENAME_FILTER_TYPE_2);
        boolean useFilenameFilter3 = inputControl.getParameterValue(InputControl.USE_FILENAME_FILTER_3);
        String filenameFilter3 = inputControl.getParameterValue(InputControl.FILENAME_FILTER_3);
        String filenameFilterType3 = inputControl.getParameterValue(InputControl.FILENAME_FILTER_TYPE_3);

        Units.setUnits(inputControl.getParameterValue(InputControl.SPATIAL_UNITS));

        String inputFile = "";
        switch (inputMode) {
            case InputControl.InputModes.SINGLE_FILE:
                inputFile = singleFile;
                break;

            case InputControl.InputModes.BATCH:
                // Initialising BatchProcessor
                if (batchFolder == null) return;

                BatchProcessor batchProcessor = new BatchProcessor(new File(batchFolder));
                batchProcessor.setnThreads(nThreads);

                // Adding extension filter
                batchProcessor.addFileCondition(new ExtensionMatchesString(new String[]{extension}));

                // Adding filename filters
                if (useFilenameFilter1) batchProcessor.addFilenameFilter(filenameFilterType1,filenameFilter1);
                if (useFilenameFilter2) batchProcessor.addFilenameFilter(filenameFilterType2,filenameFilter2);
                if (useFilenameFilter3) batchProcessor.addFilenameFilter(filenameFilterType3,filenameFilter3);

                // Running the analysis
                File nextFile = batchProcessor.getNextValidFileInStructure();
                if (nextFile == null) {
                    inputFile = null;
                } else {
                    inputFile = nextFile.getAbsolutePath();
                }
                break;
        }

        if (inputFile == null) return;

        if (getTestWorkspace().getMetadata().getFile() == null) {
            lastModuleEval = -1;
            setTestWorkspace(new Workspace(1, new File(inputFile),1));
        }

        // If the input path isn't the same assign this new file
        if (!getTestWorkspace().getMetadata().getFile().getAbsolutePath().equals(inputFile)) {
            lastModuleEval = -1;
            setTestWorkspace(new Workspace(1, new File(inputFile),1));

        }

        switch ((String) analysis.getInputControl().getParameterValue(InputControl.SERIES_MODE)) {
            case InputControl.SeriesModes.ALL_SERIES:
                getTestWorkspace().getMetadata().setSeriesNumber(1);
                getTestWorkspace().getMetadata().setSeriesName("");
                break;

            case InputControl.SeriesModes.SINGLE_SERIES:
                int seriesNumber = analysis.getInputControl().getParameterValue(InputControl.SERIES_NUMBER);
                getTestWorkspace().getMetadata().setSeriesNumber(seriesNumber);
                getTestWorkspace().getMetadata().setSeriesName("");
        }
    }
}