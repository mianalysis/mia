package io.github.mianalysis.mia.moduledependencies;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

@Plugin(type = Dependency.class, priority = Priority.LOW, visible = true)
public class SpotDetectionDependency extends Dependency {
    @Override
    public String getModuleName() {
        return "SpotDetection";
    }

    @Override
    public String getClassName() {
        return "fiji.plugin.trackmate.TrackMate";
    }

    @Override
    public String getMessage() {
        return "Please update TrackMate dependency";
    }
    
    @Override
    public String getVersionThreshold() {
        return "7.2.0";
    }

    @Override
    public Relationship getRelationship() {
        return Relationship.GREATER_THAN_OR_EQUAL_TO;
    }
}
