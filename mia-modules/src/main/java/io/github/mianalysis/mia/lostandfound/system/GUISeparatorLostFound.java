package io.github.mianalysis.mia.lostandfound.system;

import java.util.HashMap;

import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;
import io.github.mianalysis.mia.module.system.GUISeparator;

public class GUISeparatorLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new GUISeparator(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{""};
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        HashMap<String,String> parameterNames = new HashMap<String,String>();
        parameterNames.put("Show basic", GUISeparator.SHOW_PROCESSING);
        parameterNames.put("Expanded basic GUI", GUISeparator.EXPANDED_PROCESSING);
        parameterNames.put("Expanded editing GUI", GUISeparator.EXPANDED_EDITING);
        
        return parameterNames;

    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        return new HashMap<String, HashMap<String, String>>();
    }
    
}
