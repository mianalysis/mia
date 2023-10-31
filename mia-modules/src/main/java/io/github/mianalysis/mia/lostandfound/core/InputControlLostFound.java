package io.github.mianalysis.mia.lostandfound.core;

import java.util.HashMap;

import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.module.lostandfound.LostAndFoundItem;
import io.github.mianalysis.mia.object.units.SpatialUnit;

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
        
        return parameterNames;

    }

    @Override
    public HashMap<String, HashMap<String, String>> getPreviousParameterValues() {
        HashMap<String, String> values = null;
        HashMap<String, HashMap<String, String>> parameterValues = null;

        values = new HashMap<>();
        values.put("METRE", SpatialUnit.AvailableUnits.METRE);
        values.put("CENTIMETRE", SpatialUnit.AvailableUnits.CENTIMETRE);
        values.put("MILLIMETRE", SpatialUnit.AvailableUnits.MILLIMETRE);
        values.put("MICROMETRE", SpatialUnit.AvailableUnits.MICROMETRE);
        values.put("NANOMETRE", SpatialUnit.AvailableUnits.NANOMETRE);
        values.put("ANGSTROM", SpatialUnit.AvailableUnits.ANGSTROM);
        
        parameterValues = new HashMap<>();
        parameterValues.put(InputControl.SPATIAL_UNIT, values);

        return parameterValues;
    
    }
}
