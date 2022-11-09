package io.github.mianalysis.mia.moduledependencies;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

@Plugin(type = Dependency.class, priority = Priority.LOW, visible = true)
public class StarDistDetectionStarDistDependency extends Dependency {
    @Override
    public String getModuleName() {
        return "StarDistDetection";
    }

    @Override
    public String getClassName() {
        return "de.csbdresden.stardist.StarDist2DModel";
    }

    @Override
    public String getVersionThreshold() {
        return "0.0.0";
    }

    @Override
    public Relationship getRelationship() {
        return Relationship.GREATER_THAN_OR_EQUAL_TO;
    }

    @Override
    public String getMessage() {
        return "Please enable StarDist update site (see https://imagej.net/plugins/stardist for more information)";
    }
}
