package wbif.sjx.ModularImageAnalysis.Process;

import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.InputControl;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.OutputControl;
import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.io.Serializable;

/**
 * Created by sc13967 on 21/10/2016.
 *
 * Abstract Analysis-type class, which will be extended by particular analyses
 *
 */
public class Analysis implements Serializable {
    public InputControl inputControl = new InputControl();
    public OutputControl outputControl = new OutputControl();
    public ModuleCollection modules = new ModuleCollection();
    private boolean shutdown = false;

    // CONSTRUCTOR

    public Analysis() {
        initialise();

    }


    // PUBLIC METHODS

    /**
     * Initialisation method is where workspace is populated with modules and module-specific parameters.
     */
    public void initialise() {}

    /**
     * The method that gets called by the BatchProcessor.  This shouldn't have any user interaction elements
     * @param workspace Workspace containing stores for images and objects
     * @return
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

                    // End the analysis run
                    break;

                }
            }

            // Updating progress bar
            double percentageComplete = ((double) (count++))/((double) total)*100;
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

//    public boolean isUpdateProgressBar() {
//        return updateProgressBar;
//    }
//
//    public void setUpdateProgressBar(boolean updateProgressBar) {
//        this.updateProgressBar = updateProgressBar;
//    }
}
