package io.github.mianalysis.mia.lostandfound.objects.measure.miscellaneous;

import java.util.HashMap;

import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;
import io.github.mianalysis.mia.module.objects.measure.miscellaneous.ReplaceMeasurementValue;

public class ReplaceMeasurementValueLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new ReplaceMeasurementValue(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{""};
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        HashMap<String,String> parameterNames = new HashMap<String,String>();
        parameterNames.put("Value to replace", ReplaceMeasurementValue.REFERENCE_VALUE);
        
        return parameterNames;

    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        return new HashMap<String, HashMap<String, String>>();
    }
    
}
