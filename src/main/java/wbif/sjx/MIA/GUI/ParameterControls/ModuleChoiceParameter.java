package wbif.sjx.MIA.GUI.ParameterControls;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.Hidden.OutputControl;
import wbif.sjx.MIA.Object.Parameters.ModuleP;

/**
 * Created by sc13967 on 22/05/2017.
 */
public class ModuleChoiceParameter extends ParameterControl implements ActionListener {
    private ModuleP parameter;
    private WiderDropDownCombo control;

    public ModuleChoiceParameter(ModuleP parameter) {
        this.parameter = parameter;

        // Choices may have not been initialised when this first runs, so a blank list is created
        Module[] choices = parameter.getModules();
        control = new WiderDropDownCombo(choices);

        control.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        control.setSelectedItem(parameter.getSelectedModule());
        control.addActionListener(this);
        control.setWide(true);

    }

    public ModuleP getParameter() {
        return parameter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.addUndo();

        parameter.setSelectedModule((Module) control.getSelectedItem());

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
        Module[] choices = parameter.getModules();

        // Getting previously-selected item
        Module selected = (Module) control.getSelectedItem();

        // Creating a new model
        DefaultComboBoxModel<Module> model = new DefaultComboBoxModel(choices);
        model.setSelectedItem(selected);

        // Updating the control
        control.setModel(model);
        control.repaint();
        control.revalidate();

    }
}
