package wbif.sjx.HighContent.Object;

import wbif.sjx.HighContent.Module.HCModule;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * Created by sc13967 on 02/05/2017.
 */
public class HCParameterCollection implements Serializable {
    private LinkedHashMap<String,HCParameter> parameters = new LinkedHashMap<>();


    // PUBLIC METHODS

    public void addParameter(HCParameter parameter) {
        parameters.put(parameter.getName(),parameter);

    }

    public HCParameter getParameter(String name) {
        return parameters.get(name);

    }

    public <T> T getValue(String name) {
        return (T) parameters.get(name).getValue();

    }

    public boolean isVisible(String name) {
        return parameters.get(name).isVisible();

    }

    public void updateValue(String name, Object value) {
        parameters.get(name).setValue(value);

    }

    public void updateVisible(String name, boolean visible) {
        parameters.get(name).setVisible(visible);

    }

    public void updateValueRange(String name, Object valueRange) {
        parameters.get(name).setValueSource(valueRange);

    }


    // GETTERS AND SETTERS

    public LinkedHashMap<String, HCParameter> getParameters() {
        return parameters;
    }

    public void setParameters(LinkedHashMap<String, HCParameter> parameters) {
        this.parameters = parameters;
    }

}