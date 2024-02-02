package io.github.mianalysis.mia.lostandfound.images.process.binary;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.images.process.binary.DistanceMap;
import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;

@Plugin(type = LostAndFoundItem.class, priority = Priority.LOW, visible = true)
public class DistanceMapLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new DistanceMap(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{""};
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        HashMap<String,String> parameterNames = new HashMap<String,String>();
        parameterNames.put("Spatial units", DistanceMap.SPATIAL_UNITS_MODE);
        
        return parameterNames;

    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        HashMap<String, String> values = null;
        HashMap<String, HashMap<String, String>> parameterValues = null;

        values = new HashMap<>();
        values.put("Borgefors (3,4,5)", DistanceMap.WeightModes.BORGEFORS);
        values.put("City-Block (1,2,3)", DistanceMap.WeightModes.CITY_BLOCK);
        values.put("Svensson (3,4,5,7)", DistanceMap.WeightModes.WEIGHTS_3_4_5_7);
        parameterValues = new HashMap<>();
        parameterValues.put(DistanceMap.WEIGHT_MODE, values);
        
        return parameterValues;

    }
}
