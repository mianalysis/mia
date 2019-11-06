package wbif.sjx.MIA.GUI.Panels;

import wbif.sjx.MIA.GUI.ControlObjects.ModuleButton;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Module;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;

public class InputOutputPanel extends JPanel {
    private ModuleButton button;

    private static final int minimumWidth = 310;

    public InputOutputPanel() {
        int bigButtonSize = GUI.getBigButtonSize();

        // Initialising the panel
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        setPreferredSize(new Dimension(minimumWidth, bigButtonSize+15));
        setLayout(new GridBagLayout());

        validate();
        repaint();

    }

    public void updatePanel(Module module) {
        removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.PAGE_START;
        c.fill = GridBagConstraints.BOTH;

        button = new ModuleButton(module);
        add(button, c);

        revalidate();
        repaint();

    }

    public void updateButtonState() {
        if (button == null) return;
        button.updateState();
    }
}
