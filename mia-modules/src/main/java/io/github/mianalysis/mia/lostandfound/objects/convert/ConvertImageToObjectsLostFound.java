package io.github.mianalysis.mia.lostandfound.objects.convert;

import java.util.HashMap;

import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;
import io.github.mianalysis.mia.module.objects.convert.ConvertImageToObjects;

public class ConvertImageToObjectsLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new ConvertImageToObjects(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{""};
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        HashMap<String,String> parameterNames = new HashMap<String,String>();
        parameterNames.put("Create parent objects", ConvertImageToObjects.CREATE_TRACKS);
        parameterNames.put("Output track objects name", ConvertImageToObjects.TRACK_OBJECTS_NAME);
        
        return parameterNames;

    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        return new HashMap<String, HashMap<String, String>>();
    }
    
}
