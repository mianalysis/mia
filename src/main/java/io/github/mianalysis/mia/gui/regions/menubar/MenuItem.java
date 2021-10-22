package io.github.mianalysis.mia.gui.regions.menubar;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.gui.GUIAnalysisHandler;
import io.github.mianalysis.mia.gui.regions.documentation.DocumentationPanel;
import io.github.mianalysis.mia.module.Module;

/**
 * Created by stephen on 28/07/2017.
 */
public class MenuItem extends JMenuItem implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = -1055282940194950277L;
    public static final String NEW_PIPELINE = "New pipeline";
    public static final String LOAD_PIPELINE = "Load pipeline";
    public static final String SAVE_PIPELINE = "Save pipeline";
    public static final String SAVE_PIPELINE_AS = "Save pipeline as";
    public static final String UNDO = "Undo";
    public static final String REDO = "Redo";
    public static final String COPY = "Copy";
    public static final String PASTE = "Paste";
    public static final String PREFERENCES = "Preferences";
    public static final String RUN_ANALYSIS = "Run analysis";
    public static final String STOP_ANALYSIS = "Stop analysis";
    public static final String RESET_ANALYSIS = "Reset analysis";
    public static final String ENABLE_ALL = "Enable all modules";
    public static final String DISABLE_ALL = "Disable all modules";
    public static final String OUTPUT_ALL = "Show output for all modules";
    public static final String SILENCE_ALL = "Hide output for all modules";
    public static final String PROCESSING_VIEW = "Switch to processing view";
    public static final String EDITING_VIEW = "Switch to editing view";
    public static final String SHOW_ABOUT = "About";
    public static final String SHOW_GETTING_STARTED = "Getting started";
    public static final String SHOW_PONY = "Pony?";

    public MenuItem(String command) {
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        setText(command);
        addActionListener(this);
        setContentAreaFilled(false);
        setOpaque(true);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (getText()) {
            case NEW_PIPELINE:
                GUIAnalysisHandler.newAnalysis();
                GUI.setSelectedModules(null);
                GUI.updateModules();
                GUI.updateParameters();
                break;

            case LOAD_PIPELINE:
                GUIAnalysisHandler.loadAnalysis();
                break;

            case SAVE_PIPELINE:
                GUIAnalysisHandler.saveAnalysis();
                break;

            case SAVE_PIPELINE_AS:
                GUIAnalysisHandler.saveAnalysisAs();
                break;

            case UNDO:
                GUI.undo();
                GUI.updateModules();
                GUI.updateParameters();
                break;

            case REDO:
                GUI.redo();
                GUI.updateModules();
                GUI.updateParameters();
                break;

            case COPY:
                GUIAnalysisHandler.copyModules();
                GUI.updateModules();
                GUI.updateParameters();
                break;

            case PASTE:
                GUIAnalysisHandler.pasteModules();
                GUI.updateModules();
                GUI.updateParameters();
                break;

            case PREFERENCES:
                GUI.setSelectedModules(new Module[] { MIA.preferences });
                GUI.updateModules();
                GUI.updateParameters();
                break;

            case RUN_ANALYSIS:
                GUIAnalysisHandler.runAnalysis();
                break;

            case STOP_ANALYSIS:
                GUIAnalysisHandler.stopAnalysis();
                break;

            case ENABLE_ALL:
                GUIAnalysisHandler.enableAllModules();
                break;

            case DISABLE_ALL:
                GUIAnalysisHandler.disableAllModules();
                break;

            case OUTPUT_ALL:
                GUIAnalysisHandler.enableAllModulesOutput();
                break;

            case SILENCE_ALL:
                GUIAnalysisHandler.disableAllModulesOutput();
                break;

            case PROCESSING_VIEW:
                GUI.enableProcessingMode();
                GUI.setSelectedModules(null);
                setText(MenuItem.EDITING_VIEW);
                break;

            case EDITING_VIEW:
                GUI.enableEditingMode();
                GUI.setSelectedModules(null);
                setText(MenuItem.PROCESSING_VIEW);
                break;

            case SHOW_ABOUT:
                DocumentationPanel.showAbout();
                break;

            case SHOW_GETTING_STARTED:
                DocumentationPanel.showGettingStarted();
                break;

            case SHOW_PONY:
                DocumentationPanel.showPony();
                break;

        }
    }
}
