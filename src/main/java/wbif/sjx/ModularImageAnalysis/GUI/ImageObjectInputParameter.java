package wbif.sjx.ModularImageAnalysis.GUI;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.Parameter;
import wbif.sjx.ModularImageAnalysis.Object.RelationshipCollection;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Created by Stephen on 20/05/2017.
 */
public class ImageObjectInputParameter extends WiderDropDownCombo implements ActionListener, FocusListener {
    private MainGUI gui;
    private HCModule module;
    private Parameter parameter;

    ImageObjectInputParameter(MainGUI gui, String[] names, HCModule module, Parameter parameter) {
        this.gui = gui;
        this.module = module;
        this.parameter = parameter;

        if (names != null) {
            for (String name : names) addItem(name);
            setSelectedItem(parameter.getValue());
        }
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

    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        parameter.setValue(getSelectedItem());

        int idx = gui.getModules().indexOf(module);
        if (idx <= gui.getLastModuleEval()) gui.setLastModuleEval(idx-1);
        if (gui.isBasicGUI()) {
            gui.populateBasicModules();
        } else {
            gui.populateModuleParameters();
        }
    }
}
