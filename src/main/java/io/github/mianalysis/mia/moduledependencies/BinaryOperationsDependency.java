package io.github.mianalysis.mia.moduledependencies;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

@Plugin(type = Dependency.class, priority=Priority.LOW, visible=true)
public class BinaryOperationsDependency extends Dependency {
    @Override
    public String getModuleName() {
        return "BinaryOperations";
    }

    @Override
    public String getClassName() {
        return "inra.ijpb.plugins.GeodesicDistanceMap3DPlugin";
    }

    @Override
    public String getVersionThreshold() {
        return "1.5.0";
    }

    @Override
    public Relationship getRelationship() {
        return Relationship.GREATER_THAN_OR_EQUAL_TO;
    }
}
