package wbif.sjx.MIA.GUI.ParameterControls;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Object.Parameters.ObjMeasurementSelectorP;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.TreeMap;

/**
 * Created by Stephen Cross on 18/02/2020.
 */
public class RefSelectorParameter extends ParameterControl implements ActionListener {
    protected ObjMeasurementSelectorP parameter;
    protected JPanel control;
    protected JTextArea textArea;
    private HashSet<JCheckBox> checkboxes;

    public RefSelectorParameter(ObjMeasurementSelectorP parameter) {
        this.parameter = parameter;

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

        TreeMap<String,Boolean> states = parameter.getMeasurementStates();
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

            parameter.getMeasurementStates().put(name,state);

        }
    }
}
