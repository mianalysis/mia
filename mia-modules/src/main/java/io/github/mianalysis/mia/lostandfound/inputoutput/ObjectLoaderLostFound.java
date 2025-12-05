package io.github.mianalysis.mia.lostandfound.inputoutput;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.inputoutput.LoadObjectsFromCSV;
import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;

@Plugin(type = LostAndFoundItem.class, priority = Priority.LOW, visible = true)
public class ObjectLoaderLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new LoadObjectsFromCSV(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{"Load objects"};
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        HashMap<String,String> parameterNames = new HashMap<String,String>();
        parameterNames.put("Output parent clusters name", LoadObjectsFromCSV.PARENT_OBJECTS_NAME);
        parameterNames.put("Output tracks clusters name", LoadObjectsFromCSV.PARENT_OBJECTS_NAME);
        parameterNames.put("Calibration source", LoadObjectsFromCSV.PARENT_OBJECTS_NAME);
        parameterNames.put("Calibration reference image", LoadObjectsFromCSV.PARENT_OBJECTS_NAME);
        
        return parameterNames;

    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        return new HashMap<String, HashMap<String, String>>();
    }
    
}
