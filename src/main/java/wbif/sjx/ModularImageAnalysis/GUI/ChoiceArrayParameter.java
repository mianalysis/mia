package wbif.sjx.ModularImageAnalysis.GUI;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.HCParameter;

import javax.swing.*;

/**
 * Created by sc13967 on 22/05/2017.
 */
public class ChoiceArrayParameter extends WiderDropDownCombo {
    private HCModule module;
    private HCParameter parameter;

    public ChoiceArrayParameter(HCModule module, HCParameter parameter, String[] choices) {
        super(choices);
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
