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
        return "Please enable DeepImageJ update site (see https://github.com/deepimagej/deepimagej-plugin/wiki/Installation-requirements for more information)";
    }

    @Override
    public String getVersionThreshold() {
        return "0.0.0";
    }

    @Override
    public Relationship getRelationship() {
        return Relationship.GREATER_THAN_OR_EQUAL_TO;
    }
}
