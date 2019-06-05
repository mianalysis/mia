package wbif.sjx.MIA.Process.AnalysisHandling;

import wbif.sjx.MIA.Module.Hidden.InputControl;
import wbif.sjx.MIA.Module.Hidden.OutputControl;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.ModuleCollection;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;

public class AnalysisTester {
    public static int testModules(ModuleCollection modules) {
        int nRunnable = 0;

        // Testing input (this doesn't count towards nRunnable)
        InputControl inputControl = modules.getInputControl();
        boolean runnable = testModule(inputControl,modules);
        if (!runnable) {
            inputControl.setRunnable(false);

            // Exit the method here as no other modules will be able to run
            for (Module module:modules) module.setRunnable(false);
            modules.getOutputControl().setRunnable(false);
            return nRunnable;

        }

        // Testing output (tis doesn't count towards nRunnable)
        OutputControl outputControl = modules.getOutputControl();
        runnable = testModule(outputControl,modules);
        if (!runnable) outputControl.setRunnable(false);

        for (Module module:modules) {
            runnable = testModule(module,modules);

            module.setRunnable(runnable);

            if (runnable && module.isEnabled()) nRunnable++;

        }

        return nRunnable;

    }

    public static boolean testModule(Module module, ModuleCollection modules) {
        boolean runnable = true;

        if (module == null) return false;

        // Iterating over each parameter, checking if it's currently available
        for (Parameter parameter:module.updateAndGetParameters()) {
            runnable = parameter.verify();
            parameter.setValid(runnable);

            if (!runnable) return false;

        }

        return true;

    }
}
