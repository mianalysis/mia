package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.GUI.ParameterControl;
import wbif.sjx.ModularImageAnalysis.GUI.ParameterControls.RemoveParametersButton;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;

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
    public <T> T getValue() {
        return null;
    }

    @Override
    public <T> void setValue(T value) {

    }

    @Override
    public String getValueAsString() {
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
