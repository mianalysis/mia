package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.TextType;

public class DoubleP extends TextType {
    private double value;

    public DoubleP(String name, Module module, double value) {
        super(name,module);
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
        return (T) new DoubleP(name,module,value);
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
