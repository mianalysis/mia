package io.github.mianalysis.mia.lostandfound.objects.measure.spatial;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;
import io.github.mianalysis.mia.module.objects.measure.spatial.CalculateNearestNeighbour;

@Plugin(type = LostAndFoundItem.class, priority = Priority.LOW, visible = true)
public class CalculateNearestNeighbourLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new CalculateNearestNeighbour(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{""};
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        HashMap<String,String> parameterNames = new HashMap<String,String>();
        parameterNames.put("ParentChildRef mode", CalculateNearestNeighbour.RELATIONSHIP_MODE);
        
        return parameterNames;

    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        HashMap<String, String> values = null;
        HashMap<String, HashMap<String, String>> parameterValues = null;

        values = new HashMap<>();
        values.put("Centroid", CalculateNearestNeighbour.ReferenceModes.CENTROID_3D);
        values.put("Surface", CalculateNearestNeighbour.ReferenceModes.SURFACE_3D);
        
        parameterValues = new HashMap<>();
        parameterValues.put(CalculateNearestNeighbour.REFERENCE_MODE, values);

        return parameterValues;
    
    }
}
