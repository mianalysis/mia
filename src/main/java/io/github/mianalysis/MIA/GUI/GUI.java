package io.github.mianalysis.MIA.GUI;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.TreeMap;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.ToolTipManager;

import io.github.mianalysis.MIA.MIA;
import io.github.mianalysis.MIA.GUI.Regions.Abstract.AbstractPanel;
import io.github.mianalysis.MIA.GUI.Regions.BasicPanel.BasicPanel;
import io.github.mianalysis.MIA.GUI.Regions.EditingPanel.EditingPanel;
import io.github.mianalysis.MIA.GUI.Regions.MenuBar.CustomMenuBar;
import io.github.mianalysis.MIA.GUI.Regions.ProgressAndStatus.StatusTextField;
import io.github.mianalysis.MIA.Macro.MacroHandler;
import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Module.Modules;
import io.github.mianalysis.MIA.Module.Core.InputControl;
import io.github.mianalysis.MIA.Module.Core.OutputControl;
import io.github.mianalysis.MIA.Module.InputOutput.ImageLoader;
import io.github.mianalysis.MIA.Object.Workspace;
import io.github.mianalysis.MIA.Object.Workspaces;
import io.github.mianalysis.MIA.Object.Parameters.ChoiceP;
import io.github.mianalysis.MIA.Object.Parameters.FileFolderPathP;
import io.github.mianalysis.MIA.Object.Units.SpatialUnit;
import io.github.mianalysis.MIA.Object.Units.TemporalUnit;
import io.github.mianalysis.MIA.Process.ClassHunter;
import io.github.mianalysis.MIA.Process.AnalysisHandling.Analysis;
import io.github.mianalysis.MIA.Process.AnalysisHandling.AnalysisRunner;
import io.github.mianalysis.MIA.Process.AnalysisHandling.AnalysisTester;
import io.github.mianalysis.MIA.Process.Logging.StatusPanelRenderer;
import io.github.sjcross.common.System.FileCrawler;

/**
 * Created by Stephen on 20/05/2017.
 */
public class GUI {
    private static boolean initialised = false;

    private static Analysis analysis = new Analysis();
    private static AnalysisRunner analysisRunner = new AnalysisRunner();
    private static Module[] selectedModules = null;
    private static int lastModuleEval = -1;
    private static int moduleBeingEval = -1;
    private static Workspace testWorkspace = new Workspaces().getNewWorkspace(null, 1);
    private static UndoRedoStore undoRedoStore = new UndoRedoStore();

    private static int minimumFrameHeight = 600;
    private static int frameHeight = 850;
    private static int elementHeight = 26;
    private static int bigButtonSize = 45;
    private static int moduleButtonWidth = 295;
    private static int statusHeight = 20;

    private static ComponentFactory componentFactory = new ComponentFactory(elementHeight);
    private static JFrame frame = new JFrame();
    private static CustomMenuBar menuBar = new CustomMenuBar();
    private static StatusTextField textField = new StatusTextField();
    private static BasicPanel basicPan = new BasicPanel();
    private static EditingPanel editingPan;
    private static AbstractPanel mainPanel;
    private static Modules availableModules = new Modules();

