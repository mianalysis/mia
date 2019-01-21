package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.TextType;

public class IntegerP extends TextType {
    private int value;

    public IntegerP(String name, Module module, int value) {
        super(name,module);
        this.value = value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String getValueAsString() {
        return String.valueOf(value);
    }

    @Override
    public void setValueFromString(String value) {
        try {
            this.value = Integer.valueOf(value);
        } catch (NumberFormatException e) {
            System.err.println("This is a number parameter");
        }
    }

    @Override
    public <T> T getValue() {
        return (T) (Integer) value;
    }

    @Override
    public <T> void setValue(T value) {
        this.value = (Integer) value;
    }

}
