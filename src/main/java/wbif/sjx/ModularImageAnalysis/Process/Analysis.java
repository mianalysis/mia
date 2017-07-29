package wbif.sjx.ModularImageAnalysis.Process;

import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.io.Serializable;

/**
 * Created by sc13967 on 21/10/2016.
 *
 * Interface Analysis-type class, which will be extended by particular analyses
 *
 */
public abstract class Analysis implements Serializable {
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
        System.out.println("Processing file: " + workspace.getMetadata().getFile().getName());

        // Check that all available parameters have been set
        for (HCModule module:modules) {
            ParameterCollection activeParameters = module.getActiveParameters();

            for (Parameter activeParameter:activeParameters.values()) {
                if (activeParameter.getValue() == null) throw new GenericMIAException(
                        "Module \""+module.getTitle()+"\" parameter \""+activeParameter.getName()+"\" not set");
            }
        }

        // Running through modules
        for (HCModule module:modules) {
            if (module.isEnabled()) module.execute(workspace,verbose);

            if (shutdown) {
                shutdown = false;
                System.out.println("Shutdown successful");
                return false;

            }

            // Running garbage collector
            Runtime.getRuntime().gc();

        }

        // Clearing images from the workspace to prevent memory leak
        workspace.clearAllImages(true);

        if (verbose) System.out.println("Complete");

        return true;

    }

    public ModuleCollection getModules() {
        return modules;

    }

    public void shutdown() {
        shutdown = true;

    }

}
