package io.github.mianalysis.mia.lostandfound.script;

import java.util.HashMap;

import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;
import io.github.mianalysis.mia.module.script.RunSingleCommand;

public class RunSingleCommandLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new RunSingleCommand(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{"RunSingleMacroCommand"};
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        HashMap<String,String> parameterNames = new HashMap<String,String>();
        parameterNames.put("Macro title", RunSingleCommand.COMMAND);
        
        return parameterNames;
    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        return new HashMap<String, HashMap<String, String>>();
    }
    
}
