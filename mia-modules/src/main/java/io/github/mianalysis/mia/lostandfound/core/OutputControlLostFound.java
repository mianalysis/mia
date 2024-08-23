package io.github.mianalysis.mia.lostandfound.core;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.module.core.OutputControl;
import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;
import io.github.mianalysis.mia.object.units.SpatialUnit;

@Plugin(type = LostAndFoundItem.class, priority = Priority.LOW, visible = true)
public class OutputControlLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new OutputControl(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{""};
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        HashMap<String,String> parameterNames = new HashMap<String,String>();
        parameterNames.put("Save location", OutputControl.GROUP_SAVE_LOCATION);
        
        return parameterNames;

    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        return new HashMap<String, HashMap<String, String>>();
    
    }
}
