package io.github.mianalysis.mia.lostandfound.objects.process;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;
import io.github.mianalysis.mia.module.objects.process.CreateDistanceBands;

@Plugin(type = LostAndFoundItem.class, priority = Priority.LOW, visible = true)
public class CreateDistanceBandsLostFound extends LostAndFoundItem {
    @Override
    public String getModuleName() {
        return new CreateDistanceBands<>(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{""};
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        return new HashMap<String, String>();
    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        HashMap<String, String> values = null;
        HashMap<String, HashMap<String, String>> parameterValues = null;

        values = new HashMap<>();
        values.put("Borgefors (3,4,5)", CreateDistanceBands.WeightModes.BORGEFORS);
        values.put("City-Block (1,2,3)", CreateDistanceBands.WeightModes.CITY_BLOCK);
        values.put("Svensson (3,4,5,7)", CreateDistanceBands.WeightModes.WEIGHTS_3_4_5_7);
        parameterValues = new HashMap<>();
        parameterValues.put(CreateDistanceBands.WEIGHT_MODE, values);
        
        return parameterValues;

    }
}