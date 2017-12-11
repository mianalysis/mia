package wbif.sjx.ModularImageAnalysis.GUI.ParameterControls;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.Parameter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by sc13967 on 22/05/2017.
 */
public class ChoiceArrayParameter extends WiderDropDownCombo implements ActionListener {
    private GUI gui;
    private HCModule module;
    private Parameter parameter;

    public ChoiceArrayParameter(GUI gui, HCModule module, Parameter parameter, String[] choices) {
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

        gui.updateModules();
    }
}
