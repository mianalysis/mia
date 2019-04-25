package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.GUI.ParameterControls.ParameterControl;
import wbif.sjx.MIA.GUI.ParameterControls.SeparatorParameter;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;

import javax.swing.*;
import java.awt.*;

public class GUISeparatorP extends Parameter {
    private String headingText;

    public GUISeparatorP(String name, Module module, String headingText) {
        super(name, module);
        this.headingText = headingText;

        setExported(false);

    }

    @Override
    protected ParameterControl initialiseControl() {
        return new SeparatorParameter(this);

    }

    @Override
    public <T> T getValue() {
        return (T) headingText;
    }

    @Override
    public <T> void setValue(T value) {
        headingText = (String) value;
    }

    @Override
    public String getValueAsString() {
        return headingText;
    }

    @Override
    public boolean verify() {
        return true;
    }

    @Override
    public <T extends Parameter> T duplicate() {
        return (T) new GUISeparatorP(name,module,headingText);
    }
}
