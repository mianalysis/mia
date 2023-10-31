package io.github.mianalysis.mia.lostandfound.objects.process;

import java.util.HashMap;

import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;
import io.github.mianalysis.mia.module.objects.process.FitGaussian2D;

public class FitGaussian2DLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new FitGaussian2D(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{""};
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        HashMap<String,String> parameterNames = new HashMap<String,String>();
        parameterNames.put("Method to estimate spot radius", FitGaussian2D.SIGMA_MODE);
        parameterNames.put("Radius", FitGaussian2D.SIGMA_MODE);
        parameterNames.put("Radius measurement", FitGaussian2D.SIGMA_MEASUREMENT);
        parameterNames.put("Minimum sigma (x Radius)", FitGaussian2D.MIN_SIGMA);
        parameterNames.put("Maximum sigma (x Radius)", FitGaussian2D.MAX_SIGMA);
        
        return parameterNames;

    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        return new HashMap<String, HashMap<String, String>>();
    }
    
}
