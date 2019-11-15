package wbif.sjx.MIA.Process;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;

import java.util.List;
import java.util.Set;

public class ClassHunter<T> {
    private static List<String> classNames = null;

    static public List<String> getModules(boolean rescan, boolean debugOn) {
        // Check if classNames have already been searched for
        if (classNames != null &! rescan) return classNames;

        // Otherwise, scan for classNames
        return new ClassHunter<Module>().getClasses(Module.class, debugOn);

    }

    public List<String> getClasses(Class<T> clazz, boolean debugOn) {
        ScanResult scanResult = new ClassGraph().enableClassInfo().scan();
        ClassInfoList classInfos = scanResult.getSubclasses(clazz.getName());

        if (clazz.getPackage().getName().equals(Module.class.getPackage().getName())) classNames = classInfos.getNames();

        return classInfos.getNames();

    }
}
