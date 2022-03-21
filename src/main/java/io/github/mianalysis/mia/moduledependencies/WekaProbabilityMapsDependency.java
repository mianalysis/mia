package io.github.mianalysis.mia.moduledependencies;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

@Plugin(type = Dependency.class, priority=Priority.LOW, visible=true)
public class WekaProbabilityMapsDependency extends Dependency {
    @Override
    public String getModuleName() {
        return "WekaProbabilityMaps";
    }

    @Override
    public String getClassName() {
        return "trainableSegmentation.WekaSegmentation";
    }

    @Override
    public String getVersionThreshold() {
        return "3.2.35";
    }

    @Override
    public Relationship getRelationship() {
        return Relationship.NOT_EQUAL_TO;
    }
}
