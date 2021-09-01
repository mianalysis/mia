package io.github.mianalysis.MIA.Object.Parameters;

import io.github.mianalysis.MIA.GUI.ParameterControls.ParameterControl;
import io.github.mianalysis.MIA.GUI.ParameterControls.AdjustParameterGroupButton;
import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Object.Parameters.Abstract.Parameter;

/**
 * Created by Stephen Cross on 01/02/2019.
 */
public class AdjustParameters extends Parameter {
    private ParameterGroup group;
    private int collectionIndex;

    public AdjustParameters(String name, Module module, ParameterGroup group, int collectionIndex) {
        super(name, module);
        this.group = group;
        this.collectionIndex = collectionIndex;
    }

    @Override
    protected ParameterControl initialiseControl() {
        return new AdjustParameterGroupButton(this);
    }

    @Override
    public <T> T getValue() {
        return null;
    }

    @Override
    public <T> void setValue(T value) {

    }

    @Override
    public String getRawStringValue() {
        return null;
    }

    @Override
    public void setValueFromString(String string) {

    }

    @Override
    public boolean verify() {
        return true;
    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        return null;
    }

    public ParameterGroup getGroup() {
        return group;
    }

    public int getCollectionIndex() {
        return collectionIndex;
    }
}
