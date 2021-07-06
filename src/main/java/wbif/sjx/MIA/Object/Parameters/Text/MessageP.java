package wbif.sjx.MIA.Object.Parameters.Text;

import java.awt.Color;

import com.drew.lang.annotations.NotNull;

import wbif.sjx.MIA.GUI.ParameterControls.MessageArea;
import wbif.sjx.MIA.GUI.ParameterControls.ParameterControl;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;

public class MessageP extends TextAreaP {
    private Color color = Color.BLACK;
    private int controlHeight = 50;

    public MessageP(String name, Module module, Color color) {
        super(name, module, false);
        this.color = color;
        setExported(false);
    }

    public MessageP(String name, Module module, Color color, int controlHeight) {
        super(name, module, false);
        this.color = color;
        this.controlHeight = controlHeight;
        setExported(false);
    }

    public MessageP(String name, Module module, @NotNull String value, Color color) {
        super(name, module, value, false);
        this.color = color;
        setExported(false);
    }

    public MessageP(String name, Module module, @NotNull String value, Color color, int controlHeight) {
        super(name, module, value, false);
        this.color = color;
        this.controlHeight = controlHeight;
        setExported(false);
    }

    public MessageP(String name, Module module, @NotNull String value, Color color, String description) {
        super(name, module, value, false, description);
        this.color = color;
        setExported(false);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setControlHeight(int controlHeight) {
        this.controlHeight = controlHeight;
    }

    @Override
    public ParameterControl getControl() {
        return new MessageArea(this, controlHeight);
    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        MessageP newParameter = new MessageP(name, newModule, getValue(), color, getDescription());
        
        newParameter.setControlHeight(controlHeight);
        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
