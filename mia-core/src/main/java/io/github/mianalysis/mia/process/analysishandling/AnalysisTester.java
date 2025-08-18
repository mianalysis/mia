package io.github.mianalysis.mia.process.analysishandling;

import java.util.HashMap;

import com.drew.lang.annotations.Nullable;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.system.GlobalVariables;
import io.github.mianalysis.mia.module.workflow.FixedTextCondition;
import io.github.mianalysis.mia.module.workflow.GUICondition;
import io.github.mianalysis.mia.module.workflow.ModuleIsEnabled;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;

public class AnalysisTester {
    public static int testModules(Modules modules, Workspace workspace, @Nullable Module startModule) {
        GlobalVariables.updateVariables(modules);

        // Iterating over all modules, checking if they are runnable
        int startIdx = startModule == null ? 0 : Math.max(0, modules.indexOf(startModule));

        HashMap<Module, Boolean> moduleTested = new HashMap<>();
        for (Module module : modules.values())
            moduleTested.put(module, false);

        int nRunnable = 0;
        for (int i = startIdx; i < modules.size(); i++) {
            Module module = modules.get(i);
            moduleTested.put(module, true);

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

        for (Module module : moduleTested.keySet())
            if (!moduleTested.get(module) && modules.indexOf(module) >= startIdx)
                module.setReachable(false);

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
