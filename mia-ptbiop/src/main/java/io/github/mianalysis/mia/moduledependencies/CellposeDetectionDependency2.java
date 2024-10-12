package io.github.mianalysis.mia.moduledependencies;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

@Plugin(type = Dependency.class, priority=Priority.LOW, visible=true)
public class CellposeDetectionDependency2 extends Dependency {
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
        return "Latest version of PTBIOP Cellpose wrapper not currently supported.  Support to be added in coming weeks.";
    }

    @Override
    public String getVersionThreshold() {
        return "0.9.8";
    }

    @Override
    public Relationship getRelationship() {
        return Relationship.LESS_THAN_OR_EQUAL_TO;
    }
}
