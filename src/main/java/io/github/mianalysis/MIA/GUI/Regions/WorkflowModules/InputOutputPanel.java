package io.github.mianalysis.MIA.GUI.Regions.WorkflowModules;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

import io.github.mianalysis.MIA.Module.Module;

public class InputOutputPanel extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = 2844341750453225769L;

    private ModuleButton button;

    public InputOutputPanel() {
        // Initialising the panel
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
