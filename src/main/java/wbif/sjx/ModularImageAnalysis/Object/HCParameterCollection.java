package wbif.sjx.ModularImageAnalysis.Object;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * Created by sc13967 on 02/05/2017.
 */
public class HCParameterCollection extends LinkedHashMap<String,HCParameter> implements Serializable {


    // PUBLIC METHODS

    public void addParameter(HCParameter parameter) {
        put(parameter.getName(),parameter);

    }

    public HCParameter getParameter(String name) {
        return get(name);

    }

    public <T> T getValue(String name) {
        return get(name).getValue();

    }

    public boolean isVisible(String name) {
        return get(name).isVisible();

    }

    public void updateValue(String name, Object value) {
        get(name).setValue(value);

    }

    public void updateVisible(String name, boolean visible) {
        get(name).setVisible(visible);

    }

    public void updateValueRange(String name, Object valueRange) {
        get(name).setValueSource(valueRange);

    }
}