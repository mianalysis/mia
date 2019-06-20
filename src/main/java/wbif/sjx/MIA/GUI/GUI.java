// TODO: Add controls for all parameter types (hashsets, etc.)
// TODO: If an assigned image/object name is no longer available, flag up the module button in red
// TODO: Output panel could allow the user to select which objects and images to output to the spreadsheet

package wbif.sjx.MIA.GUI;

import ij.IJ;
import org.apache.commons.io.output.TeeOutputStream;
import wbif.sjx.MIA.GUI.ControlObjects.*;
import wbif.sjx.MIA.GUI.ControlObjects.MenuItem;
import wbif.sjx.MIA.Module.Hidden.InputControl;
import wbif.sjx.MIA.GUI.Panels.MainPanels.BasicPanel;
import wbif.sjx.MIA.GUI.Panels.MainPanels.EditingPanel;
import wbif.sjx.MIA.GUI.Panels.MainPanels.MainPanel;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Hidden.OutputControl;
import wbif.sjx.MIA.Module.InputOutput.ImageLoader;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.FileFolderPathP;
import wbif.sjx.MIA.Object.Parameters.IntegerP;
import wbif.sjx.MIA.Object.Parameters.SeriesListSelectorP;
import wbif.sjx.MIA.Process.AnalysisHandling.Analysis;
import wbif.sjx.MIA.Process.AnalysisHandling.AnalysisTester;
import wbif.sjx.MIA.Process.BatchProcessor;
import wbif.sjx.MIA.Process.ClassHunter;
import wbif.sjx.MIA.Process.Logging.Log;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Stephen on 20/05/2017.
 */
public class GUI {
    private static boolean initialised = false;

    private static Analysis analysis = new Analysis();
    private static Module activeModule = null;
    private static int lastModuleEval = -1;
    private static int moduleBeingEval = -1;
    private static Workspace testWorkspace = new Workspace(1, null,1);

    private static int minimumFrameHeight = 600;
    private static int minimumFrameWidth = 400;
    private static int frameHeight = 800;
    private static int elementHeight = 26;
    private static int bigButtonSize = 45;
    private static int moduleButtonWidth = 295;
    private static int statusHeight = 20;

    private static ComponentFactory componentFactory = new ComponentFactory(elementHeight);
    private static JFrame frame = new JFrame();
    private static JMenuBar menuBar = new JMenuBar();
    private static StatusTextField textField = new StatusTextField();
    private static BasicPanel basicPan = new BasicPanel();
    private static EditingPanel editingPan;
    private static MainPanel mainPanel;
    private static MenuCheckbox helpNotesCheckbox = new MenuCheckbox(MenuCheckbox.TOGGLE_HELP_NOTES);
    private static TreeMap<String,Module> availableModules = new TreeMap<>();


    public GUI() throws InstantiationException, IllegalAccessException {
        // Only create a GUI if one hasn't already been created
        if (initialised) {
            frame.setVisible(true);
            return;
        }
        initialised = true;

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        MIA.log.writeDebug("test");
        Splash splash = new Splash();
        splash.setLocation((screenSize.width - splash.getWidth()) / 2, (screenSize.height - splash.getHeight()) / 2);
        splash.setVisible(true);

        MIA.log.writeDebug("test2");
        detectAvailableModules();

        editingPan = new EditingPanel();
        basicPan = new BasicPanel();

        // Adding a new ImageLoader module to the empty analysis
        analysis.getModules().add(new ImageLoader<>(getModules()));

        // Determining which panel should be shown
        if (MIA.isDebug()) mainPanel = editingPan;
        else mainPanel = basicPan;

        initialiseStatusTextField();
        initialiseMenuBar();

        frame.setTitle("MIA (version " + MIA.getVersion() + ")");
        frame.setJMenuBar(menuBar);
        frame.add(mainPanel);
        frame.setPreferredSize(new Dimension(mainPanel.getPreferredWidth(),mainPanel.getPreferredHeight()));
        frame.setIconImage(new ImageIcon(this.getClass().getResource("/Icons/Logo_wide_32.png"),"").getImage());

        mainPanel.updatePanel();

        // Final bits for listeners
        frame.pack();
        frame.setVisible(true);
        frame.setLocation((screenSize.width - mainPanel.getPreferredWidth()) / 2, (screenSize.height - frameHeight) / 2);

        updatePanel();

    }

