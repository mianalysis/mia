package io.github.mianalysis.mia.lostandfound.objects.measure.miscellaneous;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;
import io.github.mianalysis.mia.module.objects.measure.miscellaneous.ObjectMeasurementCalculator;

@Plugin(type = LostAndFoundItem.class, priority = Priority.LOW, visible = true)
public class ObjectMeasurementCalculatorLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new ObjectMeasurementCalculator(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{""};
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        return new HashMap<String,String>();
    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        HashMap<String, String> values = null;
        HashMap<String, HashMap<String, String>> parameterValues = null;

        values = new HashMap<>();
        values.put("Add measurement 1 and measurement 2", ObjectMeasurementCalculator.CalculationModes.ADD);
        values.put("Divide measurement 1 by measurement 2", ObjectMeasurementCalculator.CalculationModes.DIVIDE);
        values.put("Multiply measurement 1 and measurement 2", ObjectMeasurementCalculator.CalculationModes.MULTIPLY);
        values.put("Subtract measurement 2 from measurement 1", ObjectMeasurementCalculator.CalculationModes.SUBTRACT);
        
        parameterValues = new HashMap<>();
        parameterValues.put(ObjectMeasurementCalculator.CALCULATION_MODE, values);

        return parameterValues;
    
    }
}
