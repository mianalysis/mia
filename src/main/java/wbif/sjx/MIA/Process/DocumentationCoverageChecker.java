package wbif.sjx.MIA.Process;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterGroup;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Object.Parameters.Text.MessageP;
import wbif.sjx.MIA.Object.References.ImageMeasurementRef;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRef;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;

public class DocumentationCoverageChecker {
    public static void main(String[] args) {
        // Get a list of Modules
        List<String> classNames = ClassHunter.getModules(false);

        int completedModuleDescriptions = 0;
        int completedParameterSets = 0;
        int completedParameters = 0;
        int totalParameters = 0;
        int completedImageRefs = 0;
        int totalImageRefs = 0;
        int completedObjRefs = 0;
        int totalObjRefs = 0;

        DecimalFormat df = new DecimalFormat("0.00");

        // Converting the list of classes to a list of Modules
        for (String className : classNames) {
            try {
                Class<Module> clazz = (Class<Module>) Class.forName(className);

                // Skip any abstract Modules
                if (Modifier.isAbstract(clazz.getModifiers()))
                    continue;

                Constructor constructor = clazz.getDeclaredConstructor(ModuleCollection.class);
                Module module = (Module) constructor.newInstance(new ModuleCollection());

                if (module.getDescription() != null) {
                    if (module.getDescription().length() > 1)
                        completedModuleDescriptions++;
                }

                int[] parameterCounts = getModuleParameterCoverage(module);
                totalParameters = totalParameters + parameterCounts[0];
                completedParameters = completedParameters + parameterCounts[1];

                if (parameterCounts[0] == parameterCounts[1])
                    completedParameterSets++;

                double parameterCoverage;
                if (parameterCounts[0] == 0) {
                    parameterCoverage = 1;
                } else {
                    parameterCoverage = (double) parameterCounts[1] / (double) parameterCounts[0];
                }
                int incompleteParameters = parameterCounts[0] - parameterCounts[1];

                int[] measurementCounts = getModuleMeasurementCoverage(module);
                totalImageRefs = totalImageRefs + measurementCounts[0];
                completedImageRefs = completedImageRefs + measurementCounts[1];
                totalObjRefs = totalObjRefs + measurementCounts[2];
                completedObjRefs = completedObjRefs + measurementCounts[3];

                double imageRefCoverage = measurementCounts[0] == 0 ? 1
                        : (double) measurementCounts[1] / (double) measurementCounts[0];
                double objRefCoverage = measurementCounts[2] == 0 ? 1
                        : (double) measurementCounts[3] / (double) measurementCounts[2];

                System.out.println("Module \"" + module.getName() + "\", parameters = " + df.format(100 * parameterCoverage)
                        + "%, incomplete parameters = "+incompleteParameters+", image refs = " + df.format(100 * imageRefCoverage) + "%, obj refs = " + df.format(100 * objRefCoverage));

            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException
                    | InvocationTargetException e) {
                System.err.println(e.getMessage());
            }
        }

        double fractionModuleDescriptions = (double) completedModuleDescriptions / (double) classNames.size();
        double fractionModuleParameters = (double) completedParameterSets / (double) classNames.size();
        double fractionParameters = (double) completedParameters / (double) totalParameters;
        double fractionImageMeasurements = (double) completedImageRefs / (double) totalImageRefs;
        double fractionObjMeasurements = (double) completedObjRefs / (double) totalObjRefs;

        System.out.println(" ");
        System.out.println("Completed module descriptions = " + completedModuleDescriptions + "/" + classNames.size()
                + " (" + df.format(100 * fractionModuleDescriptions) + "%)");
        System.out.println("Completed parameter set descriptions = " + completedParameterSets + "/" + classNames.size()
                + " (" + df.format(100 * fractionModuleParameters) + "%)");
        System.out.println("Mean parameter coverage = " + completedParameters + "/" + totalParameters + " ("
                + df.format(100 * fractionParameters) + "%)");
        System.out.println("Mean image measurement coverage = " + completedImageRefs + "/" + totalImageRefs + " ("
                + df.format(100 * fractionImageMeasurements) + "%)");
        System.out.println("Mean object measurement coverage = " + completedObjRefs + "/" + totalObjRefs + " ("
                + df.format(100 * fractionObjMeasurements) + "%)");

        System.out.println("_" + classNames.size() + "_" + completedModuleDescriptions + "_" + totalParameters + "_"
                + completedParameters + "_" + completedParameterSets + "_" + totalImageRefs + "_" + completedImageRefs
                + "_" + totalObjRefs + "_" + completedObjRefs);

    }

    public static int[] getModuleMeasurementCoverage(Module module) {
        ImageMeasurementRefCollection imageRefs = module.updateAndGetImageMeasurementRefs();
        int nImageRefs = 0;
        int nCoveredImageRefs = 0;

        if (imageRefs != null) {
            nImageRefs = imageRefs.size();
            for (ImageMeasurementRef ref : module.updateAndGetImageMeasurementRefs().values()) {
                if (ref.getDescription() == null)
                    continue;
                if (ref.getDescription().length() > 1)
                    nCoveredImageRefs++;
            }
        }

        int nObjRefs = 0;
        int nCoveredObjRefs = 0;
        ObjMeasurementRefCollection objRefs = module.updateAndGetObjectMeasurementRefs();
        if (objRefs != null) {
            nObjRefs = objRefs.size();
            for (ObjMeasurementRef ref : module.updateAndGetObjectMeasurementRefs().values()) {
                if (ref.getDescription() == null)
                    continue;
                if (ref.getDescription().length() > 1)
                    nCoveredObjRefs++;
            }
        }

        return new int[] { nImageRefs, nCoveredImageRefs, nObjRefs, nCoveredObjRefs };

    }

    public static int[] getModuleParameterCoverage(Module module) {
        int nParams = module.getAllParameters().size();
        int nCoveredParams = 0;

        for (Parameter parameter : module.getAllParameters().values()) {

            if (parameter.getDescription() == null)
                continue;

            if (parameter instanceof ParamSeparatorP || parameter instanceof MessageP) 
                nParams--;
                
            if (parameter instanceof ParameterGroup) 
                nParams = nParams + ((ParameterGroup) parameter).getTemplateParameters().size();
                
            if (parameter.getDescription().length() > 1) 
                nCoveredParams++;
        }

        return new int[] { nParams, nCoveredParams };

    }
}
