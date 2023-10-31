package io.github.mianalysis.mia.lostandfound.images.process;

import java.util.HashMap;

import io.github.mianalysis.mia.module.images.process.FilterImage;
import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;

public class FilterImageLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new FilterImage(null).getClass().getSimpleName();
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
        values.put("Difference of Gaussian 2D", FilterImage.FilterModes.LOG2DAPPROX);

        parameterValues = new HashMap<>();
        parameterValues.put(FilterImage.FILTER_MODE, values);

        return parameterValues;

    }
    
}
