package wbif.sjx.ModularImageAnalysis.Process;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;

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

//        // Iterating over each parameter, checking if it's currently available
//        for (Parameter parameter:module.updateAndGetParameters()) {
//            runnable = parameter.verify();
//            parameter.setValid(runnable);
//
//            if (!runnable) return false;
//
//        }

        return true;

    }

//    public static boolean testMeasurementParameter(ParameterOld parameter, Module module, ModuleCollection modules) {
//        MeasurementRefCollection measurements = null;
//
//        if (parameter.getValue() == null || parameter.getValueSource() == null) return false;
//
//        switch (parameter.getType()) {
//            case ParameterOld.IMAGE_MEASUREMENT:
//                measurements = modules.getImageMeasurementRefs(parameter.getValueSource(),module);
//                break;
//
//            case ParameterOld.OBJECT_MEASUREMENT:
//                measurements = modules.getObjectMeasurementReferences(parameter.getValueSource(),module);
//                break;
//        }
//
//        if (measurements == null) return false;
//
//        // Checking if a parameter with a matching name is in this list
//        for (MeasurementReference measurement:measurements.values()) {
//            if (measurement.getName().equals(parameter.getValue())) return true;
//        }
//
//        return false;
//
//    }
//
//    public static boolean testMetadataParameter(ParameterOld parameter, Module module, ModuleCollection modules) {
//        MetadataRefCollection metadataReferences = modules.getMetadataReferences(module);
//
//        if (metadataReferences == null) return false;
//
//        // Checking if a parameter with a matching name is in this list
//        for (MetadataReference metadataReference:metadataReferences.values()) {
//            if (metadataReference.getName().equals(parameter.getValue())) return true;
//        }
//
//        return false;
//
//    }

    public static void reportStatus(ModuleCollection modules) {
        for (Module module:modules) {
            if (module.isEnabled() & !module.isRunnable()) {
                System.err.println("Module \"" + module.getTitle() +
                        "\" not runnable (likely a missing input).  This module has been skipped.");
            }
        }
    }
}
