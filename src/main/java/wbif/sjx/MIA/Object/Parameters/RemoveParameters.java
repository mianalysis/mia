package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.GUI.ParameterControls.ParameterControl;
import wbif.sjx.MIA.GUI.ParameterControls.RemoveParametersButton;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;

/**
 * Created by Stephen Cross on 01/02/2019.
 */
public class RemoveParameters extends Parameter {
    private ParameterGroup group;
    private ParameterCollection collection;

    public RemoveParameters(String name, Module module, ParameterGroup group, ParameterCollection collection) {
        super(name, module);
        this.group = group;
        this.collection = collection;
    }

    public RemoveParameters(String name, Module module, ParameterGroup group, ParameterCollection collection, String description) {
        super(name, module, description);
        this.group = group;
        this.collection = collection;
    }

    @Override
    protected ParameterControl initialiseControl() {
        return new RemoveParametersButton(this);
    }

    @Override
    public <T> T getFinalValue() {
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
    public boolean verify() {
        return true;
    }

    @Override
    public <T extends Parameter> T duplicate() {
        return null;
    }

    public ParameterGroup getGroup() {
        return group;
    }

    public ParameterCollection getCollection() {
        return collection;
    }
}
