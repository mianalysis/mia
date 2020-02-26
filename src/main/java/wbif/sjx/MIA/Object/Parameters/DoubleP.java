package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Miscellaneous.GlobalVariables;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Object.Parameters.Abstract.TextType;

public class DoubleP extends TextType {
    protected String value;

    public DoubleP(String name, Module module, double value) {
        super(name,module);
        this.value = String.valueOf(value);
    }

    public DoubleP(String name, Module module, String value) {
        super(name,module);
        this.value = value;
    }

    public DoubleP(String name, Module module, double value, String description) {
        super(name,module,description);
        this.value = String.valueOf(value);
    }

    public DoubleP(String name, Module module, String value, String description) {
        super(name,module,description);
        this.value = value;
    }

    public void setValue(double value) {
        this.value = String.valueOf(value);
    }

    public void setValue(String value) throws NumberFormatException {
        // Checking this is valid
        if (GlobalVariables.containsValue(value)) {
            this.value = value;
        } else {
            try {
                Double.parseDouble(value);
                this.value = value;
            } catch (NumberFormatException e) {
                MIA.log.writeWarning("Module: \""+module.getName()+"\", parameter: \""+getName()+"\". Must be a double-precision number or global variable handle (e.g. V{name})");
            }
        }
    }

    @Override
    public String getRawStringValue() {
        return value;

    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        DoubleP newParameter = new DoubleP(name,newModule,value,getDescription());
        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }

    @Override
    public void setValueFromString(String value) {
        setValue(value);
    }

    @Override
    public <T> T getValue() throws NumberFormatException {
        return (T) (Double) Double.parseDouble(GlobalVariables.convertString(value,module.getModules()));
    }

    @Override
    public <T> void setValue(T value) {
        this.value = String.valueOf(value);

    }
}
