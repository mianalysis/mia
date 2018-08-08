package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by steph on 28/07/2017.
 */
public class ViewControlButton extends JRadioButton implements ActionListener {
    public static final String BASIC_MODE = "Basic mode";
    public static final String EDITING_MODE = "Editing mode";

    public ViewControlButton(String command) {
        setFocusPainted(false);
        setText(command);
        setSelected(true);
        addActionListener(this);


    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (getText()) {
            case BASIC_MODE:
                GUI.renderBasicMode();
                break;

            case EDITING_MODE:
                try {
                    GUI.renderEditingMode();
                } catch (InstantiationException | IllegalAccessException e1) {
                    e1.printStackTrace();
                }
                break;
        }

        GUI.getViewMenu().setSelected(false);
        GUI.getViewMenu().setPopupMenuVisible(false);
    }
}
