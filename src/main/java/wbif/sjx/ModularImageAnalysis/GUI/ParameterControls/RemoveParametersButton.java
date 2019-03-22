package wbif.sjx.ModularImageAnalysis.GUI.ParameterControls;

import wbif.sjx.ModularImageAnalysis.GUI.GUI;
import wbif.sjx.ModularImageAnalysis.GUI.ParameterControl;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.RemoveParameters;

import javax.swing.*;
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

        // Iterate over parameters in collection
        control = new JButton("Remove");
        control.addActionListener(this);

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
        parameter.getGroup().removeCollection(parameter.getCollection());

        int idx = GUI.getModules().indexOf(parameter.getModule());
        if (idx <= GUI.getLastModuleEval()) GUI.setLastModuleEval(idx-1);

        GUI.updateModules(true);
        GUI.populateModuleParameters();

        updateControl();

    }
}
