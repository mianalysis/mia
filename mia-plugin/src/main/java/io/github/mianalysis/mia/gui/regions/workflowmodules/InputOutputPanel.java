package io.github.mianalysis.mia.gui.regions.workflowmodules;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

import io.github.mianalysis.mia.module.ModuleI;

public class InputOutputPanel extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = 2844341750453225769L;

    private ModuleButton button;

    public InputOutputPanel() {
        // Initialising the panel
        setLayout(new GridBagLayout());
        setOpaque(false);
        
        validate();
        repaint();

    }

    public void updatePanel(ModuleI module) {
        removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(10, 10, 10, 10);
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
