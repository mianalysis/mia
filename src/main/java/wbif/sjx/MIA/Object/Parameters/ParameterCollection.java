package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Object.References.Abstract.RefCollection;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * Created by sc13967 on 02/05/2017.
 */
public class ParameterCollection extends LinkedHashMap<String,Parameter> implements RefCollection<Parameter>, Serializable {

    // PUBLIC METHODS

    public boolean add(Parameter parameter) {
        put(parameter.getName(),parameter);
        return true;
    }

    public void addAll(ParameterCollection parameterCollection) {
        for (Parameter parameter:parameterCollection.values()) add(parameter);

    }

    public <T extends Parameter> T getParameter(String name) {
        return (T) get(name);

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
        get(name).setVisible(visible);
    }

    public boolean hasVisibleParameters() {
        for (Parameter parameter:values()) {
            if (parameter.isVisible()) return true;

            if (parameter instanceof ParameterGroup) {
                for (ParameterCollection collection:((ParameterGroup) parameter).getCollections()) {
                    if (collection.hasVisibleParameters()) return true;
                }
            }
        }

        return false;

    }

    public ParameterCollection duplicate() {
        ParameterCollection copyParameters = new ParameterCollection();

        for (Parameter parameter:values()) copyParameters.add(parameter.duplicate(parameter.getModule()));

        return copyParameters;

    }
}