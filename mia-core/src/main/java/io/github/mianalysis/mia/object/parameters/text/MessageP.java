package io.github.mianalysis.mia.object.parameters.text;

import java.awt.Color;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.abstrakt.ParameterControl;
import io.github.mianalysis.mia.process.ParameterControlFactory;



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
        return ParameterControlFactory.getActiveFactory().getMessageTypeControl(this, controlHeight);
    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        MessageP newParameter = new MessageP(name, newModule, getRawStringValue(), color, getDescription());
        
        newParameter.setControlHeight(controlHeight);
        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
