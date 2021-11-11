package io.github.mianalysis.mia.moduledependencies;

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
    public String getVersionThreshold() {
        return "7.0.0";
    }

    @Override
    public Relationship getRelationship() {
        return Relationship.GREATER_THAN_OR_EQUAL_TO;
    }
}
