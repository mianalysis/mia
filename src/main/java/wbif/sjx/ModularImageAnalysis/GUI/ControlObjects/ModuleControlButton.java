package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Stephen on 28/07/2017.
 */
public class ModuleControlButton extends JButton implements ActionListener {

    public static final String ADD_MODULE = "+";
    public static final String REMOVE_MODULE = "-";
    public static final String MOVE_MODULE_UP = "▲";
    public static final String MOVE_MODULE_DOWN = "▼";

    public ModuleControlButton(String command, int buttonSize) {
        setText(command);
        addActionListener(this);
        setMargin(new Insets(0,0,0,0));
        setFocusPainted(false);
        setMargin(new Insets(0,0,0,0));
        setPreferredSize(new Dimension(buttonSize, buttonSize));

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (getText()) {
            case ADD_MODULE:
                GUI.addModule();
                break;

            case REMOVE_MODULE:
                GUI.removeModule();
                break;

            case MOVE_MODULE_UP:
                GUI.moveModuleUp();
                break;

            case MOVE_MODULE_DOWN:
                GUI.moveModuleDown();
                break;

        }
    }
}
