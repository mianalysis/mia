package wbif.sjx.MIA.Process.AnalysisHandling;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.Miscellaneous.WorkflowHandling;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;

public class AnalysisTester {
    public static int testModules(ModuleCollection modules) {
        // Setting all module runnable states to false
        for (Module module : modules)
            module.setRunnable(false);

        // Iterating over all modules, checking if they are runnable
        int nRunnable = 0;
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            boolean runnable = testModule(module, modules);
            module.setRunnable(runnable);

            // Checking for the special case of WorkflowHandling module in
            // "GUI choice" mode (this we can definitively evaluate at this point)
            if (module instanceof WorkflowHandling) {
                String testMode = module.updateAndGetParameters().getValue(WorkflowHandling.TEST_MODE);
                if (!testMode.equals(WorkflowHandling.TestModes.GUI_CHOICE))
                    continue;

                Module redirectModule = module.getRedirectModule();

                // If null, the analysis was terminated
                if (redirectModule == null)
                    i = modules.size();

                // Setting the index of the next module to be evaluated
                i = modules.indexOf(module.getRedirectModule()) - 1;

            }

            if (runnable && module.isEnabled())
                nRunnable++;

        }

        // // Iterating over all modules, checking if they are runnable
        // int nRunnable = 0;
        // for (Module module : modules) {
        // boolean runnable = testModule(module, modules);

        // module.setRunnable(runnable);

        // if (runnable && module.isEnabled())
        // nRunnable++;

        // }

        return nRunnable;

    }

    public static boolean testModule(Module module, ModuleCollection modules) {
        boolean runnable = true;

        if (module == null)
            return false;

        // Iterating over each parameter, checking if it's currently available
        for (Parameter parameter : module.updateAndGetParameters().values()) {
            runnable = parameter.verify();
            parameter.setValid(runnable);

            if (!runnable)
                break;

        }

        // Running module-specific test
        if (runnable)
            runnable = module.verify();

        module.setRunnable(runnable);

        return runnable;

    }
}
