package wbif.sjx.ModularImageAnalysis.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by steph on 29/07/2017.
 */
public class InputOutputButton extends JButton implements ActionListener {
    static final String INPUT_OPTIONS = "Input options";
    static final String OUTPUT_OPTIONS = "Output options";

    private MainGUI gui;
    private static int buttonWidth = 300;
    private static int buttonHeight = 50;

    InputOutputButton(MainGUI gui, String command) {
        this.gui = gui;

        setPreferredSize(new Dimension(buttonWidth,buttonHeight));
        setText(command);
        addActionListener(this);
        setFocusPainted(false);

    }

    public static int getButtonWidth() {
        return buttonWidth;
    }

    public static void setButtonWidth(int buttonWidth) {
        InputOutputButton.buttonWidth = buttonWidth;
    }

    public static int getButtonHeight() {
        return buttonHeight;
    }

    public static void setButtonHeight(int buttonHeight) {
        InputOutputButton.buttonHeight = buttonHeight;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (getText()) {
            case INPUT_OPTIONS:
                gui.setActiveModule(gui.getAnalysis().getInputControl());
                gui.populateModuleParameters();
                break;

            case OUTPUT_OPTIONS:
                gui.setActiveModule(gui.getAnalysis().getOutputControl());
                gui.populateModuleParameters();
                break;

        }
    }
}
