package wbif.sjx.MIA.GUI.ParameterControls;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Object.Parameters.RemoveParameters;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Stephen Cross on 01/02/2019.
 */
public class RemoveParametersButton extends ParameterControl implements ActionListener {
    private RemoveParameters parameter;
    private JButton control;


    public RemoveParametersButton(RemoveParameters parameter) {
        this.parameter = parameter;

        control = new JButton("Remove");
        control.addActionListener(this);
        control.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

    }

    @Override
    public JComponent getComponent() {
        return control;
    }

    @Override
    public void updateControl() {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.addUndo();

        parameter.getGroup().removeCollection(parameter.getCollection());

        int idx = GUI.getModules().indexOf(parameter.getModule());
        if (idx <= GUI.getLastModuleEval()) GUI.setLastModuleEval(idx-1);

        GUI.updateModuleStates(true);
        GUI.updateModules();
        GUI.updateParameters();

        updateControl();

    }
}
