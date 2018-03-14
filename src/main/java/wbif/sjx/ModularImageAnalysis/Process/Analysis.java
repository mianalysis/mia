package wbif.sjx.ModularImageAnalysis.Process;

import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.InputControl;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.OutputControl;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.io.Serializable;

/**
 * Created by sc13967 on 21/10/2016.
 *
 * Interface Analysis-type class, which will be extended by particular analyses
 *
 */
public abstract class Analysis implements Serializable {
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
    public abstract void initialise();

    /**
     * The method that gets called by the BatchProcessor.  This shouldn't have any user interaction elements
     * @param workspace Workspace containing stores for images and objects
     * @return
     */
    public boolean execute(Workspace workspace) throws GenericMIAException {

        return execute(workspace,false);

    }

    /**
     * The method that gets called by the BatchProcessor.  This shouldn't have any user interaction elements
     * @param workspace Workspace containing stores for images and objects
     * @param verbose Switch determining if modules should report progress to System.out
     * @return
     */
    public boolean execute(Workspace workspace, boolean verbose) throws GenericMIAException {
        if (verbose) System.out.println("Starting analysis");

        // Check that all available parameters have been set
        for (Module module:modules) {
            ParameterCollection activeParameters = module.updateAndGetParameters();
            for (Parameter activeParameter:activeParameters.values()) {
                if (activeParameter.getValue() == null) throw new GenericMIAException(
                        "Module \""+module.getTitle()+"\" parameter \""+activeParameter.getName()+"\" not set");
            }
        }

        // Running through modules
        for (Module module:modules) {
            if (Thread.currentThread().isInterrupted()) break;
            if (module.isEnabled()) module.execute(workspace,verbose);

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
}
