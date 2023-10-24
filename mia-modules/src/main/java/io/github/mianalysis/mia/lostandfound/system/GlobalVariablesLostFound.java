package io.github.mianalysis.mia.lostandfound.system;

import java.util.HashMap;

import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;
import io.github.mianalysis.mia.module.system.GlobalVariables;

public class GlobalVariablesLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new GlobalVariables(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{""};
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        HashMap<String,String> parameterNames = new HashMap<String,String>();
        parameterNames.put("Control type", GlobalVariables.VARIABLE_TYPE);
        parameterNames.put("Variable choice", GlobalVariables.VARIABLE_CHOICE);
        
        return parameterNames;

    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        return new HashMap<String, HashMap<String, String>>();
    }
    
}
