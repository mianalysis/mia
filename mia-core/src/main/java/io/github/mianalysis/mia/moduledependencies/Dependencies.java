package io.github.mianalysis.mia.moduledependencies;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.scijava.InstantiableException;
import org.scijava.plugin.PluginInfo;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.process.ClassHunter;

public class Dependencies {
    private HashMap<String, HashSet<Dependency>> dependencies = null;

    public boolean compatible(String moduleName, boolean rescan) {
        // Check if dependencies have already been searched for
        if (dependencies == null || rescan)
            updateDependencies();

        boolean compatible = true;

        if (dependencies.containsKey(moduleName))
            for (Dependency dependency : dependencies.get(moduleName))
                if (!dependency.test())
                    compatible = false;
        
        return compatible;

    }

    public HashSet<Dependency> getDependencies(String moduleName, boolean rescan) {
        // Check if dependencies have already been searched for
        if (dependencies == null || rescan)
            updateDependencies();

        return dependencies.get(moduleName);

    }

    public void updateDependencies() {
        dependencies = new HashMap<>();
        
        List<PluginInfo<Dependency>> plugins = ClassHunter.getPlugins(Dependency.class);
        for (PluginInfo<Dependency> plugin : plugins) {
            try {
                Dependency dependency = plugin.createInstance();
                dependencies.putIfAbsent(dependency.getModuleName(), new HashSet<Dependency>());
                dependencies.get(dependency.getModuleName()).add(dependency);
            } catch (InstantiableException e) {
                MIA.log.writeError(e);
            }
        }
    }
}
