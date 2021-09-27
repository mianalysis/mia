package io.github.mianalysis.mia.process;

import java.util.ArrayList;
import java.util.List;

import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Module;

public class ClassHunter<T> {
    private static List<String> moduleNames = null;

    static public List<String> getModules(boolean rescan) {
        // Check if moduleNames have already been searched for
        if (moduleNames != null &! rescan) return moduleNames;

        // Otherwise, scan for moduleNames
        return new ClassHunter<Module>().getClasses(Module.class);

    }

    public List<String> getClasses(Class<T> clazz) {
        PluginService pluginService = MIA.ijService.getContext().getService(PluginService.class);
        List<PluginInfo<Module>> modules = pluginService.getPluginsOfType(Module.class);
        List<String> names = new ArrayList<>();
        
        for (PluginInfo<Module> module : modules)
            names.add(module.getClassName());
        
        return names;

    }
}
