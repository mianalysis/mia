package io.github.mianalysis.mia.gui.regions.menubar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.JMenuItem;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.gui.GUIAnalysisHandler;
import io.github.mianalysis.mia.gui.regions.documentation.DocumentationPanel;
import io.github.mianalysis.mia.module.AvailableModules;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.core.OutputControl;
import io.github.mianalysis.mia.moduledependencies.Dependency;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.process.analysishandling.AnalysisRunner;
import io.github.mianalysis.mia.process.exporting.Exporter;

/**
 * Created by stephen on 28/07/2017.
 */
public class MenuItem extends JMenuItem implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = -1055282940194950277L;
    public static final String NEW_WORKFLOW = "New workflow";
    public static final String LOAD_WORKFLOW = "Load workflow";
    public static final String SAVE_WORKFLOW = "Save workflow";
    public static final String SAVE_WORKFLOW_AS = "Save workflow as...";
    public static final String EXPORT_TEST_WORKSPACE = "Export test workspace";
    public static final String UNDO = "Undo";
    public static final String REDO = "Redo";
    public static final String COPY = "Copy";
    public static final String PASTE = "Paste";
    public static final String PREFERENCES = "Preferences...";
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
    public static final String SHOW_UNAVAILABLE_MODULES = "Unavailable modules";
    public static final String SHOW_PONY = "Pony?";

    public MenuItem(String command) {
        setFont(GUI.getDefaultFont().deriveFont(14f));
        setText(command);
        addActionListener(this);
        setContentAreaFilled(false);
        setOpaque(true);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (getText()) {
            case NEW_WORKFLOW:
                GUIAnalysisHandler.newAnalysis();
                GUI.setSelectedModules(null);
                GUI.updateModules(false, null);
                GUI.updateParameters(false, null);
                break;

            case LOAD_WORKFLOW:
                GUIAnalysisHandler.loadModules();
                break;

            case SAVE_WORKFLOW:
                GUIAnalysisHandler.saveModules();
                break;

            case SAVE_WORKFLOW_AS:
                GUIAnalysisHandler.saveModulesAs();
                break;

            case EXPORT_TEST_WORKSPACE:
                WorkspaceI workspace = GUI.getTestWorkspace();
                Modules modules = GUI.getModules();
                OutputControl outputControl = modules.getOutputControl();
                Exporter exporter = AnalysisRunner.initialiseExporter(outputControl);
                String name = outputControl.getIndividualOutputPath(workspace.getMetadata());
                try {
                    exporter.exportResults(workspace, modules, name);
                } catch (IOException e1) {
                    MIA.log.writeError(e1);
                }
                break;

            case UNDO:
                GUI.undo();
                GUI.updateModules(false, null);
                GUI.updateParameters(false, null);
                break;

            case REDO:
                GUI.redo();
                GUI.updateModules(false, null);
                GUI.updateParameters(false, null);
                break;

            case COPY:
                GUIAnalysisHandler.copyModules();
                GUI.updateModules(false, null);
                GUI.updateParameters(false, null);
                break;

            case PASTE:
                GUIAnalysisHandler.pasteModules();
                GUI.updateModules(true, null);
                GUI.updateParameters(false, null);
                break;

            case PREFERENCES:
                GUI.setSelectedModules(new Module[] { MIA.getPreferences() });
                GUI.updateModules(false, null);
                GUI.updateParameters(false, null);
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

            case SHOW_UNAVAILABLE_MODULES:
                int count = 0;
                List<String> detectedModuleNames = AvailableModules.getModuleNames(false);
                for (String detectedModuleName : detectedModuleNames) {
                    String shortName = detectedModuleName.substring(detectedModuleName.lastIndexOf(".") + 1);
                    // Checking dependencies have been met
                    if (!MIA.getDependencies().compatible(shortName, false))
                        count++;
                }

                if (count == 0) {
                    MIA.log.writeMessage("All modules meet dependency requirements!");
                    return;
                }

                MIA.log.writeMessage(
                        "The following modules could not be loaded due to missing/incompatible dependencies:");
                for (String detectedModuleName : detectedModuleNames) {
                    String shortName = detectedModuleName.substring(detectedModuleName.lastIndexOf(".") + 1);
                    // Checking dependencies have been met
                    if (!MIA.getDependencies().compatible(shortName, false)) {
                        MIA.log.writeMessage("Module \"" + shortName + "\":");
                        for (Dependency dependency : MIA.getDependencies().getDependencies(shortName, false))
                            if (!dependency.test()) {
                                MIA.log.writeMessage("    Requirement: " + dependency.toString());
                                MIA.log.writeMessage("    Message: " + dependency.getMessage());
                            }
                    }
                }

                break;

            case SHOW_PONY:
                DocumentationPanel.showPony();
                break;

        }
    }
}
