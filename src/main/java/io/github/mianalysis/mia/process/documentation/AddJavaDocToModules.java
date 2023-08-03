package io.github.mianalysis.mia.process.documentation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.AvailableModules;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;

public class AddJavaDocToModules {
    public static void main(String[] args) throws IOException {
        TreeMap<String,Module> modules = getModules();
        for (String moduleName:modules.keySet()) {
            String path = "/Users/sc13967/Documents/Programming/Java/mia/src/main/java/"+modules.get(moduleName).getClass().getName().replace(".", "/")+".java";
            System.out.println(path);

            // Get source code as String
            StringBuilder sb = new StringBuilder();
            Iterator<String> iter = new BufferedReader(new FileReader(path)).lines().iterator();
            while (iter.hasNext())
                sb.append(iter.next()).append("\n");
            String code = sb.toString();

            // // Iterate over each parameter in module, adding description
            // for (Parameter parameter:modules.get(moduleName).getAllParameters().values()) {
            //     String parameterName = parameter.getName();
            //     String description = parameter.getDescription();

            //     // Find parameter declaration line
            //     Pattern pattern = Pattern.compile("\\n[^\\n]+ = \""+parameterName+"\";");
            //     Matcher matcher = pattern.matcher(code);

            //     if (matcher.find()) {
            //         String textBefore = code.substring(0, matcher.start());
            //         String textAfter = code.substring(matcher.start());
            //         code = textBefore+"\n\n\t/**\n\t* "+description+"\n\t*/"+textAfter;
            //     }

            // }

            // // Replace module description
            // String parameterName = parameter.getName();
            // String description = parameter.getDescription();

            // // Find parameter declaration line
            // Pattern pattern = Pattern.compile("\\n[^\\n]+ = \""+parameterName+"\";");
            // Matcher matcher = pattern.matcher(code);

            // if (matcher.find()) {
            //     String textBefore = code.substring(0, matcher.start());
            //     String textAfter = code.substring(matcher.start());
            //     code = textBefore+"\n\n\t/**\n\t* "+description+"\n\t*/"+textAfter;
            // }


            FileWriter fileWriter = new FileWriter(path);
            fileWriter.write(code);
            fileWriter.flush();
            fileWriter.close();

        }
    }

    private static TreeMap<String, Module> getModules() {
        // Get a list of Modules
        List<String> moduleNames = AvailableModules.getModuleNames(false);

        // Converting the list of classes to a list of Modules
        TreeMap<String, Module> modules = new TreeMap<>();
        Modules tempCollection = new Modules();
        for (String className : moduleNames) {
            try {
                Class<Module> clazz = (Class<Module>) Class.forName(className);

                // Skip any abstract Modules
                if (Modifier.isAbstract(clazz.getModifiers()))
                    continue;

                Constructor<Module> constructor = clazz.getDeclaredConstructor(Modules.class);
                Module module = (Module) constructor.newInstance(tempCollection);
                modules.put(module.getName(), module);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException
                    | InvocationTargetException e) {
                MIA.log.writeError(e);
            }
        }

        return modules;

    }
}
