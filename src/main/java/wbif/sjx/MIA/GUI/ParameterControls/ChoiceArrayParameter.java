package wbif.sjx.MIA.GUI.ParameterControls;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Hidden.OutputControl;
import wbif.sjx.MIA.Object.Parameters.Abstract.ChoiceType;

/**
 * Created by sc13967 on 22/05/2017.
 */
public class ChoiceArrayParameter extends ParameterControl implements ActionListener {
    private WiderDropDownCombo control;

    public ChoiceArrayParameter(ChoiceType parameter) {
        super(parameter);

        // Choices may have not been initialised when this first runs, so a blank list is created
        String[] choices = parameter.getChoices();
        if (choices == null) choices = new String[]{""};
        control = new WiderDropDownCombo(choices);

        control.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        control.setSelectedItem(parameter.getChoice());
        control.addActionListener(this);
        control.setWide(true);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.addUndo();

        ((ChoiceType) parameter).setChoice((String) control.getSelectedItem());

        int idx = GUI.getModules().indexOf(parameter.getModule());
        if (idx <= GUI.getLastModuleEval() & !(parameter.getModule() instanceof OutputControl)) GUI.setLastModuleEval(idx-1);

        GUI.updateTestFile(true);
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
        String[] choices = ((ChoiceType) parameter).getChoices();
        if (choices == null) choices = new String[]{""};

        // Getting previously-selected item
        String selected = (String) control.getSelectedItem();

        // Creating a new model
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel(choices);
        model.setSelectedItem(selected);

        // Updating the control
        control.setModel(model);
        control.repaint();
        control.revalidate();

    }
}
