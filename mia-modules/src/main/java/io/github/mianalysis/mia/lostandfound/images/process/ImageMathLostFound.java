package io.github.mianalysis.mia.lostandfound.images.process;

import java.util.HashMap;

import io.github.mianalysis.mia.module.images.process.ImageMath;
import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;

public class ImageMathLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new ImageMath(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{""};
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        HashMap<String,String> parameterNames = new HashMap<String,String>();
        
        return parameterNames;

    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        HashMap<String, String> values = null;
        HashMap<String, HashMap<String, String>> parameterValues = null;

        values = new HashMap<>();
        values.put("Measurement value", ImageMath.ValueSources.MEASUREMENT);
        parameterValues = new HashMap<>();
        parameterValues.put(ImageMath.VALUE_SOURCE, values);
        
        return parameterValues;

    }
    
}
