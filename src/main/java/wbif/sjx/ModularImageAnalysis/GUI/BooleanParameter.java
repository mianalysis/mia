package wbif.sjx.ModularImageAnalysis.GUI;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.HCParameter;

import javax.swing.*;

/**
 * Created by Stephen on 20/05/2017.
 */
public class BooleanParameter extends JCheckBox {
    private HCModule module;
    private HCParameter parameter;

    public BooleanParameter(HCModule module, HCParameter parameter) {
        this.module = module;
        this.parameter = parameter;

    }

    public HCModule getModule() {
        return module;
    }

    public HCParameter getParameter() {
        return parameter;
    }
}
