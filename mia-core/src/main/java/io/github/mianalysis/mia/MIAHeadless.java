package io.github.mianalysis.mia;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.LinkedHashMap;

import org.scijava.ItemIO;
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
import io.github.mianalysis.mia.process.analysishandling.AnalysisReader;
import io.github.mianalysis.mia.process.analysishandling.AnalysisRunner;
import io.github.mianalysis.mia.process.logging.HeadlessRenderer;
import io.github.mianalysis.mia.process.logging.ImageJGUIRenderer;
import io.github.mianalysis.mia.process.logging.LogRenderer;
import net.imagej.ImageJService;
import net.imagej.patcher.LegacyInjector;

@Plugin(type = Command.class, menuPath = "Plugins>ModularImageAnalysis (MIA)>MIA (headless)", visible = true, headless = true)
public class MIAHeadless extends MIA implements Command {
    static {
        LegacyInjector.preinit();
    }

    @Parameter
    protected static ImageJService ijService;

    @Parameter(required = false, persist = false, visibility = ItemVisibility.MESSAGE)
    private String workFlowSelectionMessage = "<html><b>Workflow selection</b></html>";

    @Parameter(label = "Workflow file path", type = ItemIO.INPUT, required = true, persist = true)
    public File workflowPath = null;

    @Parameter(required = false, persist = false, visibility = ItemVisibility.MESSAGE)
    private String workFlowConfigMessage = "<html><b>Workflow configuration (optional)</b></html>";

    // The following currently has to be a String as there's seemingly no way to
    // select either a file or folder
    @Parameter(label = "Input path", type = ItemIO.INPUT, required = false, persist = true)
    public String inputPath = null;

    @Parameter(label = "Variables", type = ItemIO.INPUT, required = false, persist = true, visibility = ItemVisibility.NORMAL)
    public String variables = null;

    @Parameter(required = false, persist = false, visibility = ItemVisibility.MESSAGE)
    private String loggingMessage = "<html><b>Logging configuration</b></html>";

    @Parameter(label = "Show debug", type = ItemIO.INPUT, required = false, persist = true)
    public boolean showDebug = false;

    @Parameter(label = "Show memory", type = ItemIO.INPUT, required = false, persist = true)
    public boolean showMemory = false;

    @Parameter(label = "Show message", type = ItemIO.INPUT, required = false, persist = true)
    public boolean showMessage = true;

    @Parameter(label = "Show status", type = ItemIO.INPUT, required = false, persist = true)
    public boolean showStatus = true;

    @Parameter(label = "Show warning", type = ItemIO.INPUT, required = false, persist = true)
    public boolean showWarning = true;

    @Parameter(label = "Verbose messages", type = ItemIO.INPUT, required = false, persist = true)
    public boolean verbose = false;

    @Parameter(required = false, persist = false, visibility = ItemVisibility.MESSAGE)
    private String recorderMessage = "<html><b>Macro recorder configuration</b></html>";

    @Parameter(label = "Display macro command", type = ItemIO.INPUT, required = false, persist = true)
    public boolean displayMacroCommand = false;

    @Override
    public void run() {
        // The macro recorder doesn't appear to pick up the parameters from this plugin,
        // so manually show the command
        if (displayMacroCommand) {
            StringBuilder sb = new StringBuilder();
            sb.append("Macro command: run(\"MIA (headless)\", \"");
            sb.append("workflowpath=[" + workflowPath + "]");
            sb.append(" inputpath=[" + inputPath + "]");
            sb.append(" variables=[" + variables + "]");
            sb.append(" showdebug=" + showDebug);
            sb.append(" showmemory=" + showMemory);
            sb.append(" showmessage=" + showMessage);
            sb.append(" showstatus=" + showStatus);
            sb.append(" showwarning=" + showWarning);
            sb.append(" verbose=" + verbose);
            sb.append(" displaymacrocommand=false"); // Although it was true, we don't want this to keep showing up
            sb.append("\");");

            MIA.log.writeMessage(sb.toString());

        }

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

            Module.setVerbose(verbose);

            Modules modules;
            if (inputPath == null) {
                modules = AnalysisReader.loadModules(workflowPath);
            } else {
                modules = AnalysisReader.loadModules(workflowPath);
                modules.getInputControl().updateParameterValue(InputControl.INPUT_PATH, inputPath);
            }

            // Inserting variables
            if (variables != null && variables.length() != 0)
                applyGlobalVariables(modules, variables);

            // Running analysis
            new AnalysisRunner().run(modules);

            if (GraphicsEnvironment.isHeadless())
                java.lang.System.exit(0);

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
