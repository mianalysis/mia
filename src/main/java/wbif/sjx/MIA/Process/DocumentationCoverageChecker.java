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

        CumStat parameterCoverageCS = new CumStat();
        int completedDescriptions = 0;
        int completedParameterSets = 0;

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
                
                double parameterCoverage = getModuleParameterCoverage(module);
                parameterCoverageCS.addMeasure(parameterCoverage);

                if (parameterCoverage > 0.999) completedParameterSets++;

                System.out.println("Module \""+module.getName()+"\", parameters = "+(100*parameterCoverage)+"%");

            } catch (ClassNotFoundException |InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                System.err.println(e.getMessage());
            }
        }

        double fractionModuleDescriptions = (double) completedDescriptions / (double) classNames.size();
        double fractionParameterDescriptions = (double) completedParameterSets/ (double) classNames.size();

        System.out.println(" ");
        System.out.println("Completed module descriptions = "+completedDescriptions+"/"+classNames.size()+" ("+(100*fractionModuleDescriptions)+"%)");
        System.out.println("Completed parameter descriptions = "+completedParameterSets+"/"+classNames.size()+" ("+(100*fractionParameterDescriptions)+"%)");
        System.out.println("Mean parameter coverage = "+(100*parameterCoverageCS.getMean())+"%");

    }

    public static double getModuleParameterCoverage(Module module) {
        int nParams = module.getAllParameters().size();
        int nCoveredParams = 0;

        for (Parameter parameter:module.getAllParameters().values()) {
            if (parameter.getDescription() == null) continue;
            if (parameter instanceof ParameterGroup || parameter instanceof ParamSeparatorP) nParams--;
            if (parameter.getDescription().length() > 1) nCoveredParams++;
        }

        if (nParams == 0) return 1;

        return (double) nCoveredParams / (double) nParams;

    }
}
