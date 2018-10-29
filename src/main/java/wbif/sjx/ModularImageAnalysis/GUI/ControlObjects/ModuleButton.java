package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.util.Map;

/**
 * Created by Stephen on 20/05/2017.
 */
public class ModuleButton extends JToggleButton implements ActionListener {
    private Module module;


    // CONSTRUCTOR

    public ModuleButton(Module module) {
        this.module = module;
        setFocusPainted(false);
        setSelected(false);
        addActionListener(this);
        setText(module.getNickname());
        setColour();
    }


    // PUBLIC METHODS

    public void setColour() {
        if (module.isEnabled() && module.isRunnable()) {
            setForeground(Color.BLACK);

            String text = getText();
            Map fontAttributes = getFont().getAttributes();
            fontAttributes.put(TextAttribute.STRIKETHROUGH,false);
            setFont(new Font(fontAttributes));

        } else if (module.isEnabled() &! module.isRunnable()) {
            setForeground(Color.RED);

            String text = getText();
            Map fontAttributes = getFont().getAttributes();
            fontAttributes.put(TextAttribute.STRIKETHROUGH,true);
            setFont(new Font(fontAttributes));

        } else {
            setForeground(Color.GRAY);

            String text = getText();
            Map fontAttributes = getFont().getAttributes();
            fontAttributes.put(TextAttribute.STRIKETHROUGH,false);
            setFont(new Font(fontAttributes));
        }
    }


    // GETTERS

    public Module getModule() {
        return module;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.setActiveModule(module);
        GUI.updateModules(false);
    }
}
