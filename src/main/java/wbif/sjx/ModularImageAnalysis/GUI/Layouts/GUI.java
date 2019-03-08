// TODO: Add controls for all parameter types (hashsets, etc.)
// TODO: If an assigned image/object name is no longer available, flag up the module button in red
// TODO: Output panel could allow the user to select which objects and images to output to the spreadsheet

package wbif.sjx.ModularImageAnalysis.GUI.Layouts;

import org.apache.commons.io.output.TeeOutputStream;
import wbif.sjx.ModularImageAnalysis.GUI.ComponentFactory;
import wbif.sjx.ModularImageAnalysis.GUI.ControlObjects.*;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.InputControl;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.OutputControl;
import wbif.sjx.ModularImageAnalysis.MIA;
import wbif.sjx.ModularImageAnalysis.Module.Miscellaneous.GUISeparator;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;
import wbif.sjx.ModularImageAnalysis.Process.AnalysisHandling.Analysis;
import wbif.sjx.ModularImageAnalysis.Process.AnalysisHandling.AnalysisTester;
import wbif.sjx.ModularImageAnalysis.Process.BatchProcessor;
import wbif.sjx.ModularImageAnalysis.Process.ClassHunter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Stephen on 20/05/2017.
 */
public class GUI {
    private static Analysis analysis = new Analysis();
    private static Module activeModule = null;
    private static Module lastHelpNotesModule = null;
    private static int lastModuleEval = -1;
    private static int moduleBeingEval = -1;
    private static Workspace testWorkspace = new Workspace(1, null,1);

    private static int editingFrameWidth = 1200;
    private static int minimumEditingFrameWidth = 800;
    private static int basicFrameWidth = 400;
    private static int minimumFrameHeight = 600;
    private static int frameHeight = 800;
    private static int elementHeight = 26;
    private static int bigButtonSize = 45;
    private static int moduleButtonWidth = 295;
    private static int statusHeight = 20;

    private static boolean initialised = false;
    private static boolean basicGUI = true;
    private static boolean showEditingHelpNotes = false;//Prefs.get("MIA.showEditingHelpNotes",true);
    private static boolean showBasicHelpNotes = false;//Prefs.get("MIA.showEditingHelpNotes",true);

    private static ComponentFactory componentFactory;
    private static final JFrame frame = new JFrame();
    private static final JMenuBar menuBar = new JMenuBar();
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
    private static final JPanel basicHelpNotesPanel = new JPanel();
    private static final JPanel helpNotesPanel = new JPanel();
    private static final JPanel basicHelpPanel = new JPanel();
    private static final JPanel helpPanel = new JPanel();
    private static final JPanel basicNotesPanel = new JPanel();
    private static final JPanel notesPanel = new JPanel();
    private static final GUISeparator loadSeparator = new GUISeparator();
    private static final ButtonGroup group = new ButtonGroup();
    private static final ModuleControlButton addModuleButton = new ModuleControlButton(ModuleControlButton.ADD_MODULE,bigButtonSize);
    private static final ModuleButton inputButton = new ModuleButton(analysis.getInputControl());
    private static final ModuleButton outputButton = new ModuleButton(analysis.getOutputControl());
    private static final MeasurementRef globalMeasurementRef = new MeasurementRef("Global");


