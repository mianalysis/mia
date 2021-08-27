package wbif.sjx.MIA.Process;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;

public class ModuleSearcher {
    HashMap<String, String> moduleDescriptions = new HashMap<>();
    HashMap<String, String[]> parameterDescriptions = new HashMap<>();

    public ModuleSearcher() {
        List<String> classNames = ClassHunter.getModules(false);

        // Converting the list of classes to a list of Modules
        for (String className : classNames) {
            try {
                Class<Module> clazz = (Class<Module>) Class.forName(className);

                // Skip any abstract Modules
                if (Modifier.isAbstract(clazz.getModifiers()))
                    continue;

                Constructor<Module> constructor = clazz.getDeclaredConstructor(ModuleCollection.class);
                Module module = (Module) constructor.newInstance(new ModuleCollection());

                moduleDescriptions.put(module.getName(), module.getDescription());

                ParameterCollection parameters = module.getAllParameters();
                String[] descriptions = new String[parameters.size()];
                int i = 0;
                for (Parameter parameter:parameters.values())
                    descriptions[i++] = parameter.getDescription();

                parameterDescriptions.put(module.getName(), descriptions);

            } catch (InvocationTargetException | InstantiationException | IllegalAccessException
                    | IllegalArgumentException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public ArrayList<SearchMatch> getMatches(String target, boolean includeModuleDescriptions, boolean includeParameterDescriptions) {
        ArrayList<SearchMatch> matches = new ArrayList<>();

        return matches;
        
    }

    public class SearchMatch {
        // private final String moduleName;
        // private final ArrayList<String> textMatches
    }
}