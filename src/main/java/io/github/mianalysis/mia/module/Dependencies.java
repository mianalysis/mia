package io.github.mianalysis.mia.module;

import java.util.HashMap;
import java.util.HashSet;

public class Dependencies {
    private HashMap<String, HashSet<Dependency>> dependencies = new HashMap<>();

    public Dependencies() {
        // RidgeDetection
        HashSet<Dependency> moduleDependencies = new HashSet<>();
        moduleDependencies.add(new Dependency("de.biomedical_imaging.ij.steger.LineDetector", "1.0.0", Dependency.Relationship.GREATER_THAN_OR_EQUAL_TO));
        dependencies.put("RidgeDetection", moduleDependencies);

        // RunTrackMate
        moduleDependencies = new HashSet<>();
        moduleDependencies.add(new Dependency("fiji.plugin.trackmate.TrackMate", "7.0.0", Dependency.Relationship.GREATER_THAN_OR_EQUAL_TO));
        dependencies.put("RunTrackMate", moduleDependencies);

        // SpotDetection
        moduleDependencies = new HashSet<>();
        moduleDependencies.add(new Dependency("fiji.plugin.trackmate.TrackMate", "7.0.0", Dependency.Relationship.GREATER_THAN_OR_EQUAL_TO));
        dependencies.put("SpotDetection", moduleDependencies);

        // TrackEditor
        moduleDependencies = new HashSet<>();
        moduleDependencies.add(new Dependency("fiji.plugin.trackmate.TrackMate", "7.0.0", Dependency.Relationship.GREATER_THAN_OR_EQUAL_TO));
        dependencies.put("TrackEditor", moduleDependencies);

        // WekaProbabilityMaps
        moduleDependencies = new HashSet<>();
        moduleDependencies.add(new Dependency("trainableSegmentation.WekaSegmentation", "3.2.35", Dependency.Relationship.NOT_EQUAL_TO));
        dependencies.put("WekaProbabilityMaps", moduleDependencies);

    }

    public void addDependency(String moduleName, Dependency dependency) {
        dependencies.putIfAbsent(moduleName, new HashSet<Dependency>());
        dependencies.get(moduleName).add(dependency);

    }

    public boolean compatible(String moduleName) {
        boolean compatible = true;

        if (dependencies.containsKey(moduleName))
            for (Dependency dependency : dependencies.get(moduleName))
                if (!dependency.test())
                    compatible = false;

        return compatible;

    }
    
    public HashSet<Dependency> getDependencies(String moduleName) {
        return dependencies.get(moduleName);
    }
}
