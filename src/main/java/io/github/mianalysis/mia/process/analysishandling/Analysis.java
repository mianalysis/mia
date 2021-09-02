package io.github.mianalysis.mia.process.analysishandling;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.macro.MacroHandler;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.miscellaneous.macros.AbstractMacroRunner;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.process.logging.LogRenderer;

/**
 * Created by sc13967 on 21/10/2016.
 */
public class Analysis {
    private Modules modules = new Modules();
    private String analysisFilename = "";

    // CONSTRUCTOR

    public Analysis() {
        initialise();

    }

    // PUBLIC METHODS

    /*
     * Initialisation method is where workspace is populated with modules and
     * module-specific parameters.
     */
    public void initialise() {
    }

    /*
     * The method that gets called by the AnalysisRunner. This shouldn't have any
     * user interaction elements
     * 
     * @param workspace Workspace containing stores for images and objects
     */
    public boolean execute(Workspace workspace) {
        MIA.log.writeDebug("Processing file \"" + workspace.getMetadata().getFile().getAbsolutePath() + "\"");

        // Setting the MacroHandler to the current workspace (only if macro modules are
        // present)
        if (modules.hasModuleMatchingType(AbstractMacroRunner.class)) {
            MacroHandler.setWorkspace(workspace);
            MacroHandler.setModules(modules);
        }

        // Running through modules
        Status status = Status.PASS;

        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);

            if (Thread.currentThread().isInterrupted())
                break;

            if (status == Status.PASS && module.isEnabled() && module.isRunnable()) {
                status = module.execute(workspace);
                workspace.setStatus(status);

                switch (status) {
                    case PASS:
                        break;
                    case FAIL:
                        MIA.log.writeWarning("Analysis failed for file \"" + workspace.getMetadata().getFile()
                                + "\" (series " + workspace.getMetadata().getSeriesNumber() + ") by module \""
                                + module.getName() + "\" (\"" + module.getNickname() + "\").");
                        break;
                    case REDIRECT:
                        // Getting index of module before one to move to
                        Module redirectModule = module.getRedirectModule();
                        if (redirectModule == null)
                            break;
                        i = modules.indexOf(redirectModule) - 1;
                        status = Status.PASS;
                        break;
                    case TERMINATE:
                        MIA.log.writeWarning("Analysis terminated early for file \"" + workspace.getMetadata().getFile()
                                + "\" (series " + workspace.getMetadata().getSeriesNumber() + ") by module \""
                                + module.getName() + "\" (\"" + module.getNickname() + "\").");
                        break;
                }
            }

            // Updating progress bar
            double fractionComplete = ((double) (i + 1)) / ((double) modules.size());
            workspace.setProgress(fractionComplete);
            if (!MIA.isHeadless())
                GUI.updateProgressBar();

        }

        // We're only interested in the measurements now, so clearing images and object
        // coordinates
        workspace.clearAllImages(true);
        workspace.clearAllObjects(true);

        // If enabled, write the current memory usage to the console
        if (MIA.getMainRenderer().isWriteEnabled(LogRenderer.Level.MEMORY)) {
            double totalMemory = Runtime.getRuntime().totalMemory();
            double usedMemory = totalMemory - Runtime.getRuntime().freeMemory();
            String dateTime = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());

            DecimalFormat df = new DecimalFormat("#.0");

            String memoryMessage = df.format(usedMemory * 1E-6) + " MB of " + df.format(totalMemory * 1E-6) + " MB"
                    + ", ANALYSIS COMPLETE, DATE/TIME = " + dateTime + ", FILE = \"" + workspace.getMetadata().getFile()
                    + "\"";

            MIA.log.write(memoryMessage, LogRenderer.Level.MEMORY);

        }

        // Transferring MacroHandler back to test workspace
        if (modules.hasModuleMatchingType(AbstractMacroRunner.class)) {
            MacroHandler.setWorkspace(GUI.getTestWorkspace());
            MacroHandler.setModules(GUI.getModules());
        }

        return true;

    }

    public Modules getModules() {
        return modules;

    }

    public void setModules(Modules modules) {
        this.modules = modules;
    }

    public void removeAllModules() {
        modules.clear();

    }

    public String getAnalysisFilename() {
        return analysisFilename;
    }

    public void setAnalysisFilename(String analysisFilename) {
        this.analysisFilename = analysisFilename;
    }

    public boolean hasVisibleParameters() {
        return (modules.hasVisibleParameters());
    }
}
