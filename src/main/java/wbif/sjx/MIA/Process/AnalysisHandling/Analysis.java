package wbif.sjx.MIA.Process.AnalysisHandling;

import wbif.sjx.MIA.GUI.InputOutput.InputControl;
import wbif.sjx.MIA.GUI.InputOutput.OutputControl;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.ModuleCollection;
import wbif.sjx.MIA.Object.ProgressMonitor;
import wbif.sjx.MIA.Object.Workspace;

import java.io.Serializable;

/**
 * Created by sc13967 on 21/10/2016.
 *
 * Abstract Analysis-type class, which will be extended by particular analyses
 *
 */
public class Analysis {
    public ModuleCollection modules = new ModuleCollection();
    public InputControl inputControl = new InputControl();
    public OutputControl outputControl = new OutputControl();
    private boolean shutdown = false;
    private String analysisFilename = "";

    // CONSTRUCTOR

    public Analysis() {
        initialise();

    }


    // PUBLIC METHODS

    /*
     * Initialisation method is where workspace is populated with modules and module-specific parameters.
     */
    public void initialise() {}

    /*
     * The method that gets called by the BatchProcessor.  This shouldn't have any user interaction elements
     * @param workspace Workspace containing stores for images and objects
     */
    public boolean execute(Workspace workspace) {
        // Running through modules
        int total = modules.size();
        int count = 0;
        for (Module module:modules) {
            if (Thread.currentThread().isInterrupted()) break;
            if (module.isEnabled() && module.isRunnable()) {
                boolean status = module.execute(workspace);
                if (!status) {
                    // The module failed or requested analysis termination.  Add this message to the log
                    System.err.println("Analysis terminated early for file \""+workspace.getMetadata().getFile()+
                            "\" by module \""+module.getTitle()+"\" (\""+module.getNickname()+"\").");

                    // End the analysis generateModuleList
                    break;

                }
            }

            // Updating progress bar
            double percentageComplete = ((double) (++count))/((double) total)*100;
            ProgressMonitor.setWorkspaceProgress(workspace,percentageComplete);
            double overallPercentageComplete = ProgressMonitor.getOverallProgress();
            GUI.setProgress((int) Math.round(overallPercentageComplete));
        }

        // We're only interested in the measurements now, so clearing images and object coordinates
        workspace.clearAllImages(true);
        workspace.clearAllObjects(true);

        return true;

    }

    public InputControl getInputControl() {
        return inputControl;
    }

    public void setInputControl(InputControl inputControl) {
        this.inputControl = inputControl;
    }

    public OutputControl getOutputControl() {
        return outputControl;
    }

    public void setOutputControl(OutputControl outputControl) {
        this.outputControl = outputControl;
    }

    public ModuleCollection getModules() {
        return modules;

    }

    public void removeAllModules() {
        modules.clear();

    }

    public void shutdown() {
        shutdown = true;

    }

    public String getAnalysisFilename() {
        return analysisFilename;
    }

    public void setAnalysisFilename(String analysisFilename) {
        this.analysisFilename = analysisFilename;
    }

    public boolean hasVisibleParameters() {
        return (inputControl.hasVisibleParameters()
                | outputControl.hasVisibleParameters()
                | modules.hasVisibleParameters());

    }
}
