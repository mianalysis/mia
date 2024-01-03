package io.github.mianalysis.mia.moduledependencies;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

@Plugin(type = Dependency.class, priority=Priority.LOW, visible=true)
public class CellposeDetectionDependency extends Dependency {
    @Override
    public String getModuleName() {
        return "CellposeDetection";
    }

    @Override
    public String getClassName() {
        return "ch.epfl.biop.wrappers.cellpose.ij2commands.Cellpose_SegmentImgPlusOwnModelAdvanced";
    }

    @Override
    public String getMessage() {
        return "Please enable PTBIOP update site";
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
