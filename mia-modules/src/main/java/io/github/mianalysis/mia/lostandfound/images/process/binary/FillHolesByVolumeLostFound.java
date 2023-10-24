package io.github.mianalysis.mia.lostandfound.images.process.binary;

import java.util.HashMap;

import io.github.mianalysis.mia.module.images.process.binary.FillHolesByVolume;
import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;

public class FillHolesByVolumeLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new FillHolesByVolume(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{""};
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        HashMap<String,String> parameterNames = new HashMap<String,String>();
        parameterNames.put("Use minimum volume", FillHolesByVolume.SET_MINIMUM_VOLUME);
        parameterNames.put("Minimum size", FillHolesByVolume.MINIMUM_VOLUME);
        parameterNames.put("Use maximum volume", FillHolesByVolume.SET_MAXIMUM_VOLUME);
        parameterNames.put("Maximum size", FillHolesByVolume.MAXIMUM_VOLUME);
        
        return parameterNames;

    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        return new HashMap<String, HashMap<String, String>>();
    }
    
}
