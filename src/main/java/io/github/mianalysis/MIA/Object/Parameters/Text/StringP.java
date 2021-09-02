package io.github.mianalysis.mia.Object.Parameters.Text;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Miscellaneous.GlobalVariables;
import io.github.mianalysis.mia.Object.Parameters.Abstract.Parameter;
import io.github.mianalysis.mia.Object.Parameters.Abstract.TextType;

public class StringP extends TextType {
    protected String value = "";

    public StringP(String name, Module module) {
        super(name,module);
    }

    public StringP(String name, Module module, @NotNull String value) {
        super(name,module);
        this.value = value;
    }

    public StringP(String name, Module module, @NotNull String value, String description) {
        super(name,module,description);
        this.value = value;
    }

    public void setValue(String value) {
            this.value = value;
    }

    @Override
    public String getRawStringValue() {
        return String.valueOf(value);
    }

    @Override
    public void setValueFromString(String value) {
        this.value = value;
    }

    @Override
    public <T> T getValue() {
        String converted1 = GlobalVariables.convertString(value, module.getModules());
        return (T) applyCalculation(converted1);

    }

    @Override
    public <T> void setValue(T value) {
        this.value = (String) value;
    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        StringP newParameter = new StringP(name,newModule,value,getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
