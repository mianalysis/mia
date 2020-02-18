package wbif.sjx.MIA.Process;

import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterGroup;
import wbif.sjx.common.MathFunc.CumStat;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

public class DocumentationCoverageChecker {
    public static void main(String[] args) {
        // Get a list of Modules
        List<String> classNames = ClassHunter.getModules(false);

        int completedDescriptions = 0;
        int completedParameterSets = 0;
        int completedParameters = 0;
        int totalParameters = 0;

        // Converting the list of classes to a list of Modules
        for (String className:classNames) {
            try {
                Class<Module> clazz = (Class<Module>) Class.forName(className);

                // Skip any abstract Modules
                if (Modifier.isAbstract(clazz.getModifiers())) continue;

                Constructor constructor = clazz.getDeclaredConstructor(ModuleCollection.class);
                Module module = (Module) constructor.newInstance(new ModuleCollection());

                if (module.getDescription() != null) {
                    if (module.getDescription().length() > 1) completedDescriptions++;
                }

                int[] parameterCounts = getModuleParameterCoverage(module);
                totalParameters = totalParameters + parameterCounts[0];
                completedParameters = completedParameters + parameterCounts[1];

                if (parameterCounts[0] == parameterCounts[1]) completedParameterSets++;

                double parameterCoverage = (double) parameterCounts[1] / (double) parameterCounts[0];
                System.out.println("Module \""+module.getName()+"\", parameters = "+(100*parameterCoverage)+"%");

            } catch (ClassNotFoundException |InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                System.err.println(e.getMessage());
            }
        }

        double fractionModuleDescriptions = (double) completedDescriptions / (double) classNames.size();
        double fractionModuleParameters = (double) completedParameterSets / (double) classNames.size();
        double fractionParameters = (double) completedParameters / (double) totalParameters;

        System.out.println(" ");
        System.out.println("Completed module descriptions = "+completedDescriptions+"/"+classNames.size()+" ("+(100*fractionModuleDescriptions)+"%)");
        System.out.println("Completed parameter descriptions = "+completedParameterSets+"/"+classNames.size()+" ("+(100*fractionModuleParameters)+"%)");
        System.out.println("Mean parameter coverage = "+completedParameters+"/"+totalParameters+" ("+(100*fractionParameters)+"%)");

    }

    public static int[] getModuleParameterCoverage(Module module) {
        int nParams = module.getAllParameters().size();
        int nCoveredParams = 0;

        int[] results = new int[2];

        for (Parameter parameter:module.getAllParameters().values()) {
            if (parameter.getDescription() == null) continue;
            if (parameter instanceof ParameterGroup || parameter instanceof ParamSeparatorP) nParams--;
            if (parameter.getDescription().length() > 1) nCoveredParams++;
        }

        results[0] = nParams;
        results[1] = nCoveredParams;

        return results;

    }
}
