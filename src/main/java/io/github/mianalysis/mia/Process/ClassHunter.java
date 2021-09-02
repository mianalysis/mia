package io.github.mianalysis.mia.Process;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Module;

import java.util.List;

public class ClassHunter<T> {
    private static List<String> moduleNames = null;

    static public List<String> getModules(boolean rescan) {
        // Check if moduleNames have already been searched for
        if (moduleNames != null &! rescan) return moduleNames;

        // Otherwise, scan for moduleNames
        return new ClassHunter<Module>().getClasses(Module.class);

    }

    public List<String> getClasses(Class<T> clazz) {
        ScanResult scanResult = new ClassGraph().enableClassInfo().scan();
        ClassInfoList classInfos = scanResult.getSubclasses(clazz.getName());

        if (clazz.getPackage().getName().equals(Module.class.getPackage().getName())) {
            moduleNames = classInfos.getNames();

            // Add any packages from the explicitly named list
            moduleNames.addAll(MIA.getPluginPackages());

        }

        List<String> classNames = classInfos.getNames();

        scanResult.close();
        scanResult = null;
        classInfos = null;

        return classNames;

    }
}
