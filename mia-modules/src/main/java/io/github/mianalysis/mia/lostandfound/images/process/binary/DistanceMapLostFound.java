package io.github.mianalysis.mia.lostandfound.images.process.binary;

import java.util.HashMap;

import io.github.mianalysis.mia.module.images.process.binary.DistanceMap;
import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;

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
        return new HashMap<String, HashMap<String, String>>();
    }
    
}
