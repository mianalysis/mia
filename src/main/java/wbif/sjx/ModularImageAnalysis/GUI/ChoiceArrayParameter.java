package wbif.sjx.ModularImageAnalysis.GUI;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.Parameter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by sc13967 on 22/05/2017.
 */
public class ChoiceArrayParameter extends WiderDropDownCombo implements ActionListener {
    private MainGUI gui;
    private HCModule module;
    private Parameter parameter;

    ChoiceArrayParameter(MainGUI gui, HCModule module, Parameter parameter, String[] choices) {
        super(choices);

        this.gui = gui;
        this.module = module;
        this.parameter = parameter;

        setSelectedItem(parameter.getValue());
        addActionListener(this);
        setWide(true);

    }

    public HCModule getModule() {
        return module;
    }

    public Parameter getParameter() {
        return parameter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        parameter.setValue(getSelectedItem());

        int idx = gui.getModules().indexOf(module);
        if (idx <= gui.getLastModuleEval()) gui.setLastModuleEval(idx-1);

        gui.updateEvalButtonStates();
        if (!gui.isBasicGUI()) gui.populateModuleParameters();

    }
}
