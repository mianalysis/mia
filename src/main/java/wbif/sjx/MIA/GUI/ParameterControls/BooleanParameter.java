package wbif.sjx.MIA.GUI.ParameterControls;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComponent;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Hidden.OutputControl;
import wbif.sjx.MIA.Object.Parameters.Abstract.BooleanType;

/**
 * Created by Stephen on 20/05/2017.
 */
public class BooleanParameter extends ParameterControl implements ActionListener {
    private JCheckBox control;

    public BooleanParameter(BooleanType parameter) {
        super(parameter);

        control = new JCheckBox();

        control.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        control.setSelected(parameter.isSelected());
        control.addActionListener(this);
        control.setOpaque(false);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.addUndo();

        ((BooleanType) parameter).setSelected(control.isSelected());

        int idx = GUI.getModules().indexOf(parameter.getModule());
        if (idx <= GUI.getLastModuleEval() & !(parameter.getModule() instanceof OutputControl)) GUI.setLastModuleEval(idx-1);

        GUI.updateModuleStates(true);
        GUI.updateModules();
        GUI.updateParameters();

        updateControl();

    }
    @Override
    public JComponent getComponent() {
        return control;
    }

    @Override
    public void updateControl() {
        control.setSelected(((BooleanType) parameter).isSelected());
    }
}
