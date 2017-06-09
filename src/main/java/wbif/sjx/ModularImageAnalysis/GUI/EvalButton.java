package wbif.sjx.ModularImageAnalysis.GUI;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Stephen on 08/06/2017.
 */
public class EvalButton extends JButton {
    private HCModule module;


    // CONSTRUCTOR

    public EvalButton(HCModule module) {
        this.module = module;
        setFocusPainted(false);
        setSelected(false);
        setMargin(new Insets(0,0,0,0));
        setName("EvalButton");
        setText("⇩");
        setFont(new Font(Font.SERIF,Font.BOLD,14));

    }


    // GETTERS

    public HCModule getModule() {
        return module;
    }
}
