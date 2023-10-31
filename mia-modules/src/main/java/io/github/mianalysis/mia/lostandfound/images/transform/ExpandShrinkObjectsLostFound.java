package io.github.mianalysis.mia.lostandfound.images.transform;

import java.util.HashMap;

import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;
import io.github.mianalysis.mia.module.objects.transform.ExpandShrinkObjects;

public class ExpandShrinkObjectsLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new ExpandShrinkObjects(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{""};
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        HashMap<String,String> parameterNames = new HashMap<String,String>();
        parameterNames.put("Radius change (px)", ExpandShrinkObjects.RADIUS_CHANGE);
        
        return parameterNames;

    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        return new HashMap<String, HashMap<String, String>>();
    }
    
}
