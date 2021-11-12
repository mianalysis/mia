package io.github.mianalysis.mia.module;

import java.util.List;

import io.github.mianalysis.mia.process.ClassHunter;

public class AvailableModules {
    private static List<String> moduleNames = null;

    static public List<String> getModuleNames(boolean rescan) {
        // Check if moduleNames have already been searched for
        if (moduleNames != null & !rescan)
            return moduleNames;

        // Otherwise, scan for moduleNames
        return new ClassHunter<Module>().getClassNames(Module.class);

    }
}
