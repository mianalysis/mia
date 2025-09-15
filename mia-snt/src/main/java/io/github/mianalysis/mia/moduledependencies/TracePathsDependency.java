package io.github.mianalysis.mia.moduledependencies;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

@Plugin(type = Dependency.class, priority=Priority.LOW, visible=true)
public class TracePathsDependency extends Dependency {
    @Override
    public String getModuleName() {
        return "TracePaths";
    }

    @Override
    public String getClassName() {
        return "sc.fiji.snt.tracing.cost.Cost";
    }

    @Override
    public String getMessage() {
        return "Please enable Neuroanatomy update site to get SNT";
    }

    @Override
    public String getVersionThreshold() {
        return "0.0.1";
    }

    @Override
    public Relationship getRelationship() {
        return Relationship.GREATER_THAN_OR_EQUAL_TO;
    }
}
