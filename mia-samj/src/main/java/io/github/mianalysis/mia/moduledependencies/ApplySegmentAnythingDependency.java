package io.github.mianalysis.mia.moduledependencies;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

@Plugin(type = Dependency.class, priority=Priority.LOW, visible=true)
public class ApplySegmentAnythingDependency extends Dependency {
    @Override
    public String getModuleName() {
        return "ApplySegmentAnything";
    }

    @Override
    public String getClassName() {
        return "ai.nets.samj.models.AbstractSamJ";
    }

    @Override
    public String getMessage() {
        return "Please enable SAMJ update site";
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
