package io.github.mianalysis.mia.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.TreeMap;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import com.drew.lang.annotations.Nullable;
import com.formdev.flatlaf.util.SystemInfo;

import ij.Prefs;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.regions.abstrakt.AbstractPanel;
import io.github.mianalysis.mia.gui.regions.editingpanel.EditingPanel;
import io.github.mianalysis.mia.gui.regions.menubar.CustomMenuBar;
import io.github.mianalysis.mia.gui.regions.processingpanel.ProcessingPanel;
import io.github.mianalysis.mia.gui.regions.progressandstatus.StatusTextField;
import io.github.mianalysis.mia.macro.MacroHandler;
import io.github.mianalysis.mia.module.AvailableModules;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.module.core.OutputControl;
import io.github.mianalysis.mia.module.inputoutput.ImageLoader;
import io.github.mianalysis.mia.module.system.GUISeparator;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.object.parameters.FileFolderPathP;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.system.Colours;
import io.github.mianalysis.mia.process.analysishandling.AnalysisRunner;
import io.github.mianalysis.mia.process.analysishandling.AnalysisTester;
import io.github.mianalysis.mia.process.logging.ProgressBar;
import io.github.mianalysis.mia.process.logging.StatusPanelRenderer;
import io.github.mianalysis.mia.process.logging.SwingProgressBar;
import io.github.mianalysis.mia.process.system.FileCrawler;

/**
 * Created by Stephen on 20/05/2017.
 */
public class GUI {
    public static boolean initialised = false;

    private static Modules modules = new Modules();
    private static AnalysisRunner analysisRunner = new AnalysisRunner();
    private static Module[] selectedModules = null;
    private static int lastModuleEval = -1;
    private static int moduleBeingEval = -1;
    private static WorkspaceI testWorkspace = new Workspaces().getNewWorkspace(null, 1);
    private static UndoRedoStore undoRedoStore = new UndoRedoStore();

    private static int minimumFrameHeight = 520;
    private static int frameHeight = 850;
    private static int elementHeight = 26;
    private static int bigButtonSize = 45;
    private static int moduleButtonWidth = 295;
    private static int statusHeight = 16;

    private static boolean showSidebar = Prefs.get("MIA.showSidebar", true);

    private static ComponentFactory componentFactory = new ComponentFactory(elementHeight);
    private static JFrame frame = new JFrame();
    private static Font defaultFont = null;
    private static CustomMenuBar menuBar = new CustomMenuBar();
    private static StatusTextField textField = new StatusTextField();
    private static ProcessingPanel processingPanel = new ProcessingPanel();
    private static EditingPanel editingPanel;
    private static AbstractPanel mainPanel;
    private static Modules availableModules = new Modules();

