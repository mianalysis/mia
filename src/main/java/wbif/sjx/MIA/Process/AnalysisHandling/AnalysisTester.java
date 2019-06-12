package wbif.sjx.MIA.Process.AnalysisHandling;

import wbif.sjx.MIA.Module.Hidden.InputControl;
import wbif.sjx.MIA.Module.Hidden.OutputControl;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.ModuleCollection;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;

public class AnalysisTester {
    public static int testModules(ModuleCollection modules) {
        int nRunnable = 0;
        for (Module module:modules) {
            boolean runnable = testModule(module,modules);

            module.setRunnable(runnable);

            if (runnable && module.isEnabled()) nRunnable++;

        }

        return nRunnable;

    }

    public static boolean testModule(Module module, ModuleCollection modules) {
        boolean runnable = true;

        if (module == null) return false;

        // Iterating over each parameter, checking if it's currently available
        for (Parameter parameter:module.updateAndGetParameters().values()) {
            runnable = parameter.verify();
            parameter.setValid(runnable);

            if (!runnable) return false;

        }

        return true;

    }
}
