package wbif.sjx.MIA.Process.AnalysisHandling;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.ModuleCollection;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;

public class AnalysisTester {
    public static int testModules(ModuleCollection modules) {
        int nRunnable = 0;
        for (Module module:modules) {
//            if (!module.isSelected()) continue;
            boolean runnable = testModule(module,modules);

            module.setRunnable(runnable);

            if (runnable) nRunnable++;

        }

        return nRunnable;

    }

    public static boolean testModule(Module module, ModuleCollection modules) {
        boolean runnable = true;

        // Iterating over each parameter, checking if it's currently available
        for (Parameter parameter:module.updateAndGetParameters()) {
            runnable = parameter.verify();
            parameter.setValid(runnable);

            if (!runnable) return false;

        }

        return true;

    }

    public static void reportStatus(ModuleCollection modules) {
        for (Module module:modules) {
            if (module.isEnabled() & !module.isRunnable()) {
                System.err.println("Module \"" + module.getTitle() +
                        "\" not runnable (likely a missing input).  This module has been skipped.");
            }
        }
    }
}
