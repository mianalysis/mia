// TODO: Add controls for all parameter types (hashsets, etc.)
// TODO: If an assigned image/object name is no longer available, flag up the module button in red
// TODO: Output panel could allow the user to select which objects and images to output to the spreadsheet

package wbif.sjx.ModularImageAnalysis.GUI;

import org.apache.commons.io.output.TeeOutputStream;
import wbif.sjx.ModularImageAnalysis.GUI.ControlObjects.*;
import wbif.sjx.ModularImageAnalysis.GUI.Panels.*;
import wbif.sjx.ModularImageAnalysis.GUI.Panels.MainPanels.BasicPanel;
import wbif.sjx.ModularImageAnalysis.GUI.Panels.MainPanels.EditingPanel;
import wbif.sjx.ModularImageAnalysis.MIA;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;
import wbif.sjx.ModularImageAnalysis.Process.AnalysisHandling.Analysis;
import wbif.sjx.ModularImageAnalysis.Process.AnalysisHandling.AnalysisTester;

import javax.swing.*;
import java.awt.*;
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
    private static Workspace testWorkspace = new Workspace(1, null,1);
    private static final MeasurementRef globalMeasurementRef = new MeasurementRef("Global");

    private static int minimumFrameHeight = 600;
    private static int minimumFrameWidth = 400;
    private static int frameHeight = 800;
    private static int elementHeight = 26;
    private static int bigButtonSize = 45;
    private static int moduleButtonWidth = 295;
    private static int statusHeight = 20;

    private static boolean initialised = false;

    private static ComponentFactory componentFactory = new ComponentFactory(elementHeight);
    private static final JFrame frame = new JFrame();
    private static final JMenuBar menuBar = new JMenuBar();
    private static final ButtonGroup moduleGroup = new ButtonGroup();
    private static final StatusTextField textField = new StatusTextField();
    private static final BasicPanel basicPan = new BasicPanel();
    private static final EditingPanel editingPan = new EditingPanel();
    private static MainPanel mainPanel;


    public GUI() throws InstantiationException, IllegalAccessException {
        // Only create a GUI if one hasn't already been created
        if (initialised) {
            frame.setVisible(true);
            return;
        }
        initialised = true;

        if (MIA.isDebug()) {
            mainPanel = editingPan;
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        } else {
            mainPanel = basicPan;
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        }

        initialiseStatusTextField();
        initialiseMenuBar();

        frame.setTitle("MIA (version " + MIA.getVersion() + ")");
        frame.setJMenuBar(menuBar);
        frame.add(mainPanel);
        frame.setPreferredSize(new Dimension(mainPanel.getPreferredWidth(),mainPanel.getPreferredHeight()));

        mainPanel.updatePanel();

        // Final bits for listeners
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.pack();
        frame.setVisible(true);
        frame.setLocation((screenSize.width - mainPanel.getPreferredWidth()) / 2, (screenSize.height - frameHeight) / 2);

    }

    private static void initialiseMenuBar() {
        // Creating the file menu
        JMenu menu = new JMenu("File");
        menu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        menuBar.add(menu);
        menu.add(new AnalysisMenuItem(AnalysisMenuItem.NEW_PIPELINE));
        menu.add(new AnalysisMenuItem(AnalysisMenuItem.LOAD_PIPELINE));
        menu.add(new AnalysisMenuItem(AnalysisMenuItem.SAVE_PIPELINE));

        // Creating the edit menu
        menu = new JMenu("Edit");
        menu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        menuBar.add(menu);
        menu.add(new AnalysisMenuItem(AnalysisMenuItem.RESET_ANALYSIS));
        menu.add(new AnalysisMenuItem(AnalysisMenuItem.ENABLE_ALL));
        menu.add(new AnalysisMenuItem(AnalysisMenuItem.DISABLE_ALL));
        menu.add(new AnalysisMenuItem(AnalysisMenuItem.OUTPUT_ALL));
        menu.add(new AnalysisMenuItem(AnalysisMenuItem.SILENCE_ALL));

        // Creating the analysis menu
        menu = new JMenu("Analysis");
        menu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        menuBar.add(menu);
        menu.add(new AnalysisMenuItem(AnalysisMenuItem.RUN_ANALYSIS));
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

    public static void updatePanel() {
        int preferredWidth = mainPanel.getPreferredWidth();
        int preferredHeight = mainPanel.getPreferredHeight();
        frame.setPreferredSize(new Dimension(preferredWidth,preferredHeight));

        int minimumWidth = mainPanel.getMinimumWidth();
        int minimumHeight = mainPanel.getMinimumHeight();
        frame.setMinimumSize(new Dimension(minimumWidth,minimumHeight));

        frame.pack();
        frame.revalidate();
        frame.repaint();

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

    public static void populateModuleList() {
        mainPanel.updateModules();
    }

    public static void populateModuleParameters() {
        mainPanel.updateParameters();
    }

    public static void populateHelpNotes() {
        mainPanel.updateHelpNotes();
    }

    public static void updateModuleParameters(Module module) {
        for (Parameter parameter:module.updateAndGetParameters()) {
            parameter.getControl().updateControl();
        }
    }

    public static ComponentFactory getComponentFactory() {
        return componentFactory;
    }

    public static JFrame getFrame() {
        return frame;
    }

    public static boolean isBasicGUI() {
        return mainPanel == basicPan;
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

    public static Workspace getTestWorkspace() {
        return testWorkspace;
    }

    public static void setTestWorkspace(Workspace testWorkspace) {
        GUI.testWorkspace =  testWorkspace;
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
        editingPan.setProgress(mainPanel.getProgress());

        frame.remove(mainPanel);
        mainPanel = editingPan;
        frame.add(mainPanel);

        mainPanel.updatePanel();
        updatePanel();

    }

    public static void enableBasicMode() {
        basicPan.setProgress(mainPanel.getProgress());

        frame.remove(mainPanel);
        mainPanel = basicPan;
        frame.add(mainPanel);

        mainPanel.updatePanel();
        updatePanel();

    }

    public static void setShowHelpNotes(boolean showHelpNotes) {
        mainPanel.setShowHelpNotes(showHelpNotes);

    }

    public static boolean showHelpNotes() {
        return mainPanel.showHelpNotes();
    }


    // COMPONENT SIZE GETTERS

    public static int getMinimumFrameWidth() {
        return minimumFrameWidth;
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