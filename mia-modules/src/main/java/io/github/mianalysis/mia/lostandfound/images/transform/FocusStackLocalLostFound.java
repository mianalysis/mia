package io.github.mianalysis.mia.lostandfound.images.transform;

import java.util.HashMap;

import io.github.mianalysis.mia.module.images.transform.FocusStackLocal;
import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;

public class FocusStackLocalLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new FocusStackLocal(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{"Focus stack"};
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        HashMap<String,String> parameterNames = new HashMap<String,String>();
        parameterNames.put("Show height image", null);
        
        return parameterNames;

    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        return new HashMap<String, HashMap<String, String>>();
    }
    
}
