package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Object.Parameters.Abstract.TextType;

public class DoubleP extends TextType {
    protected double value;

    public DoubleP(String name, Module module, double value) {
        super(name,module);
        this.value = value;
    }

    public DoubleP(String name, Module module, double value, String description) {
        super(name,module,description);
        this.value = value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String getValueAsString() {
        return String.valueOf(value);
    }

    @Override
    public <T extends Parameter> T duplicate() {
        return (T) new DoubleP(name,module,value,getDescription());
    }

    @Override
    public void setValueFromString(String value) {
        this.value = Double.valueOf(value);
    }

    @Override
    public <T> T getValue() {
        return (T) (Double) value;
    }

    @Override
    public <T> void setValue(T value) {
        this.value = (Double) value;
    }

}
