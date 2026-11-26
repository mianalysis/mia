package io.github.mianalysis.mia.moduledependencies;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

@Plugin(type = Dependency.class, priority=Priority.LOW, visible=true)
public class FitSnakeDependency extends Dependency {
    @Override
    public String getModuleName() {
        return "FitSnake";
    }

    @Override
    public String getClassName() {
        return "ABSnake.SnakeConfig";
    }

    @Override
    public String getMessage() {
        return "Please download absnake_.jar from https://web.archive.org/web/20260614160040/https://imagejdocu.list.lu/_media/plugin/segmentation/active_contour/absnake_.jar and put it in the \"plugins\" folder of your Fiji)";
    }

    @Override
    public String getVersionThreshold() {
        return "";
    }

    @Override
    public Relationship getRelationship() {
        return Relationship.ANY;
    }
}
