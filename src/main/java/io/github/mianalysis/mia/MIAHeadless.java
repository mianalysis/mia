package io.github.mianalysis.mia;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.module.system.GlobalVariables;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.process.analysishandling.Analysis;
import io.github.mianalysis.mia.process.analysishandling.AnalysisReader;
import io.github.mianalysis.mia.process.analysishandling.AnalysisRunner;
import io.github.mianalysis.mia.process.logging.HeadlessRenderer;
import io.github.mianalysis.mia.process.logging.ImageJGUIRenderer;
import io.github.mianalysis.mia.process.logging.LogRenderer;

@Plugin(type = Command.class, menuPath = "Plugins>ModularImageAnalysis (MIA)>MIA (headless)", visible = true)
public class MIAHeadless extends MIA {
    @Parameter (required = false, visibility = ItemVisibility.MESSAGE)
    private String workFlowSelectionMessage = "<html><b>Workflow selection</b></html>";

    @Parameter(label = "Workflow file path", required = true)
    public File workflowPath = null;

    @Parameter (required = false, visibility = ItemVisibility.MESSAGE)
    private String workFlowConfigMessage = "<html><b>Workflow configuration (optional)</b></html>";

    // The following currently has to be a String as there's seemingly no way to select either a file or folder
    @Parameter(label = "Input path", required = false, persist = false)
    public String inputPath = null;

    @Parameter(label = "Variables", required = false, persist = false)
    public String variables = null;

    @Parameter (required = false, visibility = ItemVisibility.MESSAGE)
    private String loggingMessage = "<html><b>Logging configuration</b></html>";

    @Parameter(label = "Show debug", required = false, persist = false)
    public boolean showDebug = false;

    @Parameter(label = "Show memory", required = false, persist = false)
    public boolean showMemory = false;

    @Parameter(label = "Show message", required = false, persist = false)
    public boolean showMessage = true;

    @Parameter(label = "Show status", required = false, persist = false)
    public boolean showStatus = true;

    @Parameter(label = "Show warning", required = false, persist = false)
    public boolean showWarning = true;

    @Parameter(label = "Verbose messages", required = false, persist = false)
    public boolean verbose = false;



    @Override
    public void run() {
        headless = true;

        try {
            // Before removing the old renderer we want to check the new one can be created
            LogRenderer newRenderer;
            if (GraphicsEnvironment.isHeadless()) {
                newRenderer = new HeadlessRenderer();
                LogRenderer.setShowProgress(true);
                LogRenderer.setProgress(0);
            } else {
                UIService uiService = ijService.context().getService(UIService.class);
                newRenderer = new ImageJGUIRenderer(uiService);

            }

            newRenderer.setWriteEnabled(LogRenderer.Level.DEBUG, showDebug);
            newRenderer.setWriteEnabled(LogRenderer.Level.MEMORY, showMemory);
            newRenderer.setWriteEnabled(LogRenderer.Level.MESSAGE, showMessage);
            newRenderer.setWriteEnabled(LogRenderer.Level.STATUS, showStatus);
            newRenderer.setWriteEnabled(LogRenderer.Level.WARNING, showWarning);

            log.removeRenderer(mainRenderer);
            log.addRenderer(newRenderer);
            mainRenderer = newRenderer;

            version = extractVersion();
            Module.setVerbose(verbose);

            Analysis analysis;
            if (inputPath == null) {
                analysis = AnalysisReader.loadAnalysis(workflowPath);
            } else {
                analysis = AnalysisReader.loadAnalysis(workflowPath);
                analysis.getModules().getInputControl().updateParameterValue(InputControl.INPUT_PATH,
                        inputPath);
            }

            // Inserting variables
            if (variables != null)
                applyGlobalVariables(analysis.getModules(), variables);

            // Running analysis
            Thread t = new Thread(() -> {
                try {
                    new AnalysisRunner().run(analysis);

                    if (GraphicsEnvironment.isHeadless())
                        java.lang.System.exit(0);

                } catch (InterruptedException | IOException e) {
                    MIA.log.writeError(e);
                }
            });
            t.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyGlobalVariables(Modules modules, String variables) {
        String[] variablesArray = variables.split(";");
        for (String variable : variablesArray) {
            String[] splitVariables = variable.split(":");
            String newVariableName = splitVariables[0].trim();
            String newVariableValue = splitVariables[1].trim();

            for (Module module : modules.values()) {
                if (module instanceof GlobalVariables && module.isEnabled()) {
                    ParameterGroup group = module.getAllParameters().getParameter(GlobalVariables.ADD_NEW_VARIABLE);
                    if (group == null)
                        continue;

                    LinkedHashMap<Integer, Parameters> collections = group.getCollections(false);
                    for (Parameters collection : collections.values()) {
                        String variableName = collection.getValue(GlobalVariables.VARIABLE_NAME, null);
                        if (!variableName.equals(newVariableName))
                            continue;

                        collection.getParameter(GlobalVariables.VARIABLE_VALUE).setValue(newVariableValue);

                    }
                }
            }
        }
    }
}
