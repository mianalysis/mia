package io.github.mianalysis.mia.moduledependencies;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

@Plugin(type = Dependency.class, priority=Priority.LOW, visible=true)
public class RidgeDetectionDependency extends Dependency {
    @Override
    public String getModuleName() {
        return "RidgeDetection";
    }

    @Override
    public String getClassName() {
        return "de.biomedical_imaging.ij.steger.LineDetector";
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