    public GUI() throws Exception {
        // Only create a GUI if one hasn't already been created
        if (initialised) {
            frame.setVisible(true);
            return;
        }
        initialised = true;

        Splash splash = new Splash();
        splash.setVisible(true);

        ProgressBar.setActive(new SwingProgressBar());

        // Creating main Frame
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        Dimension screenSize = new Dimension(gd.getDisplayMode().getWidth(), gd.getDisplayMode().getHeight());
        
        frameHeight = Math.min(frameHeight, (int) Math.floor(screenSize.height*0.8));
        frameHeight = Math.max(frameHeight, minimumFrameHeight);

        // Detecting modules
        List<String> detectedModules = AvailableModules.getModuleNames(false);
        initialiseAvailableModules(detectedModules);

        processingPanel = new ProcessingPanel();
        editingPanel = new EditingPanel();

        // Adding a new ImageLoader module to the empty analysis
        GUISeparator guiSeparator = new GUISeparator(modules);
        guiSeparator.setNickname("File loading");
        modules.add(guiSeparator);
        modules.add(new ImageLoader<>(modules));

        // Determining which panel should be shown
        if (MIA.isDebug())
            mainPanel = editingPanel;
        else
            mainPanel = processingPanel;

        initialiseStatusTextField();
        frame.setTitle("MIA");
        if (SystemInfo.isMacFullWindowContentSupported) {
            frame.getRootPane().putClientProperty("apple.awt.fullWindowContent", true);
            frame.getRootPane().putClientProperty("apple.awt.transparentTitleBar", true);
            JPanel titleBar = new JPanel();
            titleBar.setBackground(Colours.getBlue(false));
            titleBar.setPreferredSize(new Dimension(100, 50));
            // frame.add(titleBar);
        }
        // UIManager.put("TitlePane.background", Colours.getBlue(false));
        UIManager.put("PopupMenu.borderCornerRadius", 8);
        UIManager.put("Popup.borderCornerRadius", 8);
        UIManager.put("ComboBox.borderCornerRadius", 8);
        UIManager.put("ToolTip.borderCornerRadius", 8);
        UIManager.put("Button.arc", 8);
        UIManager.put("ToolTip.foreground", Color.BLACK);

        frame.getRootPane().putClientProperty("apple.awt.windowTitleVisible", false);
        frame.getRootPane().putClientProperty("apple.awt.fullscreenable", true);
        frame.setJMenuBar(menuBar);
        frame.add(mainPanel);
        frame.setPreferredSize(new Dimension(mainPanel.getPreferredWidth(), mainPanel.getPreferredHeight()));
        frame.setIconImage(new ImageIcon(this.getClass().getResource("/icons/Logo_wide_32.png"), "").getImage());

        mainPanel.updatePanel(false, null);
        menuBar.setUndoRedoStatus(undoRedoStore);
        splash.setVisible(false);

        // Final bits for listeners
        frame.pack();
        frame.validate();
        frame.repaint();
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
            String shortName = detectedModuleName.substring(detectedModuleName.lastIndexOf(".") + 1);

            try {
                // Checking dependencies have been met
                if (!MIA.getDependencies().compatible(shortName, false))
                    continue;

                Class<? extends Module> clazz = (Class<? extends Module>) Class.forName(detectedModuleName);
                if (clazz != InputControl.class && clazz != OutputControl.class) {
                    // Skip any abstract Modules
                    if (Modifier.isAbstract(clazz.getModifiers()))
                        continue;

                    Constructor constructor = clazz.getDeclaredConstructor(Modules.class);
                    Module module = (Module) constructor.newInstance(availableModules);
                    availableModules.add(module);

                }
            } catch (Exception e) {
                MIA.log.writeWarning(
                        "Module \"" + shortName + "\" not loaded.  Incompatible with MIA v" + MIA.getVersion() + ".");
                e.printStackTrace();
            }
        }
    }

    public static Font getDefaultFont() {
        if (defaultFont != null)
            return defaultFont;

        try {
            System.out.println(GUI.class);
            System.out.println(GUI.class.getResourceAsStream("/fonts/Quicksand-Medium.ttf"));
            defaultFont = Font.createFont(Font.TRUETYPE_FONT, GUI.class.getResourceAsStream("/fonts/Quicksand-Medium.ttf"));
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(defaultFont);
            return defaultFont;
        } catch (FontFormatException | IOException e) {
            MIA.log.writeError(e);
            return null;
        }
    }

    public static void refreshLookAndFeel() {
        if (frame != null)
            SwingUtilities.updateComponentTreeUI(frame);

        if (processingPanel != null)
            SwingUtilities.updateComponentTreeUI(processingPanel);

        if (editingPanel != null)
            SwingUtilities.updateComponentTreeUI(editingPanel);

        for (Parameter parameter : getModules().getInputControl().getAllParameters().values())
            SwingUtilities.updateComponentTreeUI(parameter.getControl().getComponent());

        for (Parameter parameter : getModules().getOutputControl().getAllParameters().values())
            SwingUtilities.updateComponentTreeUI(parameter.getControl().getComponent());

        for (Parameter parameter : MIA.getPreferences().getAllParameters().values())
            SwingUtilities.updateComponentTreeUI(parameter.getControl().getComponent());

        for (Module module : getModules())
            for (Parameter parameter : module.getAllParameters().values())
                SwingUtilities.updateComponentTreeUI(parameter.getControl().getComponent());

    }

    public static void updatePanel() {
        if (mainPanel != null) {
            int preferredWidth = mainPanel.getPreferredWidth();
            int preferredHeight = mainPanel.getPreferredHeight();
            frame.setPreferredSize(new Dimension(preferredWidth, preferredHeight));

            int minimumWidth = mainPanel.getMinimumWidth();
            int minimumHeight = mainPanel.getMinimumHeight();
            frame.setMinimumSize(new Dimension(minimumWidth, minimumHeight));
        }

        menuBar.update();

        frame.pack();
        frame.revalidate();
        frame.repaint();

    }

    private static void initialiseStatusTextField() {
        textField.setPreferredSize(new Dimension(Integer.MAX_VALUE, statusHeight));
        textField.setBorder(null);
        textField.setText("MIA (version " + MIA.getVersion() + ")");
        textField.setFont(GUI.getDefaultFont().deriveFont(Font.BOLD, 14f));
        textField.setToolTipText(textField.getText());
        textField.setOpaque(false);

        StatusPanelRenderer statusPanelRenderer = new StatusPanelRenderer(textField);
        MIA.log.addRenderer(statusPanelRenderer);

    }

    public static void updateAvailableModules() {
        mainPanel.updateAvailableModules();
    }

    public static void updateModules(boolean testAnalysis, @Nullable Module startModule) {
        mainPanel.updateModules(testAnalysis, startModule);

    }

    public static void updateModuleStates(boolean testAnalysis, @Nullable Module startModule) {
        if (testAnalysis)
            AnalysisTester.testModules(getModules(), testWorkspace, startModule);

        mainPanel.updateModuleStates();
    }

    public static void updateParameters(boolean testAnalysis, @Nullable Module startModule) {
        mainPanel.updateParameters(testAnalysis, startModule);
    }

    public static ComponentFactory getComponentFactory() {
        return componentFactory;
    }

    public static JFrame getFrame() {
        return frame;
    }

    public static boolean isProcessingGUI() {
        return mainPanel == processingPanel;
    }

    public static void setModules(Modules modules) {
        GUI.modules = modules;
        MacroHandler.setModules(modules);
    }

    public static Modules getModules() {
        return modules;
    }

    public synchronized static void updateProgressBar(int val) {
        mainPanel.setProgress(val);
    }

    public synchronized static void updateProgressBar() {
        Workspaces workspaces = analysisRunner.getWorkspaces();

        updateProgressBar((int) Math.round(workspaces.getOverallProgress() * 100));

    }

    public static void updateTestFile(boolean verbose) {
        // Ensuring the input file specified in the InputControl is active in the test
        // workspace
        InputControl inputControl = modules.getInputControl();
        String inputPath = ((FileFolderPathP) inputControl.getParameter(InputControl.INPUT_PATH)).getPath();

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

        MIA.log.writeStatus("Test file updated");

    }

    public static int getLastModuleEval() {
        return lastModuleEval;
    }

    public static void setLastModuleEval(int lastModuleEval) {
        GUI.lastModuleEval = Math.max(lastModuleEval, -1);
    }

    public static WorkspaceI getTestWorkspace() {
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
            selectedModules = new Module[] { modules.getInputControl() };
            return;
        } else if (selectedModuleIndices.length == 1 && selectedModuleIndices[0] == -2) {
            selectedModules = new Module[] { modules.getOutputControl() };
            return;
        }

        // If the largest index is out of range disable all selections
        if (selectedModuleIndices[selectedModuleIndices.length - 1] >= modules.size()) {
            selectedModules = new Module[0];
            return;
        }

        selectedModules = new Module[selectedModuleIndices.length];
        for (int i = 0; i < selectedModuleIndices.length; i++) {
            if (selectedModuleIndices[i] == -1) {
                selectedModules[i] = modules.getInputControl();
            } else {
                selectedModules[i] = modules.get(selectedModuleIndices[i]);
            }
        }
    }

    public static void setSelectedModules(Module[] selectedModules) {
        GUI.selectedModules = selectedModules;
    }

    public static AnalysisRunner getAnalysisRunner() {
        return analysisRunner;
    }

    public static StatusTextField getTextField() {
        return textField;
    }

    public static void enableEditingMode() {
        editingPanel.setProgress(mainPanel.getProgress());

        frame.remove(mainPanel);
        mainPanel = editingPanel;
        frame.add(mainPanel);

        mainPanel.updatePanel(false, null);
        updatePanel();

    }

    public static void enableProcessingMode() {
        processingPanel.setProgress(mainPanel.getProgress());

        frame.remove(mainPanel);
        mainPanel = processingPanel;
        frame.add(mainPanel);

        mainPanel.updatePanel(false, null);
        updatePanel();

    }

    public static boolean showSidebar() {
        return showSidebar;
    }

    public static void setShowSidebar(boolean showSidebar) {
        GUI.showSidebar = showSidebar;

        menuBar.setShowSidebar(showSidebar);
        mainPanel.setShowSidebar(showSidebar);

        Prefs.set("MIA.showSidebar", showSidebar);
        Prefs.savePreferences();

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
        undoRedoStore.addUndo(modules);
        menuBar.setUndoRedoStatus(undoRedoStore);
    }

    public static void undo() {
        int[] selectedIndices = getSelectedModuleIndices();

        Modules newModules = undoRedoStore.getNextUndo(modules);

        if (newModules == null)
            return;
        GUI.modules = newModules;

        // Updating the selected modules
        setSelectedModulesByIndex(selectedIndices);

        updateModules(true, null);
        updateParameters(false, null);

        menuBar.setUndoRedoStatus(undoRedoStore);

    }

    public static void redo() {
        int[] selectedIndices = getSelectedModuleIndices();

        Modules newModules = undoRedoStore.getNextRedo(modules);
        if (newModules == null)
            return;

        GUI.modules = newModules;

        // Updating the selected modules
        setSelectedModulesByIndex(selectedIndices);

        updateModules(true, null);
        updateParameters(false, null);

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