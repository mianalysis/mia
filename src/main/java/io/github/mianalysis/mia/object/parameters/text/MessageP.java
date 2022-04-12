package io.github.mianalysis.mia.object.parameters.text;

import java.awt.Color;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.gui.parametercontrols.MessageArea;
import io.github.mianalysis.mia.gui.parametercontrols.ParameterControl;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;

public class MessageP extends TextAreaP {
    private Color color = Color.BLACK;
    private String icon = null;
    private int controlHeight = 50;

    public interface Icons {
        String NONE = "None";
        String WARNING = "Warning";
        String ALERT = "Alert";
    }
    

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

    public MessageP(String name, Module module, @NotNull String value, String icon, Color color) {
        super(name, module, value, false);
        this.color = color;
        this.icon = icon;
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

    public MessageP(String name, Module module, @NotNull String value, String icon, Color color, String description) {
        super(name, module, value, false, description);
        this.color = color;
        this.icon = icon;
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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public ParameterControl getControl() {
        return new MessageArea(this, controlHeight);
    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        MessageP newParameter = new MessageP(name, newModule, getValue(), icon, color, getDescription());
        
        newParameter.setControlHeight(controlHeight);
        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
