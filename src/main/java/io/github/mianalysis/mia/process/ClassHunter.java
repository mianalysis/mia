package io.github.mianalysis.mia.process;

import java.util.ArrayList;
import java.util.List;

import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModuleInterface;

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
        List<PluginInfo<ModuleInterface>> modules = pluginService.getPluginsOfType(ModuleInterface.class);
        List<String> names = new ArrayList<>();
        
        for (PluginInfo<ModuleInterface> module : modules)
            names.add(module.getClassName());
        // ScanResult scanResult = new ClassGraph().enableClassInfo().scan();
        // ClassInfoList classInfos = scanResult.getSubclasses(clazz.getName());

        // if (clazz.getPackage().getName().equals(Module.class.getPackage().getName())) {
        //     moduleNames = classInfos.getNames();
        //     System.out.println(moduleNames);

        //     // Add any packages from the explicitly named list
        //     moduleNames.addAll(MIA.getPluginPackages());

        // }

        // List<String> classNames = classInfos.getNames();

        // scanResult.close();
        // scanResult = null;
        // classInfos = null;

        // return classNames;

        return names;

    }
}
