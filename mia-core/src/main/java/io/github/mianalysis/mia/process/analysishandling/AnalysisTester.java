package io.github.mianalysis.mia.process.analysishandling;

import com.drew.lang.annotations.Nullable;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModulesI;
import io.github.mianalysis.mia.module.system.GlobalVariables;
import io.github.mianalysis.mia.module.workflow.FixedTextCondition;
import io.github.mianalysis.mia.module.workflow.GUICondition;
import io.github.mianalysis.mia.module.workflow.ModuleIsEnabled;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;

public class AnalysisTester {
    public static int testModules(ModulesI modules, WorkspaceI workspace, @Nullable Module startModule) {
        GlobalVariables.updateVariables(modules);
        
        // Iterating over all modules, checking if they are runnable
        int startIdx = startModule == null ? 0 : Math.max(0,modules.indexOf(startModule));
        
        // Setting all module runnable states to false
        for (int i = startIdx; i < modules.size(); i++) {
            Module module = modules.getAtIndex(i);
            module.setRunnable(false);
            module.setReachable(false);
        }

        int nRunnable = 0;
        for (int i = startIdx; i < modules.size(); i++) {
            Module module = modules.getAtIndex(i);
            module.setReachable(true);
            module.setRunnable(testModule(module, modules));

            // Checking for the special case of WorkflowHandling module in
            // "GUI choice" mode (this we can definitively evaluate at this point)
            if (module instanceof GUICondition || module instanceof FixedTextCondition
                    || module instanceof ModuleIsEnabled) {

                // For ModuleIsEnabled check if we need to redirect/terminate
                if (module instanceof ModuleIsEnabled)
                    if (!((ModuleIsEnabled) module).testDoRedirect(workspace))
                        continue;

                Module redirectModule = modules.getModuleByID(module.getRedirectModuleID(workspace));

                // If null, the analysis was terminated
                if (redirectModule == null)
                    break;

                // Setting the index of the next module to be evaluated
                for (Module testModule : modules)
                    if (testModule.getModuleID().equals(redirectModule.getModuleID()))
                        i = modules.indexOf(testModule) - 1;

            }

            if (module.isRunnable() && module.isEnabled())
                nRunnable++;

        }

        return nRunnable;

    }

    public static boolean testModule(Module module, ModulesI modules) {
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
