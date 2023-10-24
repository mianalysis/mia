package io.github.mianalysis.mia.lostandfound.inputoutput;

import java.util.HashMap;

import io.github.mianalysis.mia.module.inputoutput.ObjectLoader;
import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;

public class ObjectLoaderLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new ObjectLoader(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{""};
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        HashMap<String,String> parameterNames = new HashMap<String,String>();
        parameterNames.put("Output parent clusters name", ObjectLoader.PARENT_OBJECTS_NAME);
        parameterNames.put("Output tracks clusters name", ObjectLoader.PARENT_OBJECTS_NAME);
        parameterNames.put("Calibration source", ObjectLoader.PARENT_OBJECTS_NAME);
        parameterNames.put("Calibration reference image", ObjectLoader.PARENT_OBJECTS_NAME);
        
        return parameterNames;

    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        return new HashMap<String, HashMap<String, String>>();
    }
    
}
