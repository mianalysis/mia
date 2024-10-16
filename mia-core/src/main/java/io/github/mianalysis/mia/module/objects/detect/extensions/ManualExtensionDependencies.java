package io.github.mianalysis.mia.module.objects.detect.extensions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.scijava.InstantiableException;
import org.scijava.plugin.PluginInfo;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.process.ClassHunter;

public class ManualExtensionDependencies {
    private HashMap<String, HashSet<ManualExtensionDependency>> dependencies = null;

    public boolean compatible(String extensionName, boolean rescan) {
        // Check if dependencies have already been searched for
        if (dependencies == null || rescan)
            updateDependencies();

        boolean compatible = true;
        if (dependencies.containsKey(extensionName))        
            for (ManualExtensionDependency dependency : dependencies.get(extensionName)) {
                if (!dependency.test())
                    compatible = false;
        }
        
        return compatible;

    }

    public HashSet<ManualExtensionDependency> getDependencies(String extensionName, boolean rescan) {
        // Check if dependencies have already been searched for
        if (dependencies == null || rescan)
            updateDependencies();

        return dependencies.get(extensionName);

    }

    public void updateDependencies() {
        dependencies = new HashMap<>();
         
        List<PluginInfo<ManualExtensionDependency>> plugins = ClassHunter.getPlugins(ManualExtensionDependency.class);        
        for (PluginInfo<ManualExtensionDependency> plugin : plugins) {
            try {
                ManualExtensionDependency dependency = plugin.createInstance();
                dependencies.putIfAbsent(dependency.getExtensionName(), new HashSet<ManualExtensionDependency>());
                dependencies.get(dependency.getExtensionName()).add(dependency);
            } catch (InstantiableException e) {
                MIA.log.writeError(e);
            }
        }
    }
}
