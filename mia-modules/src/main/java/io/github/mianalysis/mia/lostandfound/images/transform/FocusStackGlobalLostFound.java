package io.github.mianalysis.mia.lostandfound.images.transform;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.images.transform.FocusStackGlobal;
import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;

@Plugin(type = LostAndFoundItem.class, priority = Priority.LOW, visible = true)
public class FocusStackGlobalLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new FocusStackGlobal(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{"Best focus stack"};
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        return new HashMap<String,String>();
    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        return new HashMap<String, HashMap<String, String>>();
    }
    
}
