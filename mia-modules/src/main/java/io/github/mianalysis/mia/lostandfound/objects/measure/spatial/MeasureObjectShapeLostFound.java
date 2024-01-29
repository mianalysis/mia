package io.github.mianalysis.mia.lostandfound.objects.measure.spatial;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;
import io.github.mianalysis.mia.module.objects.measure.spatial.MeasureObjectShape;

@Plugin(type = LostAndFoundItem.class, priority = Priority.LOW, visible = true)
public class MeasureObjectShapeLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new MeasureObjectShape(null).getClass().getSimpleName();
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
        values.put("3", MeasureObjectShape.SurfaceAreaMethods.THREE);
        values.put("13", MeasureObjectShape.SurfaceAreaMethods.THIRTEEN);
        parameterValues = new HashMap<>();
        parameterValues.put(MeasureObjectShape.SURFACE_AREA_METHOD, values);

        return parameterValues;

    }
    
}
