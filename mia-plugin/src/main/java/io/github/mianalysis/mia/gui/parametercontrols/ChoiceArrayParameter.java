package io.github.mianalysis.mia.gui.parametercontrols;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.core.OutputControl;
import io.github.mianalysis.mia.object.parameters.abstrakt.ChoiceType;

/**
 * Created by sc13967 on 22/05/2017.
 */
public class ChoiceArrayParameter extends TextSwitchableParameterControl implements ActionListener  {
    private WiderDropDownCombo choiceControl;
    
    public ChoiceArrayParameter(ChoiceType parameter) {
        super(parameter);

        // Choices may have not been initialised when this first runs, so a blank list
        // is created
        String[] choices = parameter.getChoices();
        if (choices == null)
            choices = new String[] { "" };
        choiceControl = new WiderDropDownCombo(choices);

        choiceControl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        choiceControl.setSelectedItem(parameter.getChoice());
        choiceControl.addActionListener(this);
        choiceControl.setWide(true);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.addUndo();

        ((ChoiceType) parameter).setValueFromString((String) choiceControl.getSelectedItem());

        int idx = GUI.getModules().indexOf(parameter.getModule());
        if (idx <= GUI.getLastModuleEval() & !(parameter.getModule() instanceof OutputControl))
            GUI.setLastModuleEval(idx - 1);

        GUI.updateModules();
        GUI.updateParameters();

        updateControl();

    }

    @Override
    public JComponent getDefaultComponent() {
        return choiceControl;
    }

    @Override
    public void updateDefaultControl() {
            String[] choices = ((ChoiceType) parameter).getChoices();
            if (choices == null)
                choices = new String[] { "" };

            // Getting previously-selected item
            String selected = (String) choiceControl.getSelectedItem();

            // Creating a new model
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel(choices);
            model.setSelectedItem(selected);

            // Updating the control
            choiceControl.setModel(model);
            choiceControl.repaint();
            choiceControl.revalidate();

    }
}
