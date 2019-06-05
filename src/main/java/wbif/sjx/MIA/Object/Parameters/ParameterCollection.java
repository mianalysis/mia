package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;

import java.util.LinkedHashSet;

/**
 * Created by sc13967 on 02/05/2017.
 */
public class ParameterCollection extends LinkedHashSet<Parameter> {

    // PUBLIC METHODS

    public <T extends Parameter> T getParameter(String name) {
        for (Parameter parameter:this) {
            if (parameter.getName().equals(name)) return (T) parameter;
        }
        return null;
    }

    public <T> T getValue(String name) {
        return getParameter(name).getValue();
    }

    public boolean isVisible(String name) {
        return getParameter(name).isVisible();

    }

    public <T> void updateValue(String name, T value) {
        getParameter(name).setValue(value);
    }

    public void updateVisible(String name, boolean visible) {
        for (Parameter parameter:this) {
            if (parameter.getName().equals(name)) {
                parameter.setVisible(visible);
                return;
            }
        }
    }

    public boolean hasVisibleParameters() {
        for (Parameter parameter:this) {
            if (parameter.isVisible()) return true;

            if (parameter instanceof ParameterGroup) {
                for (ParameterCollection collection:((ParameterGroup) parameter).getCollections()) {
                    if (collection.hasVisibleParameters()) return true;
                }
            }
        }

        return false;

    }
}