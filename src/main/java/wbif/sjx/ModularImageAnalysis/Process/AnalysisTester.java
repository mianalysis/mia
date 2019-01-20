package wbif.sjx.ModularImageAnalysis.Process;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.util.LinkedHashSet;

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
        for (ParameterOld parameter:module.updateAndGetParameters().values()) {
            // How the parameters are tested will depend on their type.  ChoiceParam array and boolean parameters are
            // selected from a pre-determined pool, so should always be present.
            switch (parameter.getType()) {
                case ParameterOld.INPUT_IMAGE:
                case ParameterOld.REMOVED_IMAGE:
                case ParameterOld.INPUT_OBJECTS:
                case ParameterOld.PARENT_OBJECTS:
                case ParameterOld.CHILD_OBJECTS:
                case ParameterOld.REMOVED_OBJECTS:
                    // Input object-type parameters
                    runnable = testInputImageObjectParameter(parameter,module,modules);
                    break;

                case ParameterOld.DOUBLE:
                case ParameterOld.INTEGER:
                case ParameterOld.STRING:
                case ParameterOld.OUTPUT_IMAGE:
                case ParameterOld.OUTPUT_OBJECTS:
                case ParameterOld.FILE_PATH:
                case ParameterOld.FOLDER_PATH:
                    // Input-output value-type parameters
                    runnable = testInputValueParameter(parameter);
                    break;

                case ParameterOld.OBJECT_MEASUREMENT:
                case ParameterOld.IMAGE_MEASUREMENT:
                    // Image or object measurement parameter
                    runnable = testMeasurementParameter(parameter,module,modules);
                    break;

                case ParameterOld.METADATA_ITEM:
                    // Metadata parameter
                    break;

            }

            parameter.setValid(runnable);

            if (!runnable) return false;

        }

        return true;

    }

    public static boolean testInputImageObjectParameter(ParameterOld parameter, Module module, ModuleCollection modules) {
        String value = parameter.getValue();

        // Get available parameters up to this point
        LinkedHashSet<ParameterOld> availableParameters = null;
        switch (parameter.getType()) {
            case ParameterOld.INPUT_IMAGE:
            case ParameterOld.REMOVED_IMAGE:
                availableParameters = modules.getAvailableImages(module);
                break;

            case ParameterOld.INPUT_OBJECTS:
            case ParameterOld.REMOVED_OBJECTS:
                availableParameters = modules.getAvailableObjects(module);
                break;

            case ParameterOld.PARENT_OBJECTS:
            case ParameterOld.CHILD_OBJECTS:
                availableParameters = modules.getAvailableObjects(module);
                if (value == null) return false;
                int lastIdx = value.lastIndexOf(" // ");
                if (lastIdx != -1) value = value.substring(lastIdx+4,value.length());
                break;
        }

        if (availableParameters == null) return false;

        // Checking if a parameter with a matching name is in this list
        for (ParameterOld availableParameter:availableParameters) {
            if (availableParameter.getValue().equals(value)) return true;
        }

        return false;

    }

    public static boolean testInputValueParameter(ParameterOld parameter) {
        if (parameter.getValue() == null) return false;

        switch (parameter.getType()) {
            case ParameterOld.DOUBLE:
            case ParameterOld.INTEGER:
                return !(parameter.getValue() == null);

            case ParameterOld.STRING:
            case ParameterOld.OUTPUT_IMAGE:
            case ParameterOld.OUTPUT_OBJECTS:
            case ParameterOld.FILE_PATH:
            case ParameterOld.FOLDER_PATH:
                return !((String) parameter.getValue()).equals("");
        }

        return false;

    }

    public static boolean testMeasurementParameter(ParameterOld parameter, Module module, ModuleCollection modules) {
        MeasurementReferenceCollection measurements = null;

        if (parameter.getValue() == null || parameter.getValueSource() == null) return false;

        switch (parameter.getType()) {
            case ParameterOld.IMAGE_MEASUREMENT:
                measurements = modules.getImageMeasurementReferences(parameter.getValueSource(),module);
                break;

            case ParameterOld.OBJECT_MEASUREMENT:
                measurements = modules.getObjectMeasurementReferences(parameter.getValueSource(),module);
                break;
        }

        if (measurements == null) return false;

        // Checking if a parameter with a matching name is in this list
        for (MeasurementReference measurement:measurements.values()) {
            if (measurement.getName().equals(parameter.getValue())) return true;
        }

        return false;

    }

    public static boolean testMetadataParameter(ParameterOld parameter, Module module, ModuleCollection modules) {
        MetadataReferenceCollection metadataReferences = modules.getMetadataReferences(module);

        if (metadataReferences == null) return false;

        // Checking if a parameter with a matching name is in this list
        for (MetadataReference metadataReference:metadataReferences.values()) {
            if (metadataReference.getName().equals(parameter.getValue())) return true;
        }

        return false;

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
