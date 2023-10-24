package io.github.mianalysis.mia.lostandfound.images.process;

import java.util.HashMap;

import io.github.mianalysis.mia.module.images.process.WekaPixelClassification;
import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;

public class WekaPixelClassificationLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new WekaPixelClassification(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[] { "WekaProbabilityMaps" };
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        HashMap<String,String> parameterNames = new HashMap<String,String>();
        parameterNames.put("Block size (simultaneous slices)", WekaPixelClassification.SIMULTANEOUS_SLICES);
        
        return parameterNames;

    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        return new HashMap<String, HashMap<String, String>>();
    }

}
