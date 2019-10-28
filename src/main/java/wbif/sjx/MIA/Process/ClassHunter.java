package wbif.sjx.MIA.Process;

import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;

import java.net.URL;
import java.util.Set;

public class ClassHunter<T> {
    private static Set<Class<? extends Module>> modules = null;

    static public Set<Class<? extends Module>> getModules(boolean rescan, boolean debugOn) {
        // Check if modules have already been searched for
        if (modules != null &! rescan) return modules;

        // Otherwise, scan for modules
        return new ClassHunter<Module>().getClasses(Module.class, debugOn);

    }

    public Set<Class<? extends T>> getClasses(Class<T> clazz, boolean debugOn) {
        Reflections.log = null;
        ConfigurationBuilder builder = ConfigurationBuilder.build("");
        builder.addUrls(ClasspathHelper.forPackage("wbif.sjx.MIA"));
        for (String packageName:MIA.getPluginPackages()) builder.addUrls(ClasspathHelper.forPackage(packageName));
        if (!debugOn) {
            for (URL url : ClasspathHelper.forClassLoader()) {
                if (url.getPath().contains("plugins")) builder.addUrls(url);
            }
        }

        return new Reflections(builder).getSubTypesOf(clazz);

    }
}
