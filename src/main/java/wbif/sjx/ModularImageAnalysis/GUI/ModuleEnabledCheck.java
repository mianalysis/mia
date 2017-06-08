package wbif.sjx.ModularImageAnalysis.GUI;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;

import javax.swing.*;

/**
 * Created by sc13967 on 07/06/2017.
 */
public class ModuleEnabledCheck extends JCheckBox {
    private HCModule module;

    public ModuleEnabledCheck(HCModule module) {
        this.setSelected(module.isEnabled());
        this.setName("ModuleEnabledCheck");
        this.module = module;

    }

    public HCModule getModule() {
        return module;
    }
}
