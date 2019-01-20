package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.TextType;

public class DoubleParam extends TextType {
    private double value;

    public DoubleParam(String name, Module module, double value) {
        super(name,module);
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String getValueAsString() {
        return String.valueOf(value);
    }

    @Override
    public void setValueFromString(String text) {
        value = Double.valueOf(text);
    }
}
