package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Hidden.GlobalVariables;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Object.Parameters.Abstract.TextType;
import wbif.sjx.MIA.Process.Logging.Log;

public class IntegerP extends TextType {
    protected String value;

    public IntegerP(String name, Module module, int value) {
        super(name,module);
        this.value = String.valueOf(value);
    }

    public IntegerP(String name, Module module, String value) {
        super(name,module);
        this.value = value;
    }

    public IntegerP(String name, Module module, int value, String description) {
        super(name,module,description);
        this.value = String.valueOf(value);
    }

    public IntegerP(String name, Module module, String value, String description) {
        super(name,module,description);
        this.value = value;
    }

    public void setValue(int value) {
        this.value = String.valueOf(value);
    }

    public void setValue(String value) {
        // Checking this is valid
        if (GlobalVariables.containsMetadata(value)) {
            this.value = value;
        } else {
            try {
                Integer.parseInt(value);
                this.value = value;
            } catch (NumberFormatException e) {
                MIA.log.write("Must be an integer-precision number or metadata handle (e.g. ${name})",Log.Level.WARNING);
            }
        }
    }

    @Override
    public String getRawStringValue() {
        return value;
    }

    @Override
    public void setValueFromString(String value) {
        setValue(value);
    }

    @Override
    public <T> T getValue() {
        return (T) (Integer) Integer.parseInt(MIA.getGlobalVariables().convertString(value));
    }

    @Override
    public <T> void setValue(T value) {
        this.value = String.valueOf(value);
    }

    @Override
    public <T extends Parameter> T duplicate() {
        IntegerP newParameter = new IntegerP(name,module,value,getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
