// TODO: Add controls for all parameter types (hashsets, etc.)
// TODO: If an assigned image/object name is no longer available, flag up the module button in red
// TODO: Output panel could allow the user to select which objects and images to output to the spreadsheet

package wbif.sjx.ModularImageAnalysis.GUI.Layouts;

import ij.Prefs;
import org.apache.commons.io.output.TeeOutputStream;
import wbif.sjx.ModularImageAnalysis.GUI.ComponentFactory;
import wbif.sjx.ModularImageAnalysis.GUI.ControlObjects.*;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.InputControl;
import wbif.sjx.ModularImageAnalysis.GUI.Panels.*;
import wbif.sjx.ModularImageAnalysis.MIA;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;
import wbif.sjx.ModularImageAnalysis.Process.AnalysisHandling.Analysis;
import wbif.sjx.ModularImageAnalysis.Process.AnalysisHandling.AnalysisTester;
import wbif.sjx.ModularImageAnalysis.Process.BatchProcessor;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.io.File;
import java.io.PrintStream;

/**
 * Created by Stephen on 20/05/2017.
 */
public class GUI {
    private static Analysis analysis = new Analysis();
    private static Module activeModule = null;
    private static Module lastEditingHelpNotesModule = null;
    private static Module lastBasicHelpNotesModule = null;
    private static int lastModuleEval = -1;
    private static int moduleBeingEval = -1;
    private static final MeasurementRef globalMeasurementRef = new MeasurementRef("Global");

    private static int basicFrameWidth = 400;
    private static int minimumFrameHeight = 600;
    private static int frameHeight = 800;
    private static int elementHeight = 26;
    private static int bigButtonSize = 45;
    private static int moduleButtonWidth = 295;
    private static int statusHeight = 20;

    private static boolean initialised = false;
    private static boolean basicGUI = true;
    private static boolean showBasicHelpNotes = Prefs.get("MIA.showBasicHelpNotes",true);

    private static ComponentFactory componentFactory = new ComponentFactory(elementHeight);
    private static final JFrame frame = new JFrame();
    private static final JMenuBar menuBar = new JMenuBar();
    private static final ButtonGroup moduleGroup = new ButtonGroup();
    private static final StatusTextField textField = new StatusTextField();

    private static final JPanel basicPanel = new JPanel();
//    private static final JPanel editingPanel = new JPanel();

    private static final EditingPanel editingPan = new EditingPanel();
    private static MainPanel mainPanel;

    private static final StatusPanel basicStatusPanel = new StatusPanel();
    private static final BasicControlPanel basicControlPanel = new BasicControlPanel();
    private static final ProgressBarPanel basicProgressBarPanel = new ProgressBarPanel();
    private static final JPanel basicHelpNotesPanel = new JPanel();
    private static final HelpPanel basicHelpPanel = new HelpPanel();
    private static final NotesPanel basicNotesPanel = new NotesPanel();


    public GUI() throws InstantiationException, IllegalAccessException {
        // Only create a GUI if one hasn't already been created
        if (initialised) {
            frame.setVisible(true);
            return;
        }
        initialised = true;

        // Setting location of panel
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setTitle("MIA (version " + MIA.getVersion() + ")");

        initialiseStatusTextField();

        // Creating the menu bar
        initialiseMenuBar();
        frame.setJMenuBar(menuBar);

        if (MIA.isDebug()) {

        } else {
            mainPanel = editingPan;
        }

        mainPanel.updatePanel();

//        if (MIA.isDebug()) {
//            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//            renderEditingMode();
//        } else {
//            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
//            renderBasicMode();
//        }

        // Final bits for listeners
        frame.setVisible(true);

        frame.setLocation((screenSize.width - mainPanel.getPreferredWidth()) / 2, (screenSize.height - frameHeight) / 2);

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
            mainPanel = editingPan;
        }

        mainPanel.updatePanel();

        int preferredWidth = mainPanel.getPreferredWidth();
        int preferredHeight = mainPanel.getPreferredWidth();
        frame.setPreferredSize(new Dimension(preferredWidth,preferredHeight));

        int minimumWidth = mainPanel.getMinimumWidth();
        int minimumHeight = mainPanel.getMinimumHeight();
        frame.setMinimumSize(new Dimension(minimumWidth,minimumHeight));

