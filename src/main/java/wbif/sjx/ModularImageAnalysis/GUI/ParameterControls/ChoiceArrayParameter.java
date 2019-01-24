package wbif.sjx.ModularImageAnalysis.GUI.ParameterControls;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.GUI.ParameterControl;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.ChoiceType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by sc13967 on 22/05/2017.
 */
public class ChoiceArrayParameter extends ParameterControl implements ActionListener {
    private ChoiceType parameter;
    private WiderDropDownCombo control;

    public ChoiceArrayParameter(ChoiceType parameter) {
        this.parameter = parameter;

        // Choices may have not been initialised when this first runs, so a blank list is created
        String[] choices = parameter.getChoices();
        if (choices == null) choices = new String[]{""};
        control = new WiderDropDownCombo(choices);

        control.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        control.setSelectedItem(parameter.getChoice());
        control.addActionListener(this);
        control.setWide(true);

    }

    public ChoiceType getParameter() {
        return parameter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        parameter.setChoice((String) control.getSelectedItem());

        int idx = GUI.getModules().indexOf(parameter.getModule());
        if (idx <= GUI.getLastModuleEval()) GUI.setLastModuleEval(idx-1);

        GUI.updateTestFile();
        GUI.updateModules(true);

        updateControl();

    }

    @Override
    public JComponent getComponent() {
        return control;
    }

    @Override
    public void updateControl() {
        DefaultComboBoxModel model = new DefaultComboBoxModel(parameter.getChoices());
        control.setModel(model);
    }
}
