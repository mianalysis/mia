// TODO: Could have separator parameter, which draws a line across the GUI

package wbif.sjx.ModularImageAnalysis.Object;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * Created by sc13967 on 02/05/2017.
 */
public class ParameterCollection extends LinkedHashMap<String,Parameter> implements Serializable {


    // PUBLIC METHODS

    public void add(Parameter parameter) {
        put(parameter.getName(),parameter);

    }

    public Parameter getParameter(String name) {
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

    public void updateValueSource(String name, Object valueRange) {
        get(name).setValueSource(valueRange);

    }
}