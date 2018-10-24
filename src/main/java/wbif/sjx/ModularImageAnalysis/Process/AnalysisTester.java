package wbif.sjx.ModularImageAnalysis.Process;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.ModuleCollection;
import wbif.sjx.ModularImageAnalysis.Object.Parameter;
import wbif.sjx.ModularImageAnalysis.Object.ParameterCollection;

import java.util.LinkedHashSet;

public class AnalysisTester {
    public static void testModules(ModuleCollection modules) {
        for (Module module:modules) {
            boolean runnable = testModule(module,modules);

            module.setRunnable(runnable);

        }
    }

    public static boolean testModule(Module module, ModuleCollection modules) {
        boolean runnable = true;

        // Iterating over each parameter, checking if it's currently available
        for (Parameter parameter:module.updateAndGetParameters().values()) {
            // How the parameters are tested will depend on their type.  Choice array and boolean parameters are
            // selected from a pre-determined pool, so should always be present.
            switch (parameter.getType()) {
                case Parameter.INPUT_IMAGE:
                case Parameter.REMOVED_IMAGE:
                    // Input object-type parameters
                    runnable = testInputParameter(parameter,module,modules);
                    break;

                case Parameter.INPUT_OBJECTS:
                case Parameter.PARENT_OBJECTS:
                case Parameter.CHILD_OBJECTS:
                case Parameter.REMOVED_OBJECTS:
                    // Input object-type parameters
                    runnable = testInputParameter(parameter,module,modules);
                    break;

                case Parameter.DOUBLE:
                case Parameter.INTEGER:
                case Parameter.STRING:
                case Parameter.OUTPUT_IMAGE:
                case Parameter.OUTPUT_OBJECTS:
                    // Input-output value-type parameters
                    break;

                case Parameter.FILE_PATH:
                case Parameter.FOLDER_PATH:
                    // File-type parameters
                    break;

                case Parameter.OBJECT_MEASUREMENT:
                    // Object measurement parameter
                    break;

                case Parameter.IMAGE_MEASUREMENT:
                    // Image measurement parameter
                    break;

                case Parameter.METADATA_ITEM:
                    // Metadata parameter
                    break;

            }
        }

        return runnable;

    }

    public static boolean testInputParameter(Parameter parameter, Module module, ModuleCollection modules) {
        // Get available parameters up to this point
        LinkedHashSet<Parameter> availableParameters = null;
        switch (parameter.getType()) {
            case Parameter.INPUT_IMAGE:
            case Parameter.REMOVED_IMAGE:
                availableParameters = modules.getAvailableImages(module);
                break;

            case Parameter.INPUT_OBJECTS:
            case Parameter.PARENT_OBJECTS:
            case Parameter.CHILD_OBJECTS:
            case Parameter.REMOVED_OBJECTS:
                availableParameters = modules.getAvailableObjects(module);
                break;
        }

        if (availableParameters == null) return false;

        // Checking if a parameter with a matching name is in this list
        for (Parameter availableParameter:availableParameters) {
            if (availableParameter.getValue().equals(parameter.getValue())) return true;
        }

        return false;

    }

//    public static boolean testInputValueParameter(Parameter parameter) {
//        switch ()
//    }
}