    public GUI() throws InstantiationException, IllegalAccessException {
        // Only create a GUI if one hasn't already been created
        if (initialised) {
            frame.setVisible(true);
            return;
        }
        initialised = true;

        // Starting this processAutomatic, as it takes longest
        new Thread(GUI::listAvailableModules).start();

        loadSeparator.setNickname("File loading");
        componentFactory = new ComponentFactory(elementHeight);

        // Setting location of panel
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width - editingFrameWidth) / 2, (screenSize.height - frameHeight) / 2);
        frame.setTitle("MIA (version " + MIA.getVersion() + ")");

        initialiseStatusTextField();

        // Creating the menu bar
        initialiseMenuBar();
        frame.setJMenuBar(menuBar);

        initialiseBasicMode();
        initialiseEditingMode();

        if (MIA.isDebug()) {
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            renderEditingMode();
        } else {
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            renderBasicMode();
        }

        // Final bits for listeners
        frame.setVisible(true);

        // Populating the list containing all available modules
        moduleListMenu.show(frame, 0, 0);
        moduleListMenu.setVisible(false);

    }

    private static void initialiseMenuBar() {
        // Creating the file menu
        JMenu menu = new JMenu("File");
        menu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        menuBar.add(menu);
        menu.add(new AnalysisMenuItem(AnalysisMenuItem.LOAD_ANALYSIS));
        menu.add(new AnalysisMenuItem(AnalysisMenuItem.SAVE_ANALYSIS));

        // Creating the edit menu
        menu = new JMenu("Edit");
        menu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        menuBar.add(menu);
        menu.add(new AnalysisMenuItem(AnalysisMenuItem.CLEAR_PIPELINE));
        menu.add(new AnalysisMenuItem(AnalysisMenuItem.ENABLE_ALL));
        menu.add(new AnalysisMenuItem(AnalysisMenuItem.DISABLE_ALL));
        menu.add(new AnalysisMenuItem(AnalysisMenuItem.OUTPUT_ALL));
        menu.add(new AnalysisMenuItem(AnalysisMenuItem.SILENCE_ALL));

        // Creating the analysis menu
        menu = new JMenu("Analysis");
        menu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        menuBar.add(menu);
        menu.add(new AnalysisMenuItem(AnalysisMenuItem.START_ANALYSIS));
        menu.add(new AnalysisMenuItem(AnalysisMenuItem.STOP_ANALYSIS));

        // Creating the new menu
        menu = new JMenu("View");
        menu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        menuBar.add(menu);
        if (MIA.isDebug()) {
            menu.add(new AnalysisMenuItem(AnalysisMenuItem.BASIC_VIEW));
        } else {
            menu.add(new AnalysisMenuItem(AnalysisMenuItem.EDITING_VIEW));
        }
        menu.add(new AnalysisMenuItem(AnalysisMenuItem.TOGGLE_HELP_NOTES));

    }

    public static void render() throws IllegalAccessException, InstantiationException {
        if (basicGUI) {
            renderBasicMode();
        } else {
            renderEditingMode();
        }
    }

    public static void initialiseBasicMode() {
        basicPanel.setLayout(new GridBagLayout());
        basicPanel.setPreferredSize(new Dimension(basicFrameWidth-20,frameHeight));
        basicPanel.setMinimumSize(new Dimension(basicFrameWidth-20,frameHeight));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 0, 5);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;

        // Initialising the control panel
        basicPanel.add(initialiseBasicControlPanel(), c);

        // Initialising the parameters panel
        initialiseBasicModulesPanel();
        c.gridy++;
        c.weighty = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.BOTH;
        basicPanel.add(basicModulesScrollPane, c);

        // Initialising the help and notes panels
        initialiseBasicHelpNotesPanels();
        c.gridx++;
        c.insets = new Insets(5, 0, 0, 5);
        basicPanel.add(basicHelpNotesPanel,c);

        // Initialising the status panel
        c.gridx = 0;
        c.gridy++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0;
        c.gridwidth = 2;
        c.insets = new Insets(5, 5, 0, 5);
        initialiseBasicStatusPanel();
        basicPanel.add(basicStatusPanel,c);

        // Initialising the progress bar
        initialiseBasicProgressBar();
        c.gridy++;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5,5,5,5);
        basicPanel.add(basicProgressBar,c);

    }

    public static void initialiseEditingMode() {
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
        c.gridheight = 1;
        c.gridy++;
        c.gridy++;
        c.gridy++;
        c.weighty = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 4;
        c.insets = new Insets(0,5,5,5);
        initialiseEditingStatusPanel();
        editingPanel.add(editingStatusPanel, c);

        // Initialising the progress bar
        initialiseEditingProgressBar();
        c.gridy++;
        c.insets = new Insets(0,5,5,5);
        editingPanel.add(editingProgressBar,c);

        // Initialising the input enable panel
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

        initialiseHelpNotesPanels();
        c.gridx++;
        c.weightx = 0;
        c.insets = new Insets(5,0,5,5);
        editingPanel.add(helpNotesPanel,c);

    }

    public static void renderBasicMode() {
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

        basicHelpNotesPanel.setVisible(showBasicHelpNotes);

        basicPanel.setVisible(true);
        basicPanel.validate();
        basicPanel.repaint();

        int frameWidth = basicFrameWidth;
        if (showBasicHelpNotes) frameWidth = frameWidth + 319;
        frame.setPreferredSize(new Dimension(frameWidth,frameHeight));
        frame.setMinimumSize(new Dimension(frameWidth,minimumFrameHeight));

        frame.pack();
        frame.validate();
        frame.repaint();

        if (showBasicHelpNotes) populateBasicHelpNotes();
        populateBasicModules();
        updateTestFile();

    }

    public static void renderEditingMode() throws InstantiationException, IllegalAccessException {
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

        helpNotesPanel.setVisible(showEditingHelpNotes);

        editingPanel.setVisible(true);
        editingPanel.validate();
        editingPanel.repaint();

        int frameWidth = editingFrameWidth;
        if (showEditingHelpNotes) frameWidth = frameWidth + 315;
        frame.setPreferredSize(new Dimension(frameWidth,frameHeight));
        frame.setMinimumSize(new Dimension(frameWidth,minimumFrameHeight));

        frame.pack();
        frame.revalidate();
        frame.repaint();

        populateModuleList();
        populateModuleParameters();
        if (showEditingHelpNotes) populateHelpNotes();
        updateTestFile();

    }

    private static JPanel initialiseControlPanel() {
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
        ModuleControlButton removeModuleButton = new ModuleControlButton(ModuleControlButton.REMOVE_MODULE,bigButtonSize);
        removeModuleButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        c.gridy++;
        controlPanel.add(removeModuleButton, c);

        // Move module up button
        ModuleControlButton moveModuleUpButton = new ModuleControlButton(ModuleControlButton.MOVE_MODULE_UP,bigButtonSize);
        moveModuleUpButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        c.gridy++;
        controlPanel.add(moveModuleUpButton, c);

        // Move module down button
        ModuleControlButton moveModuleDownButton = new ModuleControlButton(ModuleControlButton.MOVE_MODULE_DOWN,bigButtonSize);
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

    private static JPanel initialiseInputEnablePanel() {
        JPanel inputEnablePanel = new JPanel();

        // Initialising the panel
        inputEnablePanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        inputEnablePanel.setPreferredSize(new Dimension(basicFrameWidth-45-bigButtonSize, bigButtonSize+15));
        inputEnablePanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.PAGE_START;
        c.fill = GridBagConstraints.BOTH;

        group.add(inputButton);
        inputEnablePanel.add(inputButton, c);

        inputEnablePanel.validate();
        inputEnablePanel.repaint();

        return inputEnablePanel;

    }

    private static JPanel initialiseOutputEnablePanel() {
        JPanel outputEnablePanel = new JPanel();

        // Initialising the panel
        outputEnablePanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        outputEnablePanel.setPreferredSize(new Dimension(basicFrameWidth-45-bigButtonSize, bigButtonSize+15));
        outputEnablePanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.PAGE_START;
        c.fill = GridBagConstraints.BOTH;

        group.add(outputButton);
        outputEnablePanel.add(outputButton, c);

        outputEnablePanel.validate();
        outputEnablePanel.repaint();

        return outputEnablePanel;

    }

    private static void initialisingModulesPanel() {
        // Initialising the scroll panel
        modulesScrollPane.setPreferredSize(new Dimension(basicFrameWidth-45-bigButtonSize, -1));
        modulesScrollPane.setMinimumSize(new Dimension(basicFrameWidth-45-bigButtonSize, -1));
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

    private static void initialiseParametersPanel() {
        // Initialising the scroll panel
        paramsScrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        paramsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        paramsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        paramsScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        paramsScrollPane.setPreferredSize(new Dimension(basicFrameWidth-45-bigButtonSize, bigButtonSize+15));

        paramsPanel.setLayout(new GridBagLayout());

        paramsPanel.validate();
        paramsPanel.repaint();

        paramsScrollPane.validate();
        paramsScrollPane.repaint();

        // Displaying the input controls
        activeModule = analysis.getInputControl();

    }

    private static void initialiseHelpNotesPanels() {
        // Initialising the scroll panel
        helpPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        helpPanel.setPreferredSize(new Dimension(basicFrameWidth-45-bigButtonSize, bigButtonSize+15));
        helpPanel.setLayout(new GridBagLayout());

        // Initialising the scroll panel
        notesPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        notesPanel.setPreferredSize(new Dimension(basicFrameWidth-45-bigButtonSize, bigButtonSize+15));
        notesPanel.setLayout(new GridBagLayout());

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

    private static void initialiseBasicHelpNotesPanels() {
        // Initialising the scroll panel
        basicHelpPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        basicHelpPanel.setPreferredSize(new Dimension(basicFrameWidth-45-bigButtonSize, bigButtonSize+15));
        basicHelpPanel.setLayout(new GridBagLayout());

        // Initialising the scroll panel
        basicNotesPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        basicNotesPanel.setPreferredSize(new Dimension(basicFrameWidth-45-bigButtonSize, bigButtonSize+15));
        basicNotesPanel.setLayout(new GridBagLayout());

        // Adding panels to combined JPanel
        basicHelpNotesPanel.setLayout(new GridBagLayout());
        GridBagConstraints cc = new GridBagConstraints();

        cc.fill = GridBagConstraints.BOTH;
        cc.gridx = 0;
        cc.gridy = 0;
        cc.weightx = 1;
        cc.weighty = 2;
        cc.insets = new Insets(0,0,5,0);
        basicHelpNotesPanel.add(basicHelpPanel,cc);

        cc.gridy++;
        cc.weighty = 1;
        cc.insets = new Insets(0,0,0,0);
        basicHelpNotesPanel.add(basicNotesPanel,cc);

    }

    private static void initialiseEditingStatusPanel() {
        editingStatusPanel.setLayout(new GridBagLayout());
        editingStatusPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        editingStatusPanel.setMinimumSize(new Dimension(0,statusHeight+15));
        editingStatusPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,statusHeight+15));
        editingStatusPanel.setPreferredSize(new Dimension(basicFrameWidth-30,statusHeight+15));
        editingStatusPanel.setOpaque(false);
    }

    private static void initialiseStatusTextField() {
        textField.setPreferredSize(new Dimension(Integer.MAX_VALUE,statusHeight));
        textField.setBorder(null);
        textField.setText("MIA (version " + MIA.getVersion() + ")");
        textField.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        textField.setToolTipText(textField.getText());
        textField.setOpaque(false);

        OutputStreamTextField outputStreamTextField = new OutputStreamTextField(textField);
        PrintStream guiPrintStream = new PrintStream(outputStreamTextField);

        if (MIA.isDebug()) {
            TeeOutputStream teeOutputStream = new TeeOutputStream(System.out,guiPrintStream);
            PrintStream printStream = new PrintStream(teeOutputStream);
            System.setOut(printStream);
        } else {
            System.setOut(guiPrintStream);
        }
    }

    private static void initialiseEditingProgressBar() {
        editingProgressBar.setValue(0);
        editingProgressBar.setBorderPainted(false);
        editingProgressBar.setMinimumSize(new Dimension(0, 15));
        editingProgressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 15));
        editingProgressBar.setStringPainted(true);
        editingProgressBar.setString("");
        editingProgressBar.setForeground(new Color(86,190,253));
    }

    private static void initialiseBasicProgressBar() {
        basicProgressBar.setValue(0);
        basicProgressBar.setBorderPainted(false);
        basicProgressBar.setMinimumSize(new Dimension(0, 15));
        basicProgressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 15));
        basicProgressBar.setStringPainted(true);
        basicProgressBar.setString("");
        basicProgressBar.setForeground(new Color(86,190,253));

    }

    private static JPanel initialiseBasicControlPanel() {
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
        AnalysisControlButton loadAnalysisButton
                = new AnalysisControlButton(AnalysisControlButton.LOAD_ANALYSIS,bigButtonSize);
        c.gridx++;
        c.anchor = GridBagConstraints.PAGE_END;
        basicControlPanel.add(loadAnalysisButton, c);

        // Save analysis protocol button
        AnalysisControlButton saveAnalysisButton
                = new AnalysisControlButton(AnalysisControlButton.SAVE_ANALYSIS,bigButtonSize);
        c.gridx++;
        basicControlPanel.add(saveAnalysisButton, c);

        // Start analysis button
        AnalysisControlButton startAnalysisButton
                = new AnalysisControlButton(AnalysisControlButton.START_ANALYSIS,bigButtonSize);
        c.gridx++;
        c.weightx = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_END;
        basicControlPanel.add(startAnalysisButton, c);

        // Stop analysis button
        AnalysisControlButton stopAnalysisButton
                = new AnalysisControlButton(AnalysisControlButton.STOP_ANALYSIS,bigButtonSize);
        c.gridx++;
        c.weightx = 0;
        basicControlPanel.add(stopAnalysisButton, c);

        basicControlPanel.validate();
        basicControlPanel.repaint();

        return basicControlPanel;

    }

    private static void initialiseBasicModulesPanel() {
        int elementWidth = basicFrameWidth;

        // Initialising the scroll panel
        basicModulesScrollPane.setPreferredSize(new Dimension(basicFrameWidth-30, -1));
        basicModulesScrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        basicModulesScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        basicModulesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        basicModulesScrollPane.getVerticalScrollBar().setUnitIncrement(10);

        // Initialising the panel for module buttons
        basicModulesPanel.setLayout(new GridBagLayout());

        basicModulesPanel.validate();
        basicModulesPanel.repaint();

        basicModulesScrollPane.validate();
        basicModulesScrollPane.repaint();

    }

    private static void initialiseBasicStatusPanel() {
        basicStatusPanel.setLayout(new GridBagLayout());
        basicStatusPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        basicStatusPanel.setMinimumSize(new Dimension(basicFrameWidth-30,statusHeight+15));
        basicStatusPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,statusHeight+15));
        basicStatusPanel.setPreferredSize(new Dimension(basicFrameWidth-30,statusHeight+15));
        basicStatusPanel.setOpaque(false);

    }

    public static void populateModuleList() {
        modulesPanel.removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.FIRST_LINE_START;

        boolean expanded = true;
        // Adding module buttons
        ModuleCollection modules = getModules();
        c.insets = new Insets(2,0,0,0);
        for (int i=0;i<modules.size();i++) {
            Module module = modules.get(i);
            int idx = modules.indexOf(module);
            if (idx == modules.size() - 1) c.weighty = 1;

            JPanel modulePanel = null;
            if (module.getClass().isInstance(new GUISeparator())) {
                expanded = ((BooleanP) module.getParameter(GUISeparator.EXPANDED_EDITING)).isSelected();
                modulePanel = componentFactory.createEditingSeparator(module, group, activeModule, moduleButtonWidth - 25);
            } else {
                if (!expanded) continue;
                modulePanel = componentFactory.createAdvancedModuleControl(module, group, activeModule, moduleButtonWidth - 25);
            }

            // If this is the final module, add a gap at the bottom
            if (i==modules.size()-1) modulePanel.setBorder(new EmptyBorder(0,0,5,0));

            modulesPanel.add(modulePanel, c);
            c.insets = new Insets(0,0,0,0);
            c.gridy++;

        }

        c.gridy++;
        c.weighty = 1;
        c.fill = GridBagConstraints.VERTICAL;
        JSeparator separator = new JSeparator();
        separator.setPreferredSize(new Dimension(-1,1));
        modulesPanel.add(separator, c);

        modulesScrollPane.revalidate();
        modulesScrollPane.repaint();

        modulesPanel.revalidate();
        modulesPanel.repaint();

    }

    public static void addAdvancedParameterControl(Parameter parameter, GridBagConstraints c) {
        c.insets = new Insets(2, 5, 0, 0);
        c.gridx = 0;
        c.gridy++;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        JPanel paramPanel = componentFactory.createParameterControl(parameter, getModules(), activeModule);
        paramsPanel.add(paramPanel, c);

        c.insets = new Insets(2, 5, 0, 5);
        c.gridx++;
        c.weightx = 0;
        c.anchor = GridBagConstraints.EAST;
        VisibleCheck visibleCheck = new VisibleCheck(parameter);
        visibleCheck.setPreferredSize(new Dimension(elementHeight, elementHeight));
        paramsPanel.add(visibleCheck, c);

    }

    public static void addAdvancedParameterGroup(ParameterGroup group, GridBagConstraints c) {
        // Iterating over each collection of Parameters.  After adding each one, a remove button is included
        LinkedHashSet<ParameterCollection> collections = group.getCollections();

        JSeparator separator = new JSeparator();
        separator.setPreferredSize(new Dimension(0,15));
        separator.setForeground(paramsPanel.getBackground());
        separator.setBackground(paramsPanel.getBackground());
        c.gridy++;
        paramsPanel.add(separator, c);

        for (ParameterCollection collection:collections) {
            // Adding the individual parameters
            for (Parameter parameter:collection) addAdvancedParameterControl(parameter,c);

            separator = new JSeparator();
            separator.setPreferredSize(new Dimension(0,15));
            separator.setForeground(paramsPanel.getBackground());
            separator.setBackground(paramsPanel.getBackground());
            c.gridy++;
            paramsPanel.add(separator, c);

        }

        // Adding an add button
        addAdvancedParameterControl(group,c);

        separator = new JSeparator();
        separator.setPreferredSize(new Dimension(0,15));
        separator.setForeground(paramsPanel.getBackground());
        separator.setBackground(paramsPanel.getBackground());
        c.gridy++;
        paramsPanel.add(separator, c);

    }

    public static void populateModuleParameters() {
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
            for (Parameter parameter : activeModule.updateAndGetParameters()) {
                if (parameter.getClass() == ParameterGroup.class) {
                    addAdvancedParameterGroup((ParameterGroup) parameter,c);
                } else {
                    addAdvancedParameterControl(parameter,c);
                }
            }
        }

        // If selected, adding the measurement selector for output control
        if (activeModule.getClass().isInstance(new OutputControl())
                && analysis.getOutputControl().isEnabled()
                && ((BooleanP) analysis.getOutputControl().getParameter(OutputControl.SELECT_MEASUREMENTS)).isSelected()) {

            // Creating global controls for the different statistics
            JPanel measurementHeader = componentFactory.createMeasurementHeader("Global control",null);
            c.gridx = 0;
            c.gridy++;
            c.gridwidth = 2;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.WEST;
            paramsPanel.add(measurementHeader,c);

            JPanel currentMeasurementPanel = componentFactory.createGlobalMeasurementControl(globalMeasurementRef);
            c.gridy++;
            c.anchor = GridBagConstraints.EAST;
            paramsPanel.add(currentMeasurementPanel,c);

            LinkedHashSet<OutputImageP> imageNameParameters = getModules().getParametersMatchingType(OutputImageP.class);
            for (OutputImageP imageNameParameter:imageNameParameters) {
                String imageName = imageNameParameter.getImageName();
                MeasurementRefCollection measurementReferences = getModules().getImageMeasurementRefs(imageName);

                if (measurementReferences.size() == 0) continue;

                measurementHeader = componentFactory.createMeasurementHeader(imageName+" (Image)", measurementReferences);
                c.gridx = 0;
                c.gridy++;
                c.anchor = GridBagConstraints.WEST;
                paramsPanel.add(measurementHeader,c);

                // Iterating over the measurements for the current image, adding a control for each
                for (MeasurementRef measurementReference:measurementReferences.values()) {
                    if (!measurementReference.isCalculated()) continue;

                    // Adding measurement control
                    currentMeasurementPanel = componentFactory.createMeasurementControl(measurementReference);
                    c.gridy++;
                    c.anchor = GridBagConstraints.EAST;
                    paramsPanel.add(currentMeasurementPanel,c);

                }
            }

            LinkedHashSet<OutputObjectsP> objectNameParameters = getModules().getParametersMatchingType(OutputObjectsP.class);
            for (OutputObjectsP objectNameParameter:objectNameParameters) {
                String objectName = objectNameParameter.getObjectsName();
                MeasurementRefCollection measurementReferences = getModules().getObjectMeasurementRefs(objectName);

                if (measurementReferences.size() == 0) continue;

                measurementHeader = componentFactory.createMeasurementHeader(objectName+" (Object)",measurementReferences);
                c.gridx = 0;
                c.gridy++;
                c.anchor = GridBagConstraints.WEST;
                paramsPanel.add(measurementHeader,c);

                // Iterating over the measurements for the current object, adding a control for each
                for (MeasurementRef measurementReference:measurementReferences.values()) {
                    if (!measurementReference.isCalculated()) continue;

                    // Adding measurement control
                    currentMeasurementPanel = componentFactory.createMeasurementControl(measurementReference);
                    c.gridy++;
                    c.anchor = GridBagConstraints.EAST;
                    paramsPanel.add(currentMeasurementPanel,c);

                }
            }
        }

        // Creating the notes/help field at the bottom of the panel
        JSeparator separator = new JSeparator();
        separator.setOpaque(false);
        separator.setSize(new Dimension(0,0));
        c.weighty = 1;
        c.gridy++;
        c.fill = GridBagConstraints.VERTICAL;
        paramsPanel.add(separator,c);

        paramsPanel.validate();
        paramsPanel.repaint();

        paramsScrollPane.validate();
        paramsScrollPane.repaint();

    }

    public static void populateHelpNotes() {
        // Only update the help and notes if the module has changed
        if (activeModule != lastHelpNotesModule) {
            lastHelpNotesModule = activeModule;
        } else {
            return;
        }

        helpPanel.removeAll();
        helpPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.insets = new Insets(5,5,0,5);
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;

        // Adding title to help window
        JLabel helpLabel = new JLabel();
        helpLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        helpLabel.setText(activeModule.getTitle());
        helpPanel.add(helpLabel,c);

        // Adding separator
        JSeparator separator = new JSeparator();
        c.gridy++;
        helpPanel.add(separator,c);

        // If no Module is selected, also skip
        HelpArea helpArea = new HelpArea();
        c.gridy++;
        c.weighty = 1;
        c.insets = new Insets(5,5,5,5);

        JScrollPane jsp = new JScrollPane(helpArea);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.getVerticalScrollBar().setUnitIncrement(10);
        jsp.setBorder(null);
        helpPanel.add(jsp,c);

        helpPanel.revalidate();
        helpPanel.repaint();

        notesPanel.removeAll();
        notesPanel.setLayout(new GridBagLayout());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.insets = new Insets(5,5,0,5);
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;

        // Adding title to help window
        JLabel notesLabel = new JLabel();
        notesLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        notesLabel.setText("Notes");
        notesPanel.add(notesLabel,c);

        // Adding separator
        separator = new JSeparator();
        c.gridy++;
        notesPanel.add(separator,c);

        String notes = activeModule == null ? "" : activeModule.getNotes();
        NotesArea notesArea = new NotesArea(notes);
        c.gridy++;
        c.weighty = 1;
        c.insets = new Insets(5,5,5,5);

        jsp = new JScrollPane(notesArea);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.getVerticalScrollBar().setUnitIncrement(10);
        notesPanel.add(jsp,c);

        notesPanel.validate();
        notesPanel.repaint();

    }

    public static void populateBasicHelpNotes() {
        // Only update the help and notes if the module has changed
        if (activeModule != lastHelpNotesModule) {
            lastHelpNotesModule = activeModule;
        } else {
            return;
        }

        basicHelpPanel.removeAll();
        basicHelpPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.insets = new Insets(5,5,0,5);
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;

        // Adding title to help window
        JLabel helpLabel = new JLabel();
        helpLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        helpLabel.setText(activeModule.getTitle());
        basicHelpPanel.add(helpLabel,c);

        // Adding separator
        JSeparator separator = new JSeparator();
        c.gridy++;
        basicHelpPanel.add(separator,c);

        // If no Module is selected, also skip
        HelpArea helpArea = new HelpArea();
        c.gridy++;
        c.weighty = 1;
        c.insets = new Insets(5,5,5,5);

        JScrollPane jsp = new JScrollPane(helpArea);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.getVerticalScrollBar().setUnitIncrement(10);
        jsp.setBorder(null);
        basicHelpPanel.add(jsp,c);

        basicHelpPanel.revalidate();
        basicHelpPanel.repaint();

        basicNotesPanel.removeAll();
        basicNotesPanel.setLayout(new GridBagLayout());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.insets = new Insets(5,5,0,5);
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;

        // Adding title to help window
        JLabel notesLabel = new JLabel();
        notesLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        notesLabel.setText("Notes");
        basicNotesPanel.add(notesLabel,c);

        // Adding separator
        separator = new JSeparator();
        c.gridy++;
        basicNotesPanel.add(separator,c);

        String notes = activeModule == null ? "" : activeModule.getNotes();
        NotesArea notesArea = new NotesArea(notes);
        c.gridy++;
        c.weighty = 1;
        c.insets = new Insets(5,5,5,5);

        jsp = new JScrollPane(notesArea);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.getVerticalScrollBar().setUnitIncrement(10);
        basicNotesPanel.add(jsp,c);

        basicNotesPanel.validate();
        basicNotesPanel.repaint();

    }

    private static void updateButtonStates() {
        if (!basicGUI) {
            for (Component panel : modulesPanel.getComponents()) {
                if (panel.getClass() == JPanel.class) {
                    for (Component component: ((JPanel) panel).getComponents()) {
                        if (component.getClass() == ModuleEnabledButton.class) {
                            ((ModuleEnabledButton) component).updateState();
                        } else if (component.getClass() == ShowOutputButton.class) {
                            ((ShowOutputButton) component).updateState();
                        } else if (component.getClass() == ModuleButton.class) {
                            ((ModuleButton) component).updateState();
                        } else if (component.getClass() == EvalButton.class) {
                            ((EvalButton) component).updateState();
                        }
                    }
                }
            }
        }
    }

    public static void updateModuleParameters(Module module) {
        for (Parameter parameter:module.updateAndGetParameters()) {
            parameter.getControl().updateControl();
        }
    }

    public static void populateBasicModules() {
        basicModulesPanel.removeAll();

        // Only modules below an expanded GUISeparator should be displayed
        BooleanP expanded = ((BooleanP) loadSeparator.getParameter(GUISeparator.EXPANDED_BASIC));

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
        basicModulesPanel.add(componentFactory.createBasicSeparator(loadSeparator,basicFrameWidth-80),c);

        // Adding input control options
        if (expanded.isSelected()) {
            c.gridy++;
            JPanel inputPanel = componentFactory.createBasicModuleControl(analysis.getInputControl(), basicFrameWidth - 80);
            if (inputPanel != null) basicModulesPanel.add(inputPanel, c);
        }

        // Adding module buttons
        ModuleCollection modules = getModules();
        for (Module module : modules) {
            // If the module is the special-case GUISeparator, create this module, then return
            JPanel modulePanel = null;
            if (module.getClass().isInstance(new GUISeparator())) {
                // Not all GUI separators are shown on the basic panel
                BooleanP showBasic = (BooleanP) module.getParameter(GUISeparator.SHOW_BASIC);
                if (!showBasic.isSelected()) continue;

                // Adding a blank space before the next separator
                if (expanded.isSelected()) {
                    JPanel blankPanel = new JPanel();
                    blankPanel.setPreferredSize(new Dimension(10, 10));
                    c.gridy++;
                    basicModulesPanel.add(blankPanel, c);
                }

                expanded = (BooleanP) module.getParameter(GUISeparator.EXPANDED_BASIC);
                modulePanel = componentFactory.createBasicSeparator(module, basicFrameWidth-80);
            } else {
                if (module.isRunnable() || module.invalidParameterIsVisible()) {
                    modulePanel = componentFactory.createBasicModuleControl(module, basicFrameWidth - 80);
                }
            }

            if (modulePanel!=null && (expanded.isSelected() || module.getClass().isInstance(new GUISeparator()))) {
                c.gridy++;
                basicModulesPanel.add(modulePanel,c);
            }
        }

        JPanel outputPanel =componentFactory.createBasicModuleControl(analysis.getOutputControl(),basicFrameWidth-80);
        if (outputPanel != null && expanded.isSelected()) {
            c.gridy++;
            basicModulesPanel.add(outputPanel,c);
        }

        c.gridy++;
        c.weighty = 1;
        c.fill = GridBagConstraints.VERTICAL;
        JSeparator separator = new JSeparator();
        separator.setPreferredSize(new Dimension(-1,1));
        basicModulesPanel.add(separator, c);

        basicModulesPanel.revalidate();
        basicModulesPanel.repaint();
        basicModulesScrollPane.revalidate();
        basicModulesScrollPane.repaint();

    }

    private static void listAvailableModules() {
        try {
            addModuleButton.setEnabled(false);
            addModuleButton.setToolTipText("Loading modules");

            Set<Class<? extends Module>> availableModules = new ClassHunter<Module>().getClasses(Module.class,MIA.isDebug());

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

            LinkedHashSet<ModuleListMenu> topList = new LinkedHashSet<>();
            for (String name : modules.keySet()) {
                // ActiveList starts at the top list
                LinkedHashSet<ModuleListMenu> activeList = topList;
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
                        ModuleListMenu newItem = new ModuleListMenu(names[i], new ArrayList<>());
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

        } catch (IllegalAccessException | InstantiationException e){
            e.printStackTrace(System.err);
        }

        addModuleButton.setToolTipText("Add module");
        addModuleButton.setEnabled(true);

    }

    public static void addModule() {
        moduleListMenu.setLocation(MouseInfo.getPointerInfo().getLocation());
        moduleListMenu.setVisible(true);
        updateModules(true);

    }

    public static void removeModule() {
        if (activeModule != null) {
            ModuleCollection modules = getModules();
            // Removing a module resets all the current evaluation
            int idx = modules.indexOf(activeModule);

            if (idx <= lastModuleEval) lastModuleEval = idx - 1;

            modules.remove(activeModule);
            activeModule = null;

            populateModuleList();
            populateModuleParameters();
            populateHelpNotes();
            updateModules(true);

        }
    }

    public static void moveModuleUp() {
        if (activeModule != null) {
            ModuleCollection modules = getModules();
            int idx = modules.indexOf(activeModule);

            if (idx != 0) {
                if (idx - 2 <= lastModuleEval) lastModuleEval = idx - 2;

                modules.remove(activeModule);
                modules.add(idx - 1, activeModule);
                populateModuleList();
                updateModules(true);
            }
        }
    }

    public static void moveModuleDown() {
        if (activeModule != null) {
            ModuleCollection modules = getModules();
            int idx = modules.indexOf(activeModule);

            if (idx < modules.size()-1) {
                if (idx <= lastModuleEval) lastModuleEval = idx - 1;

                modules.remove(activeModule);
                modules.add(idx + 1, activeModule);
                populateModuleList();
                updateModules(true);
            }
        }
    }

    public static JFrame getFrame() {
        return frame;
    }

    public static JPopupMenu getModuleListMenu() {
        return moduleListMenu;
    }

    public static boolean isBasicGUI() {
        return basicGUI;
    }

    public static void setAnalysis(Analysis analysis) {
        GUI.analysis = analysis;
    }

    public static ModuleCollection getModules() {
        return analysis.getModules();
    }

    public static void setProgress(int val) {
        editingProgressBar.setValue(val);
        basicProgressBar.setValue(val);
    }

    public static void updateModules(boolean verbose) {
        int nRunnable = AnalysisTester.testModules(analysis.getModules());
        int nActive = 0;
        for (Module module:analysis.getModules()) if (module.isEnabled()) nActive++;
        int nModules = analysis.getModules().size();
        if (verbose && nModules > 0) System.out.println(nRunnable+" of "+nActive+" active modules are runnable");

        boolean runnable = AnalysisTester.testModule(analysis.getInputControl(),analysis.getModules());
        analysis.getInputControl().setRunnable(runnable);
        inputButton.updateState();
        runnable = AnalysisTester.testModule(analysis.getOutputControl(),analysis.getModules());
        analysis.getInputControl().setRunnable(runnable);
        outputButton.updateState();

        updateButtonStates();

        if (basicGUI) populateBasicModules();
        else {
            populateModuleParameters();
        }

        populateHelpNotes();

    }

    public static void updateTestFile() {
        // Ensuring the input file specified in the InputControl is active in the test workspace
        InputControl inputControl = getAnalysis().getInputControl();
        String inputPath = ((FileFolderPathP) inputControl.getParameter(InputControl.INPUT_PATH)).getPath();
        int nThreads = ((IntegerP) inputControl.getParameter(InputControl.SIMULTANEOUS_JOBS)).getValue();
        Units.setUnits(((ChoiceP) inputControl.getParameter(InputControl.SPATIAL_UNITS)).getChoice());

        if (inputPath == null) return;

        String inputFile = "";
        if (new File(inputPath).isFile()) {
            inputFile = inputPath;
        } else {
            BatchProcessor batchProcessor = new BatchProcessor(new File(inputPath));
            batchProcessor.setnThreads(nThreads);

            // Adding filename filters
            inputControl.addFilenameExtensionFilter(batchProcessor);
            inputControl.addFilenameFilters(batchProcessor);

            // Running the analysis
            File nextFile = batchProcessor.getNextValidFileInStructure();
            if (nextFile == null) {
                inputFile = null;
            } else {
                inputFile = nextFile.getAbsolutePath();
            }
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

        ChoiceP seriesMode = (ChoiceP) analysis.getInputControl().getParameter(InputControl.SERIES_MODE);
        switch (seriesMode.getChoice()) {
            case InputControl.SeriesModes.ALL_SERIES:
                getTestWorkspace().getMetadata().setSeriesNumber(1);
                getTestWorkspace().getMetadata().setSeriesName("");
                break;

            case InputControl.SeriesModes.SERIES_LIST:
                SeriesListSelectorP listParameter = analysis.getInputControl().getParameter(InputControl.SERIES_LIST);
                int[] seriesList = listParameter.getSeriesList();
                getTestWorkspace().getMetadata().setSeriesNumber(seriesList[0]);
                getTestWorkspace().getMetadata().setSeriesName("");
                break;

        }
    }

    public static int getLastModuleEval(){
        return lastModuleEval;
    }

    public static void setLastModuleEval(int lastModuleEval) {
        GUI.lastModuleEval = Math.max(lastModuleEval,-1);
    }

    public static int getModuleBeingEval() {
        return moduleBeingEval;
    }

    public static void setModuleBeingEval(int moduleBeingEval) {
        GUI.moduleBeingEval = moduleBeingEval;
    }

    public static Module getActiveModule() {
        return activeModule;
    }

    public static void setActiveModule(Module activeModule) {
        GUI.activeModule = activeModule;
    }

    public static void evaluateModule(Module module) {
        // Setting the index to the previous module.  This will make the currently-evaluated module go red
        lastModuleEval = getModules().indexOf(module) - 1;
        moduleBeingEval = getModules().indexOf(module);
        updateModules(false);

        Module.setVerbose(true);
        module.execute(testWorkspace);
        lastModuleEval = getModules().indexOf(module);
        moduleBeingEval = -1;

        updateModules(false);

    }

    public static void setTestWorkspace(Workspace testWorkspace) {
        GUI.testWorkspace = testWorkspace;
    }

    public static Analysis getAnalysis() {
        return analysis;
    }

    public static Workspace getTestWorkspace() {
        return testWorkspace;
    }

    public static MeasurementRef getGlobalMeasurementRef() {
        return globalMeasurementRef;
    }

    public static boolean isShowEditingHelpNotes() {
        return showEditingHelpNotes;
    }

    public static void setShowEditingHelpNotes(boolean showEditingHelpNotes) {
        GUI.showEditingHelpNotes = showEditingHelpNotes;
    }

    public static boolean isShowBasicHelpNotes() {
        return showBasicHelpNotes;
    }

    public static void setShowBasicHelpNotes(boolean showBasicHelpNotes) {
        GUI.showBasicHelpNotes = showBasicHelpNotes;
    }

    public static int getEditingFrameWidth() {
        return editingFrameWidth;
    }

    public static void setEditingFrameWidth(int editingFrameWidth) {
        GUI.editingFrameWidth = editingFrameWidth;
    }
}