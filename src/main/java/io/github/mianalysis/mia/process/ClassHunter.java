package io.github.mianalysis.mia.process;

import java.util.ArrayList;
import java.util.List;

import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.SciJavaPlugin;

import io.github.mianalysis.mia.MIA;

public class ClassHunter<T extends SciJavaPlugin> {
    public static <T extends SciJavaPlugin> List<PluginInfo<T>> getPlugins(Class<T> clazz) {        
        return MIA.getPluginService().getPluginsOfType(clazz);
    }

    public static <T extends SciJavaPlugin> List<String> getClassNames(Class<T> clazz) {
        List<PluginInfo<T>> plugins = getPlugins(clazz);
        List<String> names = new ArrayList<>();

        for (PluginInfo<T> plugin : plugins)
            names.add(plugin.getClassName());

        return names;

    }
}
