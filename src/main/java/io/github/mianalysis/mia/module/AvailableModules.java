package io.github.mianalysis.mia.module;

import java.util.List;

import io.github.mianalysis.mia.process.ClassHunter;

public class AvailableModules {
    private static List<String> moduleNames = null;

    public static List<String> getModuleNames(boolean rescan) {
        // Check if moduleNames have already been searched for
        if (moduleNames != null & !rescan)
            return moduleNames;

        // Otherwise, scan for moduleNames
        moduleNames = new ClassHunter<Module>().getClassNames(Module.class);

        return moduleNames;

    }

    public static <T extends Module> void addModuleName(Class<T> clazz) {
        if (moduleNames == null)
            getModuleNames(true);
            
        moduleNames.add(clazz.getCanonicalName());

    } 
}
