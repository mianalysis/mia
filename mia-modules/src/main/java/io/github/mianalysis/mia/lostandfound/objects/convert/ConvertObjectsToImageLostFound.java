package io.github.mianalysis.mia.lostandfound.objects.convert;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;
import io.github.mianalysis.mia.module.objects.convert.ConvertObjectsToImage;

@Plugin(type = LostAndFoundItem.class, priority = Priority.LOW, visible = true)
public class ConvertObjectsToImageLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new ConvertObjectsToImage(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{""};
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        HashMap<String,String> parameterNames = new HashMap<String,String>();
        parameterNames.put("Measurement", ConvertObjectsToImage.MEASUREMENT_FOR_COLOUR);
        
        return parameterNames;

    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        return new HashMap<String, HashMap<String, String>>();
    }
    
}
