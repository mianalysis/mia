package io.github.mianalysis.mia.lostandfound.objects.filter;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;
import io.github.mianalysis.mia.module.objects.filter.FilterByMeasurementExtremes;

@Plugin(type = LostAndFoundItem.class, priority = Priority.LOW, visible = true)
public class FilterByMeasurementExtremesLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new FilterByMeasurementExtremes(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[] { "" };
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        HashMap<String, String> parameterNames = new HashMap<String, String>();

        return parameterNames;

    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        HashMap<String, String> values = new HashMap<>();
        HashMap<String, HashMap<String, String>> parameterValues = new HashMap<>();

        values.put("Remove object with largest measurement",
                FilterByMeasurementExtremes.FilterMethods.REMOVE_LARGEST);
        values.put("Remove object with smallest measurement",
                FilterByMeasurementExtremes.FilterMethods.REMOVE_SMALLEST);
        values.put("Retain object with largest measurement",
                FilterByMeasurementExtremes.FilterMethods.RETAIN_LARGEST);
        values.put("Retain object with smallest measurement",
                FilterByMeasurementExtremes.FilterMethods.RETAIN_SMALLEST);
                
        parameterValues.put(FilterByMeasurementExtremes.FILTER_METHOD, values);

        return parameterValues;

    }

}
