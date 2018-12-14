package wbif.sjx.ModularImageAnalysis.Process;

import com.sun.org.apache.xpath.internal.operations.Mod;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import wbif.sjx.ModularImageAnalysis.MIA;
import wbif.sjx.ModularImageAnalysis.Module.Module;

import java.net.URL;
import java.util.Set;

public class ModuleReader {
    public static Set<Class<? extends Module>> getModules(boolean debugOn) {
        Reflections.log = null;
        ConfigurationBuilder builder = ConfigurationBuilder.build("");
        builder.addUrls(ClasspathHelper.forPackage("wbif.sjx.ModularImageAnalysis"));
        for (String packageName:MIA.getPluginPackages()) builder.addUrls(ClasspathHelper.forPackage(packageName));
        if (!debugOn) {
            for (URL url : ClasspathHelper.forClassLoader()) {
                if (url.getPath().contains("plugins")) builder.addUrls(url);
            }
        }

        return new Reflections(builder).getSubTypesOf(Module.class);

    }
}
