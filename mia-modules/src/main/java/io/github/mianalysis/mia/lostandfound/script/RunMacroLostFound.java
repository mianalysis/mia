package io.github.mianalysis.mia.lostandfound.script;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;
import io.github.mianalysis.mia.module.script.RunMacro;

@Plugin(type = LostAndFoundItem.class, priority = Priority.LOW, visible = true)
public class RunMacroLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new RunMacro(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{"RunMacroOnImage"};
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
