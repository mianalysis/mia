package io.github.mianalysis.mia.lostandfound.core;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;
import io.github.mianalysis.mia.object.units.SpatialUnit;

@Plugin(type = LostAndFoundItem.class, priority = Priority.LOW, visible = true)
public class InputControlLostFound extends LostAndFoundItem {

    @Override
    public String getModuleName() {
        return new InputControl(null).getClass().getSimpleName();
    }

    @Override
    public String[] getPreviousModuleNames() {
        return new String[]{""};
    }

    @Override
    public HashMap<String, String> getPreviousParameterNames() {
        HashMap<String,String> parameterNames = new HashMap<String,String>();
        parameterNames.put("Spatial units", InputControl.SPATIAL_UNIT);
        parameterNames.put("Ignore case", "");
        
        return parameterNames;

    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        HashMap<String, HashMap<String, String>> parameterValues = new HashMap<>();
        
        HashMap<String, String> values = new HashMap<>();
        values.put("METRE", SpatialUnit.AvailableUnits.METRE);
        values.put("CENTIMETRE", SpatialUnit.AvailableUnits.CENTIMETRE);
        values.put("MILLIMETRE", SpatialUnit.AvailableUnits.MILLIMETRE);
        values.put("MICROMETRE", SpatialUnit.AvailableUnits.MICROMETRE);
        values.put("NANOMETRE", SpatialUnit.AvailableUnits.NANOMETRE);
        values.put("ANGSTROM", SpatialUnit.AvailableUnits.ANGSTROM);        
        parameterValues.put(InputControl.SPATIAL_UNIT, values);

        values = new HashMap<>();
        values.put("Series list (comma separated)", InputControl.SeriesModes.SERIES_LIST);
        parameterValues.put(InputControl.SERIES_MODE, values);

        return parameterValues;
    
    }
}
