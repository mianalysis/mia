package wbif.sjx.ModularImageAnalysis.GUI.ParameterControls;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by sc13967 on 22/05/2017.
 */
public class ChoiceArrayParameter extends WiderDropDownCombo implements ActionListener {
    private Module module;
    private Parameter parameter;

    public ChoiceArrayParameter(Module module, Parameter parameter, String[] choices) {
        super(choices);

        this.module = module;
        this.parameter = parameter;

        setSelectedItem(parameter.getValue());
        addActionListener(this);
        setWide(true);

    }

    public Module getModule() {
        return module;
    }

    public Parameter getParameter() {
        return parameter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        parameter.setValue(getSelectedItem());

        int idx = GUI.getModules().indexOf(module);
        if (idx <= GUI.getLastModuleEval()) GUI.setLastModuleEval(idx-1);

        GUI.updateTestFile();
        GUI.updateModules(true);

    }
}