    void detectAvailableModules() {
        try {
            Set<Class<? extends Module>> detectedModules = new ClassHunter<Module>().getClasses(Module.class, MIA.isDebug());

            // Creating an alphabetically-ordered list of all modules
            ModuleCollection moduleCollection = new ModuleCollection();
            availableModules = new TreeMap<>();
            for (Class clazz : detectedModules) {
                if (clazz != InputControl.class && clazz != OutputControl.class) {
                    // Skip any abstract Modules
                    if (Modifier.isAbstract(clazz.getModifiers())) continue;

                    Constructor constructor = clazz.getDeclaredConstructor(ModuleCollection.class);
                    Module module = (Module) constructor.newInstance(moduleCollection);
                    String packageName = module.getPackageName();
                    String moduleName = module.getName();
                    availableModules.put(packageName + moduleName, module);

                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private static void initialiseMenuBar() {
        // Creating the file menu
        JMenu menu = new JMenu("File");
        menu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        menuBar.add(menu);
        menu.add(new MenuItem(MenuItem.NEW_PIPELINE));
        menu.add(new MenuItem(MenuItem.LOAD_PIPELINE));
        menu.add(new MenuItem(MenuItem.SAVE_PIPELINE));

        // Creating the edit menu
        menu = new JMenu("Edit");
        menu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        menuBar.add(menu);
        menu.add(new MenuItem(MenuItem.RESET_ANALYSIS));
        menu.add(new MenuItem(MenuItem.ENABLE_ALL));
        menu.add(new MenuItem(MenuItem.DISABLE_ALL));
        menu.add(new MenuItem(MenuItem.OUTPUT_ALL));
        menu.add(new MenuItem(MenuItem.SILENCE_ALL));

        // Creating the analysis menu
        menu = new JMenu("Analysis");
        menu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        menuBar.add(menu);
        menu.add(new MenuItem(MenuItem.RUN_ANALYSIS));
        menu.add(new MenuItem(MenuItem.STOP_ANALYSIS));

        // Creating the new menu
        menu = new JMenu("View");
        menu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        menuBar.add(menu);
        if (MIA.isDebug()) {
            menu.add(new MenuItem(MenuItem.BASIC_VIEW));
        } else {
            menu.add(new MenuItem(MenuItem.EDITING_VIEW));
        }
        menu.add(new MenuItem(MenuItem.SHOW_GLOBAL_VARIABLES));
        helpNotesCheckbox.setSelected(showHelpNotes());
        menu.add(helpNotesCheckbox);

        // Creating the help menu
        menu = new JMenu("Help");
        menuBar.add(menu);
        menu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        menu.add(new MenuItem(MenuItem.SHOW_ABOUT));
        menu.add(new MenuItem(MenuItem.SHOW_GETTING_STARTED));

        JMenu logMenu = new JMenu("Logging");
        menu.add(logMenu);
        logMenu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        Log.Level level = Log.Level.MESSAGE;
        MenuLogCheckbox menuLogCheckbox = new MenuLogCheckbox(level,MIA.log.isWriteEnabled(level));
        logMenu.add(menuLogCheckbox);

        level = Log.Level.WARNING;
        menuLogCheckbox = new MenuLogCheckbox(level,MIA.log.isWriteEnabled(level));
        logMenu.add(menuLogCheckbox);

//        Enabling/disabling errors is disabled, because these should always be present
//        level = Log.Level.ERROR;
//        menuLogCheckbox = new MenuLogCheckbox(level,MIA.log.isWriteEnabled(level));
//        logMenu.add(menuLogCheckbox);

        level = Log.Level.DEBUG;
        menuLogCheckbox = new MenuLogCheckbox(level,MIA.log.isWriteEnabled(level));
        logMenu.add(menuLogCheckbox);

        level = Log.Level.MEMORY;
        menuLogCheckbox = new MenuLogCheckbox(level,MIA.log.isWriteEnabled(level));
        logMenu.add(menuLogCheckbox);

        menuBar.add(Box.createHorizontalGlue());
        menu = new JMenu("");
        menuBar.add(menu);
        menu.add(new MenuItem(MenuItem.SHOW_PONY));

    }

    public static void updatePanel() {
        int preferredWidth = mainPanel.getPreferredWidth();
        int preferredHeight = mainPanel.getPreferredHeight();
        frame.setPreferredSize(new Dimension(preferredWidth,preferredHeight));

        int minimumWidth = mainPanel.getMinimumWidth();
        int minimumHeight = mainPanel.getMinimumHeight();
        frame.setMinimumSize(new Dimension(minimumWidth,minimumHeight));

        helpNotesCheckbox.setSelected(showHelpNotes());

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

        TeeOutputStream teeOutputStream = new TeeOutputStream(System.out,guiPrintStream);
        System.setOut(new PrintStream(teeOutputStream));

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

    public static void updateModuleStates(boolean verbose) {
        int nRunnable = AnalysisTester.testModules(getModules());
        int nActive = 0;
        for (Module module:getModules()) if (module.isEnabled()) nActive++;
        int nModules = getModules().size();
        if (verbose && nModules > 0) System.out.println(nRunnable+" of "+nActive+" active modules are runnable");

        mainPanel.updateModuleStates();

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

    public static void updateModules() {
        mainPanel.updateModules();
        mainPanel.updateHelpNotes();

    }

    public static void updateParameters() {
        mainPanel.updateParameters();
    }

    public static void updateTestFile() {
        // Ensuring the input file specified in the InputControl is active in the test workspace
        InputControl inputControl = analysis.getModules().getInputControl();
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

        if (testWorkspace.getMetadata().getFile() == null) {
            lastModuleEval = -1;
            testWorkspace = new Workspace(1, new File(inputFile),1);
        }

        // If the input path isn't the same assign this new file
        if (!testWorkspace.getMetadata().getFile().getAbsolutePath().equals(inputFile)) {
            lastModuleEval = -1;
            testWorkspace = new Workspace(1, new File(inputFile),1);

        }

        ChoiceP seriesMode = (ChoiceP) inputControl.getParameter(InputControl.SERIES_MODE);
        switch (seriesMode.getChoice()) {
            case InputControl.SeriesModes.ALL_SERIES:
                testWorkspace.getMetadata().setSeriesNumber(1);
                testWorkspace.getMetadata().setSeriesName("");
                break;

            case InputControl.SeriesModes.SERIES_LIST:
                SeriesListSelectorP listParameter = inputControl.getParameter(InputControl.SERIES_LIST);
                int[] seriesList = listParameter.getSeriesList();
                testWorkspace.getMetadata().setSeriesNumber(seriesList[0]);
                testWorkspace.getMetadata().setSeriesName("");
                break;

        }
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

    public static void setShowEditingHelpNotes(boolean showEditingHelpNotes) {
        mainPanel.setShowHelpNotes(showEditingHelpNotes);
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

    public static TreeMap<String, Module> getAvailableModules() {
        return availableModules;
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