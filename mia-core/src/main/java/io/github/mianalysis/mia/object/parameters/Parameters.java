package io.github.mianalysis.mia.object.parameters;

import java.util.LinkedHashMap;

import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.parameters.abstrakt.ParameterI;
import io.github.mianalysis.mia.object.refs.collections.Refs;

/**
 * Created by sc13967 on 02/05/2017.
 */
public class Parameters extends LinkedHashMap<String, ParameterI> implements Refs<ParameterI> {

    // PUBLIC METHODS

    /**
     *
     */
    private static final long serialVersionUID = -1104212695371396327L;

    public boolean add(ParameterI parameter) {
        put(parameter.getName(), parameter);
        return true;
    }

    public void addAll(Parameters parameterCollection) {
        for (ParameterI parameter : parameterCollection.values())
            add(parameter);

    }

    public void removeAll(Parameters parameterCollection) {
        for (ParameterI parameter : parameterCollection.values())
            remove(parameter);
    }

    public <T extends ParameterI> T getParameter(String name) {
        return (T) get(name);

    }

    public <T> T getValue(String name, WorkspaceI workspace) {
        return getParameter(name).getValue(workspace);
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

    public boolean invalidParameterIsVisible() {
        for (ParameterI parameter : values()) {
            if (!parameter.isValid() && parameter.isVisible())
                return true;

            if (parameter instanceof ParameterGroup) {
                for (Parameters collection : ((ParameterGroup) parameter).getCollections(true).values()) {
                    if (collection.invalidParameterIsVisible())
                        return true;
                }
            }
        }

        return false;

    }

    public boolean hasVisibleParameters() {
        for (ParameterI parameter : values()) {
            if (parameter.isVisible())
                return true;

            if (parameter instanceof ParameterGroup) {
                for (Parameters collection : ((ParameterGroup) parameter).getCollections(true).values()) {
                    if (collection.hasVisibleParameters())
                        return true;
                }
            }
        }

        return false;

    }

    public Parameters duplicate() {
        Parameters copyParameters = new Parameters();

        for (ParameterI parameter : values())
            copyParameters.add(parameter.duplicate(parameter.getModule()));

        return copyParameters;

    }
}