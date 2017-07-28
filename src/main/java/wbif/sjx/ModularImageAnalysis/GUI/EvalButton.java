package wbif.sjx.ModularImageAnalysis.GUI;

import ij.IJ;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.ModuleCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Stephen on 08/06/2017.
 */
public class EvalButton extends JButton implements ActionListener {
    private MainGUI gui;
    private HCModule module;


    // CONSTRUCTOR

    EvalButton(MainGUI gui, HCModule module) {
        this.gui = gui;
        this.module = module;

        setFocusPainted(false);
        setSelected(false);
        setMargin(new Insets(0,0,0,0));
        setName("EvalButton");
        setText("⇩");
        setFont(new Font(Font.SERIF,Font.BOLD,14));
        addActionListener(this);

    }


    // GETTERS

    public HCModule getModule() {
        return module;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int idx = gui.getModules().indexOf(module);

        // If the module is ready to be evaluated
        if (idx <= gui.getLastModuleEval()) new Thread(() -> {
            try {
                gui.evaluateModule(module);
            } catch (GenericMIAException ex) {
                IJ.showMessage(ex.getMessage());
            }
        }).start();

        // If multiple modules will need to be evaluated first
        new Thread(() -> {
            for (int i = gui.getLastModuleEval()+1;i<=idx;i++) {
                HCModule module = gui.getModules().get(i);
                if (module.isEnabled()) try {
                    gui.evaluateModule(module);
                } catch (GenericMIAException ex) {
                    IJ.showMessage(ex.getMessage());
                }

            }
        }).start();
    }
}
