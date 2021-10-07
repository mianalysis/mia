package io.github.mianalysis.mia.process;

import java.util.ArrayList;
import java.util.List;

import org.scijava.Context;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;

import ij.IJ;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Module;
import net.imagej.ImageJ;
import net.imagej.ImageJService;

public class ClassHunter<T extends org.scijava.plugin.SciJavaPlugin> {

    private static List<String> moduleNames = null;

    static public List<String> getModules(boolean rescan) {
        // Check if moduleNames have already been searched for
        if (moduleNames != null & !rescan)
            return moduleNames;

        // Otherwise, scan for moduleNames
        return new ClassHunter<Module>().getClasses(Module.class);

    }

    public List<String> getClasses(Class<T> clazz) {
        Context context;
        ImageJService iJService = MIA.ijService;
        if (iJService == null) {
            ImageJ ij = new ImageJ();  
            context = ( Context ) IJ.runPlugIn( "org.scijava.Context", "" );
        } else {
            context = iJService.getContext();
        }

        PluginService pluginService = context.getService(PluginService.class);
        List<PluginInfo<T>> modules = pluginService.getPluginsOfType(clazz);
        List<String> names = new ArrayList<>();

        for (PluginInfo<T> module : modules)
            names.add(module.getClassName());

        return names;

    }
}
