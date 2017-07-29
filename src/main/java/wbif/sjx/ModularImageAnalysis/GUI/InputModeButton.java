package wbif.sjx.ModularImageAnalysis.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class InputModeButton extends JButton implements ActionListener {
    MainGUI gui;

    InputModeButton(MainGUI gui) {
        this.gui = gui;

        setName("Input options");
        setFocusPainted(false);
        setMargin(new Insets(0,0,0,0));
        setPreferredSize(new Dimension(10,10));

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        gui.populateInputMode();

    }
}
