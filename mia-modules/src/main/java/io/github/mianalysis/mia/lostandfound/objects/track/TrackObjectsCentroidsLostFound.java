package io.github.mianalysis.mia.lostandfound.objects.track;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;
import io.github.mianalysis.mia.module.objects.track.TrackObjectsCentroids;

@Plugin(type = LostAndFoundItem.class, priority = Priority.LOW, visible = true)
public class TrackObjectsCentroidsLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new TrackObjectsCentroids(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[] { "Centroid tracking 3D" };
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        HashMap<String, String> parameterNames = new HashMap<>();

        return parameterNames;

    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        HashMap<String, HashMap<String, String>> parameterValues = new HashMap<>();

        return parameterValues;

    }

}
