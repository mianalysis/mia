package io.github.mianalysis.mia.lostandfound.objects.measure.intensity;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;
import io.github.mianalysis.mia.module.objects.measure.intensity.MeasureObjectIntensity;

@Plugin(type = LostAndFoundItem.class, priority = Priority.LOW, visible = true)
public class MeasureObjectIntensityLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new MeasureObjectIntensity(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{""};
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        HashMap<String,String> parameterNames = new HashMap<String,String>();
        parameterNames.put("Measure weighted distance to edge", "");
        parameterNames.put("Edge distance mode", "");
        parameterNames.put("Measure intensity profile from edge", "");
        parameterNames.put("Minimum distance", "");
        parameterNames.put("Maximum distance", "");
        parameterNames.put("Calibrated distances", "");
        parameterNames.put("Number of measurements", "");
        parameterNames.put("Only measure on masked regions", "");
        parameterNames.put("Mask image", "");
        
        return parameterNames;

    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        return new HashMap<String, HashMap<String, String>>();
    }
    
}
