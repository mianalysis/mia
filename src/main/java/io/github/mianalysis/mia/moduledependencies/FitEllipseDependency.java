// package io.github.mianalysis.mia.moduledependencies;

// import org.scijava.Priority;
// import org.scijava.plugin.Plugin;

// @Plugin(type = Dependency.class, priority=Priority.LOW, visible=true)
// public class FitEllipseDependency extends Dependency {
//     @Override
//     public String getModuleName() {
//         return "FitEllipse";
//     }

//     @Override
//     public String getClassName() {
//         return "org.bonej.geometry.FitEllipse";
//     }

//     @Override
//     public String getMessage() {
//         return "Please install BoneJ dependency";
//     }

//     @Override
//     public String getVersionThreshold() {
//         return "1.0.0";
//     }

//     @Override
//     public Relationship getRelationship() {
//         return Relationship.GREATER_THAN_OR_EQUAL_TO;
//     }
// }