        frame.pack();
        frame.revalidate();
        frame.repaint();

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
        c.gridy++;
        c.weighty = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.BOTH;
        basicPanel.add(basicControlPanel, c);

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
        basicPanel.add(basicStatusPanel,c);

        // Initialising the progress bar
        c.gridy++;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5,5,5,5);
        basicPanel.add(basicProgressBarPanel,c);

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

        frame.remove(editingPan);
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

    private static void initialiseBasicHelpNotesPanels() {
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

    public static void populateModuleList() {
        mainPanel.updateModules();
    }

    public static void populateModuleParameters() {
        mainPanel.updateParameters();
    }

    public static void populateHelpNotes() {
        mainPanel.updateHelpNotes();
    }

    public static void populateBasicHelpNotes() {
        // If null, show a special message
        if (activeModule == null) {
            basicHelpPanel.showUsageMessage();
            basicNotesPanel.updatePanel();
            return;
        }

        // Only update the help and notes if the module has changed
        if (activeModule != lastBasicHelpNotesModule) {
            lastBasicHelpNotesModule = activeModule;
        } else {
            return;
        }

        basicHelpPanel.updatePanel();
        basicNotesPanel.updatePanel();

    }

    public static void updateModuleParameters(Module module) {
        for (Parameter parameter:module.updateAndGetParameters()) {
            parameter.getControl().updateControl();
        }
    }

    public static void populateBasicModules() {
        basicControlPanel.updatePanel();
    }

    public static ComponentFactory getComponentFactory() {
        return componentFactory;
    }

    public static JFrame getFrame() {
        return frame;
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
        mainPanel.setProgress(val);
    }

    public static void updateModules(boolean verbose) {
        int nRunnable = AnalysisTester.testModules(analysis.getModules());
        int nActive = 0;
        for (Module module:analysis.getModules()) if (module.isEnabled()) nActive++;
        int nModules = analysis.getModules().size();
        if (verbose && nModules > 0) System.out.println(nRunnable+" of "+nActive+" active modules are runnable");

        mainPanel.updateModules();
        mainPanel.updateHelpNotes();

    }

    public static void updateTestFile() {
        mainPanel.updateTestFile();
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

    public static Analysis getAnalysis() {
        return analysis;
    }

    public static MeasurementRef getGlobalMeasurementRef() {
        return globalMeasurementRef;
    }

    public static void setShowEditingHelpNotes(boolean showEditingHelpNotes) {
        mainPanel.setShowHelpNotes(showEditingHelpNotes);
    }

    public static boolean isShowBasicHelpNotes() {
        return showBasicHelpNotes;
    }

    public static void setShowBasicHelpNotes(boolean showBasicHelpNotes) {
        GUI.showBasicHelpNotes = showBasicHelpNotes;
    }

    public static Module getLastEditingHelpNotesModule() {
        return lastEditingHelpNotesModule;
    }

    public static void setLastEditingHelpNotesModule(Module lastEditingHelpNotesModule) {
        GUI.lastEditingHelpNotesModule = lastEditingHelpNotesModule;
    }

    public static Module getLastBasicHelpNotesModule() {
        return lastBasicHelpNotesModule;
    }

    public static void setLastBasicHelpNotesModule(Module lastBasicHelpNotesModule) {
        GUI.lastBasicHelpNotesModule = lastBasicHelpNotesModule;
    }

    public static ButtonGroup getModuleGroup() {
        return moduleGroup;
    }

    public static StatusTextField getTextField() {
        return textField;
    }

    public static void enableEditingMode() {
        frame.remove(mainPanel);

        mainPanel = editingPan;

        frame.add(mainPanel);
    }

    public static void setShowHelpNotes(boolean showHelpNotes) {
        mainPanel.setShowHelpNotes(showHelpNotes);
    }

    public static boolean showHelpNotes() {
        return mainPanel.showHelpNotes();
    }

    // COMPONENT SIZE GETTERS

    public static int getBasicFrameWidth() {
        return basicFrameWidth;
    }

    public static int getMinimumFrameHeight() {
        return minimumFrameHeight;
    }

    public static int getFrameHeight() {
        return frameHeight;
    }

    public static int getElementHeight() {
        return elementHeight;
    }

    public static int getBigButtonSize() {
        return bigButtonSize;
    }

    public static int getModuleButtonWidth() {
        return moduleButtonWidth;
    }

    public static int getStatusHeight() {
        return statusHeight;
    }
}