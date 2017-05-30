package wbif.sjx.ModularImageAnalysis.GUI;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;

import javax.swing.*;

/**
 * Created by Stephen on 20/05/2017.
 */
public class ModuleButton extends JToggleButton {
    private HCModule module;


    // CONSTRUCTOR

    public ModuleButton(HCModule module) {
        this.module = module;
        setFocusPainted(false);
        if (module != null) setText(module.getTitle());

    }


    // GETTERS

    public HCModule getModule() {
        return module;
    }
}
