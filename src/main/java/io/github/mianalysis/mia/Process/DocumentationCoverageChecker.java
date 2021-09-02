package io.github.mianalysis.mia.Process;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.util.List;
import java.util.StringTokenizer;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.Object.Parameters.Parameters;
import io.github.mianalysis.mia.Object.Parameters.ParameterGroup;
import io.github.mianalysis.mia.Object.Parameters.SeparatorP;
import io.github.mianalysis.mia.Object.Parameters.Abstract.Parameter;
import io.github.mianalysis.mia.Object.Parameters.Text.MessageP;
import io.github.mianalysis.mia.Object.Refs.ImageMeasurementRef;
import io.github.mianalysis.mia.Object.Refs.ObjMeasurementRef;
import io.github.mianalysis.mia.Object.Refs.Collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.Object.Refs.Collections.ObjMeasurementRefs;

public class DocumentationCoverageChecker {
    public static void main(String[] args) {
        // Get a list of Modules
        List<String> classNames = ClassHunter.getModules(false);

        int nModules = 0;
        int completedModuleDescriptions = 0;
        int completedParameterSets = 0;
        int completedParameters = 0;
        int totalParameters = 0;
        int completedImageRefs = 0;
        int totalImageRefs = 0;
        int completedObjRefs = 0;
        int totalObjRefs = 0;

        StringBuilder sb = new StringBuilder();

        DecimalFormat df = new DecimalFormat("0.00");

        // classNames = new ArrayList<>();
        // classNames.add("io.github.mianalysis.MIA.Module.Miscellaneous.Macros.RunMacroOnImage");

        // Converting the list of classes to a list of Modules
        for (String className : classNames) {
            try {
                Class<Module> clazz = (Class<Module>) Class.forName(className);

                // Skip any abstract Modules
                if (Modifier.isAbstract(clazz.getModifiers()))
                    continue;
                
                nModules++;

                Constructor<Module> constructor = clazz.getDeclaredConstructor(Modules.class);
                Module module = (Module) constructor.newInstance(new Modules());

                boolean hasDescription = module.getDescription() != null && module.getDescription().length() > 1;
                if (hasDescription)
                    completedModuleDescriptions++;

                int[] parameterCounts = getModuleParameterCoverage(module,sb);
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

                boolean hasSeparators = moduleHasSeparators(module);

                System.out.println("Module \"" + module.getName() + "\", description = " + hasDescription
                        + ", parameters = " + df.format(100 * parameterCoverage) + "%, incomplete parameters = "
                        + incompleteParameters + ", image refs = " + df.format(100 * imageRefCoverage)
                        + "%, obj refs = " + df.format(100 * objRefCoverage) + ", separators = " + hasSeparators);

            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException
                    | InvocationTargetException e) {
                System.err.println(e.getMessage());
            }
        }

        double fractionModuleDescriptions = (double) completedModuleDescriptions / (double) nModules;
        double fractionModuleParameters = (double) completedParameterSets / (double) nModules;
        double fractionParameters = (double) completedParameters / (double) totalParameters;
        double fractionImageMeasurements = (double) completedImageRefs / (double) totalImageRefs;
        double fractionObjMeasurements = (double) completedObjRefs / (double) totalObjRefs;

        System.out.println(" ");
        System.out.println("Completed module descriptions = " + completedModuleDescriptions + "/" + nModules
                + " (" + df.format(100 * fractionModuleDescriptions) + "%)");
        System.out.println("Completed parameter set descriptions = " + completedParameterSets + "/" + nModules
                + " (" + df.format(100 * fractionModuleParameters) + "%)");
        System.out.println("Mean parameter coverage = " + completedParameters + "/" + totalParameters + " ("
                + df.format(100 * fractionParameters) + "%)");
        System.out.println("Mean image measurement coverage = " + completedImageRefs + "/" + totalImageRefs + " ("
                + df.format(100 * fractionImageMeasurements) + "%)");
        System.out.println("Mean object measurement coverage = " + completedObjRefs + "/" + totalObjRefs + " ("
                + df.format(100 * fractionObjMeasurements) + "%)");

        System.out.println("_" + nModules + "_" + completedModuleDescriptions + "_" + totalParameters + "_"
                + completedParameters + "_" + completedParameterSets + "_" + totalImageRefs + "_" + completedImageRefs
                + "_" + totalObjRefs + "_" + completedObjRefs);

        System.out.println(new StringTokenizer(sb.toString()).countTokens()+" words");

    }

    public static int[] getModuleMeasurementCoverage(Module module) {
        ImageMeasurementRefs imageRefs = module.updateAndGetImageMeasurementRefs();
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
        ObjMeasurementRefs objRefs = module.updateAndGetObjectMeasurementRefs();
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

    public static int[] getModuleParameterCoverage(Module module, StringBuilder sb) {
        int nParams = module.getAllParameters().size();
        int nCoveredParams = 0;

        for (Parameter parameter : module.getAllParameters().values()) {
            if (parameter instanceof SeparatorP || parameter instanceof MessageP) {
                nParams--;
                continue;
            }

            if (parameter.getDescription() == null)
                continue;

            if (parameter.getDescription().length() > 1) {
                sb.append(" ");
                sb.append(parameter.getDescription());
                nCoveredParams++;
            }

            if (parameter instanceof ParameterGroup) {
                Parameters collection = ((ParameterGroup) parameter).getTemplateParameters();
                nParams = nParams + collection.size();
                for (Parameter collectionParam : collection.values()) {
                    if (collectionParam instanceof SeparatorP || collectionParam instanceof MessageP) {
                        nParams--;
                        continue;
                    }
                    if (collectionParam.getDescription().length() > 1) {
                        sb.append(" ");
                        sb.append(parameter.getDescription());
                        nCoveredParams++;
                    }
                }
            }
        }

        return new int[] { nParams, nCoveredParams };

    }

    static boolean moduleHasSeparators(Module module) {
        Parameters parameters = module.getAllParameters();

        if (parameters == null)
            return true;

        for (Parameter parameter : parameters.values()) {
            if (parameter instanceof SeparatorP)
                return true;
        }

        return false;

    }
}
