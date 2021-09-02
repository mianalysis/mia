package io.github.mianalysis.mia.gui.parametercontrols;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.core.OutputControl;
import io.github.mianalysis.mia.module.miscellaneous.GUISeparator;
import io.github.mianalysis.mia.object.Colours;
import io.github.mianalysis.mia.object.parameters.ModuleP;

/**
 * Created by sc13967 on 22/05/2017.
 */
public class ModuleChoiceParameter extends ParameterControl implements ActionListener {
    private WiderDropDownCombo control;

    public ModuleChoiceParameter(ModuleP parameter) {
        super(parameter);

        // Choices may have not been initialised when this first runs, so a blank list
        // is created
        Module[] choices = parameter.getModules();
        control = new WiderDropDownCombo(choices);

        control.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        control.setSelectedItem(parameter.getSelectedModule());
        control.addActionListener(this);
        control.setWide(true);

        control.setRenderer(new ModuleListRenderer(control.getRenderer()));

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.addUndo();

        ((ModuleP) parameter).setSelectedModule((Module) control.getSelectedItem());

        int idx = GUI.getModules().indexOf(parameter.getModule());
        if (idx <= GUI.getLastModuleEval() & !(parameter.getModule() instanceof OutputControl))
            GUI.setLastModuleEval(idx - 1);

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
        Module[] choices = ((ModuleP) parameter).getModules();

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

class ModuleListRenderer extends DefaultListCellRenderer {
    private ListCellRenderer defaultRenderer;

    public ModuleListRenderer(ListCellRenderer defaultRenderer) {
        this.defaultRenderer = defaultRenderer;
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
            boolean cellHasFocus) {
        Component c = defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value == null) 
            return c;

        if (c instanceof JLabel && value instanceof GUISeparator)
            c.setForeground(Colours.DARK_BLUE);
        
        return c;
    }
}
