package io.github.mianalysis.mia.lostandfound.objects.measure.spatial;

import java.util.HashMap;

import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;
import io.github.mianalysis.mia.module.objects.measure.spatial.MeasureObjectCurvature;

public class MeasureObjectCurvatureLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new MeasureObjectCurvature(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{"FitSpline"};
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
