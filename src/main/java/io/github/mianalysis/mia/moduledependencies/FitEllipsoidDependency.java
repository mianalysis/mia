package io.github.mianalysis.mia.moduledependencies;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

@Plugin(type = Dependency.class, priority=Priority.LOW, visible=true)
public class FitEllipsoidDependency extends Dependency {
    @Override
    public String getModuleName() {
        return "FitEllipsoid";
    }

    @Override
    public String getClassName() {
        return "org.bonej.geometry.FitEllipsoid";
    }

    @Override
    public String getMessage() {
        return "Please install BoneJ dependency";
    }
    
    @Override
    public String getVersionThreshold() {
        return "1.0.0";
    }

    @Override
    public Relationship getRelationship() {
        return Relationship.GREATER_THAN_OR_EQUAL_TO;
    }
}
