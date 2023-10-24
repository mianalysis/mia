package io.github.mianalysis.mia.lostandfound.objects.process;

import java.util.HashMap;

import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;
import io.github.mianalysis.mia.module.objects.process.GetLocalObjectRegion;

public class GetObjectLocalRegionLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new GetLocalObjectRegion(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{""};
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        HashMap<String,String> parameterNames = new HashMap<String,String>();
        parameterNames.put("Local radius", GetLocalObjectRegion.FIXED_VALUE_FOR_RADIUS);
        parameterNames.put("Fixed value", GetLocalObjectRegion.FIXED_VALUE_FOR_RADIUS);
        parameterNames.put("Measurement name", GetLocalObjectRegion.RADIUS_MEASUREMENT);
        parameterNames.put("Parent object", GetLocalObjectRegion.PARENT_OBJECT_FOR_RADIUS);
        
        return parameterNames;

    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        return new HashMap<String, HashMap<String, String>>();
    }
    
}
