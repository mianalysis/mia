package wbif.sjx.ModularImageAnalysis.Process;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.io.Serializable;

/**
 * Created by sc13967 on 21/10/2016.
 *
 * Interface Analysis-type class, which will be extended by particular analyses
 *
 */
public abstract class HCAnalysis implements Serializable {
    public HCModuleCollection modules = new HCModuleCollection();
    private boolean shutdown = false;

    // CONSTRUCTOR

    public HCAnalysis() {
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
    public void execute(HCWorkspace workspace) {
        execute(workspace,false);

    }

    /**
     * The method that gets called by the BatchProcessor.  This shouldn't have any user interaction elements
     * @param workspace Workspace containing stores for images and objects
     * @param verbose Switch determining if modules should report progress to System.out
     * @return
     */
    public void execute(HCWorkspace workspace, boolean verbose) {
        if (verbose) System.out.println("Starting analysis");

        // Running through modules
        for (HCModule module:modules) {
            module.execute(workspace,verbose);

            if (shutdown) {
                break;

            }
        }

        // Resetting the shutdown boolean
        shutdown = false;

        if (verbose) System.out.println("Complete");

    }

    public HCModuleCollection getModules() {
        return modules;

    }

    public void shutdown() {
        shutdown = true;

    }
}
