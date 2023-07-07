package io.github.mianalysis.mia.moduledependencies;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

@Plugin(type = Dependency.class, priority = Priority.LOW, visible = true)
public class ApplyDeepImageJModelDependency extends Dependency {
    @Override
    public String getModuleName() {
        return "ApplyDeepImageJModel";
    }

    @Override
    public String getClassName() {
        return "deepimagej.DeepImageJ";
    }

    @Override
    public String getMessage() {
        return "MIA currently only supports DeepImageJ up to version 2.1.16.  Compatibility with DeepImageJ version 3 and above will be added in the coming weeks (target end of August 2023).  DeepImageJ 2.1.16 and its dependencies can be downloaded from https://github.com/deepimagej/deepimagej-plugin/releases/tag/2.1.16.";
    }

    @Override
    public String getVersionThreshold() {
        return "2.1.16";
    }

    @Override
    public Relationship getRelationship() {
        return Relationship.LESS_THAN_OR_EQUAL_TO;
    }
}
