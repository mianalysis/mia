package io.github.mianalysis.mia.gui.parametercontrols;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.TreeMap;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.object.parameters.ObjMeasurementSelectorP;

/**
 * Created by Stephen Cross on 18/02/2020.
 */
public class RefSelectorParameter extends ParameterControl implements ActionListener {
    protected JPanel control;
    protected JTextArea textArea;
    private HashSet<JCheckBox> checkboxes;

    public RefSelectorParameter(ObjMeasurementSelectorP parameter) {
        super(parameter);

        control = new JPanel();
        control.setLayout(new GridBagLayout());

        updateControl();

    }

    @Override
    public JComponent getComponent() {
        return control;
    }

    @Override
    public void updateControl() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0,50,0,0);

        control.removeAll();

        TreeMap<String,Boolean> states = ((ObjMeasurementSelectorP) parameter).getMeasurementStates();
        checkboxes = new HashSet<>();

        for (String name:states.keySet()) {
            JCheckBox state = new JCheckBox(name,states.get(name));
            state.setBorder(null);
            state.setOpaque(false);
            state.setPreferredSize(new Dimension(0, GUI.getElementHeight()));
            state.addActionListener(this);

            control.add(state,c);
            c.gridy++;

            checkboxes.add(state);

        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JCheckBox) {
            JCheckBox checkBox = (JCheckBox) e.getSource();
            String name = checkBox.getText();
            boolean state = checkBox.isSelected();

            ((ObjMeasurementSelectorP) parameter).getMeasurementStates().put(name,state);

        }
    }
}
