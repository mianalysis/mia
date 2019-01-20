package wbif.sjx.ModularImageAnalysis.Object;

import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;

import java.util.LinkedHashSet;

/**
 * Created by sc13967 on 02/05/2017.
 */
public class ParameterCollection extends LinkedHashSet<Parameter> {

    // PUBLIC METHODS

    public <T extends Parameter> T getParameter(String name) {
        for (Parameter parameter:this) {
            System.err.println("Para "+parameter.getName()+"_____"+name);
            if (parameter.getName().equals(name)) return (T) parameter;
            System.err.println("    Still here");
        }
        return null;
    }

    public <T> T getValue(String name) {
        return getParameter(name).getValue();
    }

//    public <T> T getDoubleValue(String name) {
//        return get(name).getDoubleValue();
//
//    }
//
//    public boolean isVisible(String name) {
//        return get(name).isVisible();
//
//    }
//
//    public void updateValue(String name, Object value) {
//        get(name).setValue(value);
//
//    }

    public void updateVisible(String name, boolean visible) {
        for (Parameter parameter:this) {
            if (parameter.getName().equals(name)) {
                parameter.setVisible(visible);
                return;
            }
        }
    }

//    public void updateValueSource(String name, Object valueRange) {
//        get(name).setValueSource(valueRange);
//
//    }
}