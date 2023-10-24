package io.github.mianalysis.mia.lostandfound.images.process.binary;

import java.util.HashMap;

import io.github.mianalysis.mia.module.images.process.binary.BinaryOperations;
import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;

public class BinaryOperationsLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new BinaryOperations(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{""};
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        HashMap<String,String> parameterNames = new HashMap<String,String>();
        parameterNames.put("Connectivity (3D)", BinaryOperations.CONNECTIVITY);
        
        return parameterNames;

    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        return new HashMap<String, HashMap<String, String>>();
    }
    
}
