package io.github.mianalysis.mia.Process.AnalysisHandling;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.Miscellaneous.GlobalVariables;
import io.github.mianalysis.mia.module.workflowhandling.FixedTextCondition;
import io.github.mianalysis.mia.module.workflowhandling.GUICondition;
import io.github.mianalysis.mia.module.workflowhandling.ModuleIsEnabled;
import io.github.mianalysis.mia.Object.Parameters.Abstract.Parameter;

public class AnalysisTester {
    public static int testModules(Modules modules) {
        GlobalVariables.updateVariables(modules);

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
            if (module instanceof GUICondition || module instanceof FixedTextCondition
                    || module instanceof ModuleIsEnabled) {

                        // For ModuleIsEnabled check if we need to redirect/terminate
                if (module instanceof ModuleIsEnabled)
                    if (!((ModuleIsEnabled) module).testDoRedirect())
                        continue;

                Module redirectModule = module.getRedirectModule();

                // If null, the analysis was terminated
                if (redirectModule == null)
                    break;

                // Setting the index of the next module to be evaluated
                i = modules.indexOf(redirectModule) - 1;

            }

            if (runnable && module.isEnabled())
                nRunnable++;

        }

        return nRunnable;

    }

    public static boolean testModule(Module module, Modules modules) {
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