    public GUI() throws InstantiationException, IllegalAccessException {
        // Only create a GUI if one hasn't already been created
        if (initialised) {
            frame.setVisible(true);
            return;
        }
        initialised = true;

        // Creating main Frame
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frameHeight = Math.min(frameHeight, screenSize.height - 50);

        Splash splash = new Splash();
        splash.setLocation((screenSize.width - splash.getWidth()) / 2, (screenSize.height - splash.getHeight()) / 2);
        splash.setVisible(true);

        // Detecting modules
        splash.setStatus(Splash.Status.DETECTING_MODULES);
        List<String> detectedModules = ClassHunter.getModules(false);

        splash.setStatus(Splash.Status.INITIALISING_MODULES);
        initialiseAvailableModules(detectedModules);

        splash.setStatus(Splash.Status.CREATING_INTERFACE);
        basicPan = new BasicPanel();
        editingPan = new EditingPanel();

        // Adding a new ImageLoader module to the empty analysis
        analysis.getModules().add(new ImageLoader<>(getModules()));

        // Determining which panel should be shown
        if (MIA.isDebug())
            mainPanel = editingPan;
        else
            mainPanel = basicPan;

        initialiseStatusTextField();
        frame.setTitle("MIA (version " + MIA.getVersion() + ")");
        frame.setJMenuBar(menuBar);
        frame.add(mainPanel);
        frame.setPreferredSize(new Dimension(mainPanel.getPreferredWidth(), mainPanel.getPreferredHeight()));
        frame.setIconImage(new ImageIcon(this.getClass().getResource("/Icons/Logo_wide_32.png"), "").getImage());

        mainPanel.updatePanel();
        menuBar.setUndoRedoStatus(undoRedoStore);
        splash.setVisible(false);

        // Final bits for listeners
        frame.pack();
        frame.setVisible(true);
        frame.setLocation((screenSize.width - mainPanel.getPreferredWidth()) / 2,
                (screenSize.height - frameHeight) / 2);

        updatePanel();

        // Setting macro
        MacroHandler.setWorkspace(GUI.getTestWorkspace());
        MacroHandler.setModules(GUI.getModules());

        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);

    }

    void initialiseAvailableModules(List<String> detectedModuleNames) {
        // Creating an alphabetically-ordered list of all modules
        for (String detectedModuleName : detectedModuleNames) {
            try {
                Class<? extends Module> clazz = (Class<? extends Module>) Class.forName(detectedModuleName);
                if (clazz != InputControl.class && clazz != OutputControl.class) {
                    // Skip any abstract Modules
                    if (Modifier.isAbstract(clazz.getModifiers()))
                        continue;

                    Constructor constructor = clazz.getDeclaredConstructor(Modules.class);
                    Module module = (Module) constructor.newInstance(availableModules);
                    availableModules.add(module);

                }
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException
                    | InvocationTargetException | NoClassDefFoundError e) {
                MIA.log.writeWarning("Module \"" + detectedModuleName + "\" incompatible with MIA v" + MIA.getVersion()
                        + ".  Module not loaded.");
                e.printStackTrace();
            }
        }
    }

    public static void updatePanel() {
        int preferredWidth = mainPanel.getPreferredWidth();
        int preferredHeight = mainPanel.getPreferredHeight();
        frame.setPreferredSize(new Dimension(preferredWidth, preferredHeight));

        int minimumWidth = mainPanel.getMinimumWidth();
        int minimumHeight = mainPanel.getMinimumHeight();
        frame.setMinimumSize(new Dimension(minimumWidth, minimumHeight));

        menuBar.setHelpSelected(showHelp());
        menuBar.setNotesSelected(showNotes());
        menuBar.setFileListSelected(showFileList());
        menuBar.setSearchSelected(showSearch());

        frame.pack();
        frame.revalidate();
        frame.repaint();

    }

    private static void initialiseStatusTextField() {
        textField.setPreferredSize(new Dimension(Integer.MAX_VALUE, statusHeight));
        textField.setBorder(null);
        textField.setText("MIA (version " + MIA.getVersion() + ")");
        textField.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        textField.setToolTipText(textField.getText());
        textField.setOpaque(false);

        StatusPanelRenderer statusPanelRenderer = new StatusPanelRenderer(textField);
        MIA.log.addRenderer(statusPanelRenderer);

    }

    public static void updateAvailableModules() {
        mainPanel.updateAvailableModules();
    }

    public static void updateModuleList() {
        mainPanel.updateModules();
    }

    public static void updateHelpNotes() {
        mainPanel.updateHelpNotes();
    }

    public static void updateFileList() {
        mainPanel.updateFileList();
    }

    public static void updateModuleStates(boolean verbose) {
        AnalysisTester.testModules(getModules());
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
        MacroHandler.setModules(analysis.getModules());
    }

    public static Modules getModules() {
        return analysis.getModules();
    }

    public synchronized static void updateProgressBar(int val) {
        mainPanel.setProgress(val);
    }

    public synchronized static void updateProgressBar() {
        Workspaces workspaces = analysisRunner.getWorkspaces();

        updateProgressBar((int) Math.round(workspaces.getOverallProgress() * 100));

    }

    public static void updateModules() {
        mainPanel.updateModules();
        mainPanel.updateHelpNotes();

    }

    public static void updateParameters() {
        mainPanel.updateParameters();
    }

    public static void updateTestFile(boolean verbose) {
        // Ensuring the input file specified in the InputControl is active in the test
        // workspace
        InputControl inputControl = analysis.getModules().getInputControl();
        String inputPath = ((FileFolderPathP) inputControl.getParameter(InputControl.INPUT_PATH)).getPath();
        SpatialUnit.setUnit(((ChoiceP) inputControl.getParameter(InputControl.SPATIAL_UNIT)).getChoice());
        TemporalUnit.setUnit(((ChoiceP) inputControl.getParameter(InputControl.TEMPORAL_UNIT)).getChoice());

        // Getting the next file
        File nextFile = null;
        if (inputPath != null) {
            if (new File(inputPath).isFile()) {
                nextFile = new File(inputPath);
            } else {
                if (inputPath != null && !inputPath.equals("")) {
                    FileCrawler fileCrawler = new FileCrawler(new File(inputPath));
                    inputControl.addFilenameFilters(fileCrawler);
                    nextFile = fileCrawler.getNextValidFileInStructure();
                }
            }
        }

        if (nextFile == null)
            return;

        // Getting the next series
        TreeMap<Integer, String> seriesNumbers = inputControl.getSeriesNumbers(nextFile);
        if (seriesNumbers.size() == 0)
            return;
        int nextSeriesNumber = seriesNumbers.firstEntry().getKey();
        String nextSeriesName = seriesNumbers.get(nextSeriesNumber);

        // If the new file is the same as the old, skip this
        File previousFile = testWorkspace.getMetadata().getFile();
        int previousSeries = testWorkspace.getMetadata().getSeriesNumber();

        if (previousFile != null && previousFile.getAbsolutePath().equals(nextFile.getAbsolutePath())
                && previousSeries == nextSeriesNumber)
            return;

        lastModuleEval = -1;
        testWorkspace = new Workspaces().getNewWorkspace(nextFile, nextSeriesNumber);
        testWorkspace.getMetadata().setSeriesName(nextSeriesName);

        // Setting macro
        MacroHandler.setWorkspace(testWorkspace);

    }

    public static int getLastModuleEval() {
        return lastModuleEval;
    }

    public static void setLastModuleEval(int lastModuleEval) {
        GUI.lastModuleEval = Math.max(lastModuleEval, -1);
    }

    public static Workspace getTestWorkspace() {
        return testWorkspace;
    }

    public static void setTestWorkspace(Workspace testWorkspace) {
        GUI.testWorkspace = testWorkspace;
    }

    public static int getModuleBeingEval() {
        return moduleBeingEval;
    }

    public static void setModuleBeingEval(int moduleBeingEval) {
        GUI.moduleBeingEval = moduleBeingEval;
    }

    public static Module getFirstSelectedModule() {
        if (selectedModules == null || selectedModules.length == 0)
            return null;
        return selectedModules[0];
    }

    public static Module[] getSelectedModules() {
        return selectedModules;
    }

    public static int[] getSelectedModuleIndices() {
        if (selectedModules == null || selectedModules.length == 0)
            return new int[0];

        int[] selectedIndices = new int[selectedModules.length];
        Modules modules = getModules();

        for (int i = 0; i < selectedModules.length; i++) {
            Module selectedModule = selectedModules[i];
            if (selectedModule instanceof InputControl) {
                selectedIndices[i] = -1;
            } else if (selectedModule instanceof OutputControl) {
                selectedIndices[i] = -2;
            } else {
                selectedIndices[i] = modules.indexOf(selectedModule);
            }
        }

        return selectedIndices;

    }

    public static void setSelectedModulesByIndex(int[] selectedModuleIndices) {
        if (selectedModuleIndices == null || selectedModuleIndices.length == 0)
            return;

        // The input and output controls are special cases
        if (selectedModuleIndices.length == 1 && selectedModuleIndices[0] == -1) {
            selectedModules = new Module[] { analysis.getModules().getInputControl() };
            return;
        } else if (selectedModuleIndices.length == 1 && selectedModuleIndices[0] == -2) {
            selectedModules = new Module[] { analysis.getModules().getOutputControl() };
            return;
        }

        // If the largest index is out of range disable all selections
        if (selectedModuleIndices[selectedModuleIndices.length - 1] >= analysis.getModules().size()) {
            selectedModules = new Module[0];
            return;
        }

        selectedModules = new Module[selectedModuleIndices.length];
        for (int i = 0; i < selectedModuleIndices.length; i++) {
            if (selectedModuleIndices[i] == -1) {
                selectedModules[i] = analysis.getModules().getInputControl();
            } else {
                selectedModules[i] = analysis.getModules().get(selectedModuleIndices[i]);
            }
        }
    }

    public static void setSelectedModules(Module[] selectedModules) {
        GUI.selectedModules = selectedModules;
    }

    public static Analysis getAnalysis() {
        return analysis;
    }

    public static AnalysisRunner getAnalysisRunner() {
        return analysisRunner;
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

    public static void setShowHelp(boolean showHelp) {
        mainPanel.setShowHelp(showHelp);

    }

    public static boolean showHelp() {
        if (mainPanel == null)
            return false;
        return mainPanel.showHelp();
    }

    public static void setShowNotes(boolean showNotes) {
        mainPanel.setShowNotes(showNotes);

    }

    public static boolean showNotes() {
        if (mainPanel == null)
            return false;
        return mainPanel.showNotes();
    }

    public static void setShowFileList(boolean showFileList) {
        mainPanel.setShowFileList(showFileList);
    }

    public static boolean showFileList() {
        if (mainPanel == null)
            return false;
        return mainPanel.showFileList();
    }

    public static void setShowSearch(boolean showSearch) {
        mainPanel.setShowSearch(showSearch);
    }

    public static boolean showSearch() {
        if (mainPanel == null)
            return false;
        return mainPanel.showSearch();
    }

    public static void resetJobNumbers() {
        mainPanel.resetJobNumbers();
    }

    public static Modules getAvailableModules() {
        return availableModules;
    }

    public static UndoRedoStore getUndoRedoStore() {
        return undoRedoStore;
    }

    public static void addUndo() {
        undoRedoStore.addUndo(analysis.getModules());
        menuBar.setUndoRedoStatus(undoRedoStore);
    }

    public static void undo() {
        int[] selectedIndices = getSelectedModuleIndices();

        Modules newModules = undoRedoStore.getNextUndo(analysis.getModules());

        if (newModules == null)
            return;
        analysis.setModules(newModules);

        // Updating the selected modules
        setSelectedModulesByIndex(selectedIndices);

        updateParameters();
        updateModules();
        updateModuleStates(false);

        menuBar.setUndoRedoStatus(undoRedoStore);

    }

    public static void redo() {
        int[] selectedIndices = getSelectedModuleIndices();

        Modules newModules = undoRedoStore.getNextRedo(analysis.getModules());
        if (newModules == null)
            return;
        analysis.setModules(newModules);

        // Updating the selected modules
        setSelectedModulesByIndex(selectedIndices);

        updateParameters();
        updateModules();
        updateModuleStates(false);

        menuBar.setUndoRedoStatus(undoRedoStore);

    }

    // COMPONENT SIZE GETTERS

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