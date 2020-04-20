package wbif.sjx.MIA.Object.Parameters.Text;

import java.awt.Color;

import javax.annotation.Nonnull;

import wbif.sjx.MIA.GUI.ParameterControls.MessageArea;
import wbif.sjx.MIA.GUI.ParameterControls.ParameterControl;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.Miscellaneous.GlobalVariables;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;

public class MessageP extends TextAreaP {
    private Color color = Color.BLACK;

    public MessageP(String name, Module module, Color color) {
        super(name, module, false);
        this.color = color;
        setExported(false);
    }

    public MessageP(String name, Module module, @Nonnull String value, Color color) {
        super(name, module, value, false);
        this.color = color;
        setExported(false);
    }

    public MessageP(String name, Module module, @Nonnull String value, Color color, String description) {
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

    @Override
    public ParameterControl getControl() {
        return new MessageArea(this);
    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        MessageP newParameter = new MessageP(name,newModule, getValue(),color,getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
