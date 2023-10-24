package io.github.mianalysis.mia.lostandfound.images.process.threshold;

import java.util.HashMap;

import io.github.mianalysis.mia.module.images.process.threshold.LocalAutoThreshold;
import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;

public class LocalThresholdLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new LocalAutoThreshold(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{""};
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        HashMap<String,String> parameterNames = new HashMap<String,String>();
        parameterNames.put("Spatial units", LocalAutoThreshold.SPATIAL_UNITS_MODE);
        
        return parameterNames;

    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        return new HashMap<String, HashMap<String, String>>();
    }
    
}
