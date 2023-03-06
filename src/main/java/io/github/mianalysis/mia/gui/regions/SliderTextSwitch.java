package io.github.mianalysis.mia.gui.regions;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.object.parameters.abstrakt.NumberType;

public class SliderTextSwitch extends JPopupMenu implements ActionListener {
    private final NumberType parameter;
    private final JMenuItem switchMenuItem;

    public SliderTextSwitch(NumberType parameter) {
        this.parameter = parameter;

        switchMenuItem = new JMenuItem();
        if (parameter.isSlider())
            switchMenuItem.setText("Switch to number");
        else
            switchMenuItem.setText("Switch to slider");
        switchMenuItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        switchMenuItem.addActionListener(this);

        add(switchMenuItem);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.addUndo();
        setVisible(false);

        switch (e.getActionCommand()) {
            case "Switch to number":
                parameter.setIsSlider(false);
                break;
            case "Switch to slider":
                parameter.setIsSlider(true);
                break;
        }

        GUI.updateModules();
        GUI.updateParameters();

    }
}
