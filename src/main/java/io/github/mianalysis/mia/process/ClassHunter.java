package io.github.mianalysis.mia.process;

import java.util.ArrayList;
import java.util.List;

import org.scijava.Context;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.plugin.SciJavaPlugin;

import ij.IJ;
import io.github.mianalysis.mia.MIA;
import net.imagej.ImageJ;
import net.imagej.ImageJService;

public class ClassHunter<T extends SciJavaPlugin> {
    public static <T extends SciJavaPlugin> List<PluginInfo<T>> getPlugins(Class<T> clazz) {
        Context context;
        ImageJService iJService = MIA.ijService;
        if (iJService == null) {
            ImageJ ij = new ImageJ();
            context = ( Context ) IJ.runPlugIn( "org.scijava.Context", "" );
        } else {
            context = iJService.getContext();
        }

        PluginService pluginService = context.getService(PluginService.class);
        return pluginService.getPluginsOfType(clazz);

    }

    public static <T extends SciJavaPlugin> List<String> getClassNames(Class<T> clazz) {
        List<PluginInfo<T>> plugins = getPlugins(clazz);
        List<String> names = new ArrayList<>();

        for (PluginInfo<T> plugin : plugins)
            names.add(plugin.getClassName());

        return names;

    }
}
